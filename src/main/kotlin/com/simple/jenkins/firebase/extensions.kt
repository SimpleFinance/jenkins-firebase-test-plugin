package com.simple.jenkins.firebase

import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.plugins.workflow.steps.StepContext

fun StepContext.build(): Run<*,*> = get(Run::class.java)!!
fun StepContext.launcher(): Launcher = get(Launcher::class.java)!!
fun StepContext.listener(): TaskListener = get(TaskListener::class.java)!!
fun StepContext.path(): FilePath = get(FilePath::class.java)!!