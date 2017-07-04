package com.simple.jenkins.firebase

import org.junit.Test
import org.junit.Assert.*

class InstrumentationCommandTest {

    @Test fun basic() {
        val args = InstrumentationCommand("app.apk", "test.apk").args()
        assertEquals("", args)
    }
}