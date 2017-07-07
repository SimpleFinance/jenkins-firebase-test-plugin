package com.simple.jenkins.firebase

import com.fasterxml.jackson.databind.annotation.JsonSerialize
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

    abstract class AdHocCommandDescriptor : CommandDescriptor()

    override fun args(): String {
        val argspec = FirebaseTestStep.mapper.writeValueAsString(mapOf("adhoc-test" to this))
        return "<(echo '$argspec'):adhoc-test"
    }
}