package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.util.ListBoxModel
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import java.awt.ComponentOrientation

class AndroidDevice(
        @set:DataBoundSetter var model: String? = null,
        @set:DataBoundSetter var version: String? = null,
        @set:DataBoundSetter var orientation: String? = null,
        @set:DataBoundSetter var locale: String? = null)
 : AbstractDescribableImpl<AndroidDevice>() {

    @DataBoundConstructor constructor() : this(null)

    @JsonIgnore override fun getDescriptor(): hudson.model.Descriptor<AndroidDevice> = super.getDescriptor()

    @Extension class Descriptor : hudson.model.Descriptor<AndroidDevice>() {
        override fun getDisplayName(): String = "Target device"

        fun doFillOrientationItems(): ListBoxModel = ListBoxModel().apply {
            add("Portrait", "portrait")
            add("Landscape", "landscape")
        }
    }
}
