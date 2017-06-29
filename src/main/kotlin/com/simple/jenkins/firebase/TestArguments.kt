package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonUnwrapped
import hudson.model.AbstractDescribableImpl
import hudson.model.Describable
import hudson.model.Descriptor
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

abstract class TestArguments @DataBoundConstructor constructor(val app: String)
    : AbstractDescribableImpl<TestArguments>() {

    @set:DataBoundSetter var device: List<AndroidDevice>? = null
    @set:DataBoundSetter var timeout: String? = null
    @set:DataBoundSetter var appPackage: String? = null
    @set:DataBoundSetter var async: Boolean? = null
    @set:DataBoundSetter var autoGoogleLogin: Boolean? = null
    @set:DataBoundSetter var directoriesToPull: List<String>? = null
    @set:DataBoundSetter var environmentVariables: Map<String, String>? = null
    @set:DataBoundSetter var obbFiles: List<String>? = null
    @set:DataBoundSetter var resultsBucket: String? = null
    @set:DataBoundSetter var resultsDir: String? = null
    @set:DataBoundSetter var resultsHistoryName: String? = null

    @JsonIgnore override fun getDescriptor(): Descriptor<TestArguments> = super.getDescriptor()

    abstract class TestArgumentsDescriptor : Descriptor<TestArguments>()
}