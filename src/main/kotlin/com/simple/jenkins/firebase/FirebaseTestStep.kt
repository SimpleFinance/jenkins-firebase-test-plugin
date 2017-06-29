package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.Proc
import hudson.model.Run
import hudson.model.TaskListener
import hudson.util.ListBoxModel
import org.jenkinsci.plugins.workflow.steps.Step
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.StepDescriptor
import org.jenkinsci.plugins.workflow.steps.StepExecution
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

@Extension
class FirebaseTestStep
@DataBoundConstructor constructor(val tests: Map<String, TestArguments>) : Step() {

    companion object {
        private val factory = YAMLFactory().apply {
            disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        }
        val mapper = ObjectMapper(factory).apply {
            registerKotlinModule()
            propertyNamingStrategy = PropertyNamingStrategy.KEBAB_CASE
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        val reader = mapper.readerFor(Map::class.java)
        val writer = mapper.writerFor(Map::class.java)
    }

    @set:DataBoundSetter @JsonIgnore var argfile: String? = null

    // Commonly used flags
    override fun start(context: StepContext): StepExecution = Execution(this, context)

    fun argspec(): String = writer.writeValueAsString(tests)

    @JsonIgnore override fun getDescriptor(): StepDescriptor = Descriptor()

    @Extension class Descriptor : StepDescriptor() {

        override fun getDisplayName(): String = "Run Firebase test"
        override fun getFunctionName(): String = "firebaseTest"
        override fun getRequiredContext(): Set<Class<*>> = setOf(
                FilePath::class.java,
                Launcher::class.java,
                Run::class.java,
                TaskListener::class.java)

        fun doFillTypeItems(): ListBoxModel = ListBoxModel().apply {
            add("Instrumentation test", "instrumentation")
            add("Robo test", "robo")
        }
    }

    class Execution(val step: FirebaseTestStep, context: StepContext) : StepExecution(context) {

        companion object {
            private const val serialVersionUID: Long = 1L
        }

        lateinit var proc: Proc

        override fun start(): Boolean {
            proc = context.launcher().launch()
                    .pwd(context.path())
                    .readStdout()
                    .readStderr()
                    .start()

            return false
        }

        override fun stop(cause: Throwable) {
            proc.kill()
            context.onFailure(cause)
        }
    }
}