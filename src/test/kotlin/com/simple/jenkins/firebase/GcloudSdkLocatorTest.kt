package com.simple.jenkins.firebase

import com.cloudbees.jenkins.plugins.gcloudsdk.GCloudInstallation
import hudson.model.Node
import hudson.model.TaskListener
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import java.io.File

class GcloudSdkLocatorTest {

    @Rule @JvmField val rule = JenkinsRule()

    val node: Node by lazy { rule.createOnlineSlave() }
    val listener: TaskListener by lazy { rule.createTaskListener() }

    @Test fun locatesGcloudToolBinary() {
        val plugin = File(rule.pluginManager.javaClass.getResource("/test-dependencies/gcloud-sdk.hpi").toURI())
        rule.pluginManager.dynamicLoad(plugin)
        assertTrue("gcloud plugin should be installed", GcloudSdkLocator.isGcloudPluginEnabled)

        val installation = GCloudInstallation("gcloud-tool", "/home/gcloud", JenkinsRule.NO_PROPERTIES)
        rule.jenkins.getDescriptorByType(GCloudInstallation.DescriptorImpl::class.java).setInstallations(installation)

        assertEquals("/home/gcloud/bin/gcloud", GcloudSdkLocator.locate(node, listener, "gcloud-tool"))
    }

    @Test fun locatesGcloudDefaultBinary() {
        assertFalse("gcloud plugin should not be installed", GcloudSdkLocator.isGcloudPluginEnabled)
        assertEquals("gcloud", GcloudSdkLocator.locate(node, listener, "gcloud-tool"))
    }
}
