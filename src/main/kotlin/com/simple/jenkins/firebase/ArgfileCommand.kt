package com.simple.jenkins.firebase

import hudson.Extension
import hudson.util.FormValidation
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

class ArgfileCommand @DataBoundConstructor constructor(val path: String, val group: String) : Command() {

    override fun args(): String = "'$path:$group'"

    @Extension @Symbol("argfile") class DescriptorImpl : CommandDescriptor() {
        override fun getDisplayName(): String = "Test from arguments file"
    }
}