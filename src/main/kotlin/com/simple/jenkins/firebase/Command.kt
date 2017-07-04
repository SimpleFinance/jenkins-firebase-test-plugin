package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import hudson.FilePath
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor

abstract class Command : AbstractDescribableImpl<Command>() {

    open fun setup(path: FilePath) {
        // Pass
    }

    abstract fun args(): String

    @JsonIgnore override fun getDescriptor(): Descriptor<Command> = super.getDescriptor()

    abstract class CommandDescriptor : Descriptor<Command>()
}