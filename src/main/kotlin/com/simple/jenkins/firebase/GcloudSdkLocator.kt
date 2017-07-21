package com.simple.jenkins.firebase

import com.cloudbees.jenkins.plugins.gcloudsdk.GCloudInstallation
import hudson.EnvVars
import hudson.model.Node
import hudson.model.TaskListener
import hudson.tools.ToolInstallation
import hudson.util.LogTaskListener
import jenkins.model.Jenkins
import java.util.logging.Level
import java.util.logging.Logger

object GcloudSdkLocator {

    const val default = "gcloud"

    val isGcloudPluginEnabled: Boolean
            get() = Jenkins.getInstance().getPlugin("gcloud-sdk")?.wrapper?.isEnabled ?: false

    fun locate(node: Node, log: TaskListener, toolId: String? = null): String {
        val install = getInstallations().find { toolId == null || it.name == toolId } ?: return default
        return (install as GCloudInstallation).forNode(node, log).home + "/bin/gcloud"
    }

    fun locate(env: EnvVars, toolId: String? = null): String {
        val install = getInstallations().find { toolId == null || it.name == toolId } ?: return default
        return (install as GCloudInstallation).forEnvironment(env).home + "/bin/gcloud"
    }

    fun locateAny(): String {
        val install = getInstallations().firstOrNull() ?: return default
        with (install as GCloudInstallation) {
            val jenkins = Jenkins.getInstance()
            val node = jenkins.getComputer("")?.node ?: return default
            val log = LogTaskListener(Logger.getGlobal(), Level.INFO)
            return install.forNode(node, log).home + "/bin/gcloud"
        }
    }

    private fun getInstallations(): List<ToolInstallation> =
        if (isGcloudPluginEnabled) {
            GCloudInstallation.getInstallations().map { it as ToolInstallation }
        } else {
            listOf<ToolInstallation>()
        }
}