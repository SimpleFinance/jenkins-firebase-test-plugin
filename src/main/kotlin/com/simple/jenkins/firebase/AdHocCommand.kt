package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import hudson.FilePath
import org.kohsuke.stapler.DataBoundSetter

abstract class AdHocCommand(val app: String) : Command() {

    @set:DataBoundSetter var device: List<AndroidDevice>? = null
    @set:DataBoundSetter var timeout: String? = null
    @set:DataBoundSetter var appPackage: String? = null
    @set:DataBoundSetter var async: Boolean? = null
    @set:DataBoundSetter var autoGoogleLogin: Boolean? = null
    @set:DataBoundSetter @JsonSerialize(using = StringToArray::class) var directoriesToPull: String? = null
    @set:DataBoundSetter @JsonSerialize(using = StringToObject::class) var environmentVariables: String? = null
    @set:DataBoundSetter @JsonSerialize(using = StringToArray::class) var obbFiles: String? = null
    @set:DataBoundSetter var resultsBucket: String? = null
    @set:DataBoundSetter var resultsDir: String? = null
    @set:DataBoundSetter var resultsHistoryName: String? = null

    @JsonIgnore lateinit var argfile: FilePath

    override fun setup(controller: FirebaseTestStep.FirebaseTestController) {
        argfile = controller.argfile
        FirebaseTestStep.mapper.writeValue(argfile.write(), mapOf("adhoc-test" to this))
    }

    override fun args(): String = "${argfile.remote}:adhoc-test"

    abstract class AdHocCommandDescriptor : CommandDescriptor()
}