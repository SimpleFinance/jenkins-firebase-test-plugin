package com.simple.jenkins.firebase

import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor

class ArgfileCommand @DataBoundConstructor constructor(val argfile: String, val groupName: String) : Command() {

    override fun args(): String = "'$argfile:$groupName'"

    @Extension class DescriptorImpl : CommandDescriptor() {
        override fun getDisplayName(): String = "Test from arguments file"
    }
}