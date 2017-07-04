package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.Descriptor
import hudson.model.TaskListener
import jenkins.model.Jenkins
import org.jenkinsci.plugins.durabletask.BourneShellScript
import org.jenkinsci.plugins.durabletask.Controller
import org.jenkinsci.plugins.durabletask.DurableTask
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep
import org.kohsuke.stapler.DataBoundConstructor

class FirebaseTestStep @DataBoundConstructor constructor(val command: String) : DurableTaskStep() {

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

    override fun task(): DurableTask = FirebaseTestTask("gcloud firebase test android run $command")

    @Extension class FirebaseTestStepDescriptor : DurableTaskStepDescriptor() {

        override fun getDisplayName(): String = "Run Firebase test"
        override fun getFunctionName(): String = "firebaseTest"

        fun getCommandDescriptors(): List<Descriptor<Command>> =
                Jenkins.getInstance().getDescriptorList(Command::class.java)
    }

    class FirebaseTestTask(val command: String) : DurableTask() {
        override fun launch(env: EnvVars, workspace: FilePath, launcher: Launcher, listener: TaskListener): Controller {
            return BourneShellScript(command).apply { captureOutput() }.launch(env, workspace, launcher, listener)
        }
    }
}