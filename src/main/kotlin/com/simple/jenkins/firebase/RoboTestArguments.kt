package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonUnwrapped
import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class RoboTestArguments(
        app: String,
        @set:DataBoundSetter var appInitialActivity: String? = null,
        @set:DataBoundSetter var maxDepth: Int? = null,
        @set:DataBoundSetter var maxSteps: Int? = null,
        @set:DataBoundSetter var roboDirectives: Map<String, String>? = null)
    : TestArguments(app) {

    val type = "robo"

    @DataBoundConstructor constructor(app: String) : this(app, null)

    @Extension class Descriptor : TestArgumentsDescriptor() {
        override fun getDisplayName(): String = "robo"
    }
}