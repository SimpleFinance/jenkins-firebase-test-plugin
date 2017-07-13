package com.simple.jenkins.firebase

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import hudson.remoting.RemoteOutputStream
import hudson.remoting.VirtualChannel
import jenkins.security.Roles
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.remoting.RoleChecker
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption

fun StepContext.build(): Run<*,*> = get(Run::class.java)!!
fun StepContext.env(): EnvVars = get(EnvVars::class.java)!!
fun StepContext.launcher(): Launcher = get(Launcher::class.java)!!
fun StepContext.listener(): TaskListener = get(TaskListener::class.java)!!
fun StepContext.logger(): PrintStream = listener().logger
fun StepContext.path(): FilePath = get(FilePath::class.java)!!

fun ReadableByteChannel.transferTo(dest: WritableByteChannel) {
    val buf = ByteBuffer.allocateDirect(16 * 1024)
    while (read(buf) != -1) {
        buf.flip()
        dest.write(buf)
        buf.compact()
    }
    buf.flip()
    while (buf.hasRemaining()) {
        dest.write(buf)
    }
}

fun FilePath.append(): OutputStream {
    if (isRemote) return act(AppendingFileCallable())

    val f = File(remote).absoluteFile
    f.parentFile?.mkdirs()
    return Files.newOutputStream(f.toPath(), StandardOpenOption.APPEND)
}

internal class AppendingFileCallable : FilePath.FileCallable<OutputStream> {
    companion object {
        private const val serialVersionUID: Long = 1
    }

    override fun checkRoles(checker: RoleChecker) {
        checker.check(this, Roles.MASTER)
    }

    override fun invoke(f: File, channel: VirtualChannel?): OutputStream {
        val af = f.absoluteFile
        af.parentFile?.mkdirs()
        return RemoteOutputStream(Files.newOutputStream(af.toPath(), StandardOpenOption.APPEND))
    }
}
