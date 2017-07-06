package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.util.ListBoxModel
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

class AndroidDevice @DataBoundConstructor constructor() : AbstractDescribableImpl<AndroidDevice>() {

    @set:DataBoundSetter var model: String? = null
    @set:DataBoundSetter var version: String? = null
    @set:DataBoundSetter var orientation: String? = null
    @set:DataBoundSetter var locale: String? = null

    @JsonIgnore override fun getDescriptor(): hudson.model.Descriptor<AndroidDevice> = super.getDescriptor()

    @Extension @Symbol("device") class DescriptorImpl : Descriptor<AndroidDevice>() {
        override fun getDisplayName(): String = "Target device"

        @Suppress("unused")
        fun doFillOrientationItems(): ListBoxModel = ListBoxModel().apply {
            add("Portrait", "portrait")
            add("Landscape", "landscape")
        }
    }
}
