package com.simple.jenkins.firebase

import hudson.Extension
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class RoboCommand @DataBoundConstructor constructor(app: String) : AdHocCommand(app) {

    val type = "robo"

    @set:DataBoundSetter var appInitialActivity: String? = null
    @set:DataBoundSetter var maxDepth: Int? = null
    @set:DataBoundSetter var maxSteps: Int? = null
    @set:DataBoundSetter var roboDirectives: String? = null

    @Extension @Symbol("robo") class DescriptorImpl : AdHocCommandDescriptor() {
        override fun getDisplayName(): String = "Robo test"
    }
}