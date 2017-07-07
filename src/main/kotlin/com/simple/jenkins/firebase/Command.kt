package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor

@JsonPropertyOrder(alphabetic = true)
abstract class Command : AbstractDescribableImpl<Command>() {

    open fun setup(controller: FirebaseTestStep.FirebaseTestController) {}

    abstract fun args(): String

    @JsonIgnore override fun getDescriptor(): Descriptor<Command> = super.getDescriptor()

    abstract class CommandDescriptor : Descriptor<Command>()
}