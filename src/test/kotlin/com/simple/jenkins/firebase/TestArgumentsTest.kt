package com.simple.jenkins.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class TestArgumentsTest {

    @Test fun writesArgfile() {
        val cmd: Command = ArgfileCommand("test.yaml", "test-group")
        val res = cmd.args()
        assertEquals("'test.yaml:test-group'", res)
    }

    @Test fun writesRobo() {
        val cmd: AdHocCommand = RoboCommand(app = "app.apk").apply {
            appInitialActivity = "com.foo.bar/BazActivity"
            maxDepth = 5
            roboDirectives = "username_resource=username,password_resource=password"
        }
        val result = FirebaseTestStep.mapper.writeValueAsString(cmd)
        assertEquals("""
            app: "app.apk"
            app-initial-activity: "com.foo.bar/BazActivity"
            max-depth: 5
            robo-directives:
              username_resource: "username"
              password_resource: "password"
            type: "robo"
        """.trimIndent(), result.trim())
    }

    @Test fun writesBasicInstrumentation() {
        val args: AdHocCommand = InstrumentationCommand(app = "app.apk", test = "test.apk").apply {
            testPackage = "com.foo.app"
            testTargets = "foo,bar,baz"
        }
        val result = FirebaseTestStep.mapper.writeValueAsString(args)
        assertEquals("""
            app: "app.apk"
            test: "test.apk"
            test-package: "com.foo.app"
            test-targets:
            - "foo"
            - "bar"
            - "baz"
            type: "instrumentation"
        """.trimIndent(), result.trim())
    }

    @Test fun writesDeviceList() {
        val args: AdHocCommand = InstrumentationCommand(app = "app.apk", test = "test.apk").apply {
            device = listOf(
                    AndroidDevice().apply { model = "flo" },
                    AndroidDevice().apply { model = "g3"; version = "19"; locale = "zh" },
                    AndroidDevice().apply { model = "mako"; version = "21" })
        }
        val result = FirebaseTestStep.mapper.writeValueAsString(args)
        assertEquals("""
            app: "app.apk"
            test: "test.apk"
            device:
            - model: "flo"
            - model: "g3"
              version: "19"
              locale: "zh"
            - model: "mako"
              version: "21"
            type: "instrumentation"
        """.trimIndent(), result.trim())
    }

    @Test fun writesEnvVars() {
        val args: AdHocCommand = RoboCommand(app = "app.apk").apply {
            environmentVariables = "FOO=foo,BAR=bar"
        }
        val result = FirebaseTestStep.mapper.writeValueAsString(args)
        assertEquals("""
            app: "app.apk"
            environment-variables:
              FOO: "foo"
              BAR: "bar"
            type: "robo"
        """.trimIndent(), result.trim())
    }
}