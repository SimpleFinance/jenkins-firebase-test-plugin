package com.simple.jenkins.firebase

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.jenkins.plugins.credentials.oauth.*
import hudson.*
import hudson.model.Descriptor
import hudson.model.Item
import hudson.model.TaskListener
import hudson.remoting.Future
import hudson.remoting.VirtualChannel
import hudson.security.ACL
import hudson.slaves.WorkspaceList
import jenkins.MasterToSlaveFileCallable
import jenkins.model.Jenkins
import org.apache.commons.io.output.TeeOutputStream
import org.jenkinsci.plugins.durabletask.BourneShellScript
import org.jenkinsci.plugins.durabletask.Controller
import org.jenkinsci.plugins.durabletask.DurableTask
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.StepExecution
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.jvm.jvmName

class FirebaseTestStep @DataBoundConstructor constructor(val command: Command)
    : DurableTaskStep() {

    companion object {
        internal val logger: Logger = Logger.getLogger(FirebaseTestStep::class.jvmName)

        private val factory = YAMLFactory().apply {
            disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        }

        val mapper = ObjectMapper(factory).apply {
            registerKotlinModule()
            propertyNamingStrategy = PropertyNamingStrategy.KEBAB_CASE
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    @set:DataBoundSetter var credentialsId: String? = null

    private var credential: GoogleRobotPrivateKeyCredentials? = null

    override fun start(context: StepContext): StepExecution {
        credential = credentialsId?.let {
            findCredentials(context, it) ?: throw RuntimeException("Cannot locate Google OAuth credentials.")
        }
        return super.start(context)
    }

    override fun task(): DurableTask = FirebaseTestTask(credential, command)

    private fun findCredentials(context: StepContext, credentialsId: String): GoogleRobotPrivateKeyCredentials? =
            CredentialsProvider.findCredentialById(
                    credentialsId,
                    GoogleRobotPrivateKeyCredentials::class.java,
                    context.build(),
                    object : GoogleOAuth2ScopeRequirement() {
                        override fun getScopes(): Collection<String> = listOf()
                    })

    @Extension class FirebaseTestStepDescriptor : DurableTaskStepDescriptor() {

        override fun getDisplayName(): String = "Run Firebase test"
        override fun getFunctionName(): String = "firebaseTest"

        @Suppress("unused")
        fun getCommandDescriptors(): List<Descriptor<Command>> =
                Jenkins.getInstance().getDescriptorList(Command::class.java)

        fun doFillCredentialsIdItems(@AncestorInPath project: Item?) = StandardListBoxModel()
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM, project, GoogleRobotPrivateKeyCredentials::class.java)
    }

    class FirebaseTestTask(val credential: GoogleRobotPrivateKeyCredentials?, val command: Command) : DurableTask() {

        override fun launch(env: EnvVars, workspace: FilePath, launcher: Launcher, listener: TaskListener): Controller {
            val controlDirPath: String = let {
                val tag = Util.getDigestOf(UUID.randomUUID().toString()).substring(0, 8)
                WorkspaceList.tempDir(workspace.apply { mkdirs() }).child("firebase-$tag").apply { mkdirs() }.remote
            }
            val controlDir = workspace.child(controlDirPath)

            val controller = FirebaseTestController(workspace, controlDirPath)
            command.setup(controller)

            val script = StringBuilder()

            credential?.let {
                val config = credential.serviceAccountConfig
                val configDir = controlDir.child("gcloud").apply { mkdirs() }
                val keyFile = keyFile(config, configDir)

                env.override("GCLOUDSDK_CONFIG", configDir.remote)
                env.override("GOOGLE_APPLICATION_CREDENTIALS", keyFile.remote)
                script.append("gcloud auth activate-service-account ${config.accountId} --key-file=${keyFile.remote}\n")

                controller.googleCredentials = ServiceAccountCredentials.fromStream(keyFile.read())
            }

            script.append("gcloud firebase test android run ${command.args()} --format=yaml")

            val delegate = BourneShellScript(script.toString())
                    .apply { captureOutput() }
                    .launch(env, workspace, launcher, listener)

            return controller.apply { this.delegate = delegate }
        }

        private fun keyFile(config: ServiceAccountConfig, configDir: FilePath): FilePath {
            val path = when (config) {
                is JsonServiceAccountConfig -> config.jsonKeyFile
                is P12ServiceAccountConfig -> throw RuntimeException("Only JSON service account keys are supported")
                else -> throw RuntimeException("Invalid Google service account config")
            }
            return configDir.createTempFile("gcloud", "key").apply {
                copyFrom(FileInputStream(File(path)))
            }
        }
    }

    class FirebaseTestController(workspace: FilePath,
                                 @JvmField val controlDirPath: String) : Controller()  {

        companion object {
            private const val serialVersionUID: Long = 1
        }

        @JvmField var googleCredentials: GoogleCredentials? = null
        @JvmField var links: Links? = null
        @JvmField var status: Int? = null
        @JvmField var testResults: List<TestResult>? = null
        @JvmField var testArtifacts: Map<String, TestArtifacts>? = null

        val argfile by lazy { controlDir(workspace).child("argfile.yaml") }
        val logfile by lazy { controlDir(workspace).child("firebase.log") }
        val results by lazy { controlDir(workspace).child("junit").apply { mkdirs() } }

        @Transient var resultFuture: Future<Map<String, TestArtifacts>>? = null

        lateinit var delegate: Controller

        override fun exitStatus(workspace: FilePath, launcher: Launcher): Int? {
            status = delegate.exitStatus(workspace, launcher) ?: return null

            links = links ?: Links.parse(logfile.readToString())
            if (links == null) {
                logger.log(Level.WARNING, "Could not parse Firebase log output")
                return status
            }

            val output = delegate.getOutput(workspace, launcher)
            testResults = testResults ?:
                    mapper.readerFor(TestResult::class.java).readValues<TestResult>(output).readAll()
            if (testResults == null) {
                logger.log(Level.WARNING, "Could not parse Firebase test output")
                return status
            }

            if (testArtifacts != null) {
                return status
            }

            if (resultFuture == null) {
                resultFuture = results.actAsync(ArtifactFetcher(googleCredentials, links!!, testResults!!))
            }

            resultFuture?.let {
                if (it.isDone) {
                    try {
                        testArtifacts = it.get()
                    } catch (e: Exception) {
                        logger.log(Level.WARNING, "Failed to fetch test artifacts", e)
                    }
                    return status
                }
            }

            return null
        }

        override fun writeLog(workspace: FilePath, sink: OutputStream): Boolean {
            // This is dumb, but we need to copy the script's logs for two reasons:
            // 1. gcloud prints valuable data to stderr
            // 2. BourneShellScript doesn't let us grab logs or stderr output directly unless we subclass both
            //    FileMonitoringTask and FileMonitoringController (the latter is a protected inner class).
            val tee = TeeOutputStream(sink, logfile.append())
            return delegate.writeLog(workspace, tee)
        }

        override fun getOutput(workspace: FilePath, launcher: Launcher): ByteArray =
                delegate.getOutput(workspace, launcher)

        override fun stop(workspace: FilePath, launcher: Launcher) {
            delegate.stop(workspace, launcher)
            resultFuture?.cancel(true)
        }

        override fun getDiagnostics(workspace: FilePath, launcher: Launcher): String =
                delegate.getDiagnostics(workspace, launcher)

        override fun cleanup(workspace: FilePath) {
            delegate.cleanup(workspace)
            //controlDir(workspace).deleteRecursive()
        }

        fun controlDir(workspace: FilePath): FilePath = workspace.child(controlDirPath)
    }

    class ArtifactFetcher(val credentials: GoogleCredentials?, val links: Links, val testResults: List<TestResult>)
        : MasterToSlaveFileCallable<Map<String, TestArtifacts>>() {

        override fun invoke(f: File, channel: VirtualChannel?): Map<String, TestArtifacts> {
            val storage = StorageOptions.newBuilder()
                    .setCredentials(credentials ?: GoogleCredentials.getApplicationDefault())
                    .build()
                    .service

            val result = mutableMapOf<String, TestArtifacts>()

            for ((axisValue) in testResults) {
                val junit: String? = try {
                    val junitPrefix = "${links.dir}/$axisValue/test_result_"
                    val blobs = storage.list(links.bucket,
                            Storage.BlobListOption.prefix(junitPrefix))
                    with (blobs.values.first()) {
                        val dest = File(f, name)
                        storage.reader(blobId).transferTo(FileOutputStream(dest).channel)
                        dest.toRelativeString(f)
                    }
                } catch (e: Exception) {
                    logger.log(Level.WARNING, "Failed to fetch test artifact for $axisValue", e)
                    null
                }

                val logcat: String? = try {
                    val dest = File(f, "logcat")
                    storage.reader(BlobId.of(links.bucket, "$axisValue/logcat"))
                            .transferTo(FileOutputStream(dest).channel)
                    dest.toRelativeString(f)
                } catch (e: Exception) {
                    logger.log(Level.WARNING, "Failed to fetch test artifact for $axisValue", e)
                    null
                }

                result[axisValue] = TestArtifacts(junit, logcat)
            }

            return result
        }
    }

    data class Links(val bucket: String, val dir: String, val console: String) {

        companion object {
            private val bucketRegex =
                    Regex("""\[https://console\.developers\.google\.com\/storage\/browser\/(.+)\/(.+)\/\]""")
            private val consoleRegex =
                    Regex("""\[(https:\/\/console\.firebase\.google\.com\/project\/.+)\]""")

            fun parse(source: String): Links? {
                val (bucket, dir) = bucketRegex.find(source)?.destructured ?: return null
                val console = consoleRegex.find(source)?.groupValues?.getOrNull(1) ?: return null
                return Links(bucket, dir, console)
            }
        }
    }

    data class TestResult(val axisValue: String, val outcome: String, val testDetails: String)

    data class TestArtifacts(val junit: String?, val logcat: String?)
}