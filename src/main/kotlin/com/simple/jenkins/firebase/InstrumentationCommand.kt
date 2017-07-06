package com.simple.jenkins.firebase

import hudson.Extension
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class InstrumentationCommand @DataBoundConstructor constructor(app: String, val test: String) : AdHocCommand(app) {

    val type = "instrumentation"

    @set:DataBoundSetter var testPackage: String? = null
    @set:DataBoundSetter var testRunnerClass: String? = null
    @set:DataBoundSetter var testTargets: String? = null

    @Extension @Symbol("instrumentation") class DescriptorImpl : AdHocCommandDescriptor() {
        override fun getDisplayName(): String = "Instrumentation test"
    }
}
