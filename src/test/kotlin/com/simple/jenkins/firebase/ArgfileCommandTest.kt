package com.simple.jenkins.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class ArgfileCommandTest {

    @Test fun command() {
        val cmd: Command = ArgfileCommand("test.yaml", "test-group")
        val res = cmd.args()
        assertEquals("", res)
    }
}