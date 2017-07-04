package com.simple.jenkins.firebase

import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class InstrumentationCommand @DataBoundConstructor constructor(app: String, val test: String) : AdHocCommand(app) {

    val type = "instrumentation"

    @set:DataBoundSetter var testPackage: String? = null
    @set:DataBoundSetter var testRunnerClass: String? = null
    @set:DataBoundSetter var testTargets: List<String>? = null

    @Extension class DescriptorImpl : AdHocCommandDescriptor() {
        override fun getDisplayName(): String = "Instrumentation test"
    }
}