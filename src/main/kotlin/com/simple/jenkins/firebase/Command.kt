package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor

@JsonPropertyOrder(alphabetic = true)
abstract class Command : AbstractDescribableImpl<Command>() {

    abstract fun args(): String

    @JsonIgnore override fun getDescriptor(): Descriptor<Command> = super.getDescriptor()

    abstract class CommandDescriptor : Descriptor<Command>()
}