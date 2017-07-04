package com.simple.jenkins.firebase

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.plugins.workflow.steps.StepContext
import java.io.PrintStream

fun StepContext.build(): Run<*,*> = get(Run::class.java)!!
fun StepContext.env(): EnvVars = get(EnvVars::class.java)!!
fun StepContext.launcher(): Launcher = get(Launcher::class.java)!!
fun StepContext.listener(): TaskListener = get(TaskListener::class.java)!!
fun StepContext.logger(): PrintStream = listener().logger
fun StepContext.path(): FilePath = get(FilePath::class.java)!!