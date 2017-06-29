package com.simple.jenkins.firebase

import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class InstrumentationTestArguments
@DataBoundConstructor constructor(app: String, val test: String) : TestArguments(app) {

    @set:DataBoundSetter var testPackage: String? = null
    @set:DataBoundSetter var testRunnerClass: String? = null
    @set:DataBoundSetter var testTargets: List<String>? = null

    val type = "instrumentation"

    @Extension class Descriptor : TestArgumentsDescriptor() {
        override fun getDisplayName(): String = "Instrumentation test"
    }
}