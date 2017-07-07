package com.simple.jenkins.firebase

import org.jenkinsci.plugins.workflow.steps.StepConfigTester
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class FirebaseTestStepTest {

    @Rule @JvmField val rule = JenkinsRule()

    val stepConfigTester = StepConfigTester(rule)

    @Test fun configuresArgfileArguments() {
        val step = FirebaseTestStep(ArgfileCommand("argfile", "group"))
        stepConfigTester.configRoundTrip(step)
    }

    @Test fun configuresRoboArguments() {
        val step = FirebaseTestStep(RoboCommand("app.apk").apply {
            appInitialActivity = "com.test.app.SomeActivity"
            maxDepth = 5
            maxSteps = 20
            roboDirectives = "username_resource=username,password_resource=password"
            configCommon()
        })
        stepConfigTester.configRoundTrip(step)
    }

    @Test fun configuresInstrumentationArguments() {
        val step = FirebaseTestStep(InstrumentationCommand("app.apk", "test.apk").apply {
            testPackage = "com.test.app.test"
            testRunnerClass = "com.test.app.test.TestRunner"
            testTargets = "com.test.app.test.Foo,com.test.app.test.Bar"
            configCommon()
        })
        stepConfigTester.configRoundTrip(step)
    }

    fun AdHocCommand.configCommon(): AdHocCommand = apply {
        device = listOf(AndroidDevice().apply {
            model = "model"
            version = "21"
            orientation = "portrait"
            locale = "en_US"
        })
        timeout = "20s"
        appPackage = "com.test.app"
        async = false
        autoGoogleLogin = true
        directoriesToPull = "/directory-to-pull,/other-directory"
        environmentVariables = "FOO=foo,BAR=bar"
        obbFiles="/path/to/obb1,/path/to/obb2"
        resultsBucket = "some-bucket"
        resultsDir = "/results/dir"
        resultsHistoryName = "some history name"
    }
}