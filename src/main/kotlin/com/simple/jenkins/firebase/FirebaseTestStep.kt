package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hudson.*
import hudson.model.Descriptor
import hudson.model.TaskListener
import jenkins.model.Jenkins
import org.jenkinsci.plugins.durabletask.BourneShellScript
import org.jenkinsci.plugins.durabletask.Controller
import org.jenkinsci.plugins.durabletask.DurableTask
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep
import org.kohsuke.stapler.DataBoundConstructor
import java.io.OutputStream
import java.util.*

class FirebaseTestStep @DataBoundConstructor constructor(val command: Command) : DurableTaskStep() {

    companion object {
        private val factory = YAMLFactory().apply {
            disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        }
        val mapper = ObjectMapper(factory).apply {
            registerKotlinModule()
            propertyNamingStrategy = PropertyNamingStrategy.KEBAB_CASE
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    override fun task(): DurableTask = FirebaseTestTask(command)

    @Extension class FirebaseTestStepDescriptor : DurableTaskStepDescriptor() {

        override fun getDisplayName(): String = "Run Firebase test"
        override fun getFunctionName(): String = "firebaseTest"

        @Suppress("unused")
        fun getCommandDescriptors(): List<Descriptor<Command>> =
                Jenkins.getInstance().getDescriptorList(Command::class.java)
    }

    class FirebaseTestTask(val command: Command) : DurableTask() {

        override fun launch(env: EnvVars, workspace: FilePath, launcher: Launcher, listener: TaskListener): Controller {
            val controller = FirebaseTestController(workspace)
            command.setup(controller)
            val script = "gcloud firebase test android run ${command.args()}"
            val delegate = BourneShellScript(script).apply { captureOutput() }.launch(env, workspace, launcher, listener)
            return controller.apply { this.delegate = delegate }
        }
    }

    class FirebaseTestController(val workspace: FilePath) : Controller()  {

        val argfile = controlDir(workspace).child("argfile.yaml")

        lateinit var delegate: Controller

        override fun exitStatus(workspace: FilePath, launcher: Launcher): Int? =
                delegate.exitStatus(workspace, launcher)

        override fun writeLog(workspace: FilePath, sink: OutputStream): Boolean =
                delegate.writeLog(workspace, sink)

        override fun getOutput(workspace: FilePath, launcher: Launcher): ByteArray =
                delegate.getOutput(workspace, launcher)

        override fun stop(workspace: FilePath, launcher: Launcher) =
                delegate.stop(workspace, launcher)

        override fun getDiagnostics(workspace: FilePath, launcher: Launcher): String =
                delegate.getDiagnostics(workspace, launcher)

        override fun cleanup(workspace: FilePath) {
            delegate.cleanup(workspace)
            controlDir(workspace).deleteRecursive()
        }

        fun controlDir(workspace: FilePath): FilePath = with (workspace) {
            mkdirs()
            child("firebase-" + Util.getDigestOf(UUID.randomUUID().toString()).substring(0, 8))
        }.apply { mkdirs() }
    }
}