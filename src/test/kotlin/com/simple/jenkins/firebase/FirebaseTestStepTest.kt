package com.simple.jenkins.firebase

import org.jenkinsci.plugins.workflow.steps.StepConfigTester
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class FirebaseTestStepTest {

    @Rule @JvmField val rule = JenkinsRule()

    @Test fun writesBasicArgspec() {
        val step = FirebaseTestStep(linkedMapOf("basic-test" to RoboTestArguments("app.apk")))
        val spec = step.argspec()
        assertEquals("""
basic-test:
  app: "app.apk"
  type: "robo"
        """.trim(), spec.trim())
    }

    @Test fun writesMultipleArgspecGroups() {
        val step = FirebaseTestStep(linkedMapOf(
                "basic-test" to RoboTestArguments("app.apk"),
                "another-test" to RoboTestArguments("app.apk")))
        val spec = step.argspec()
        assertEquals("""
basic-test:
  app: "app.apk"
  type: "robo"
another-test:
  app: "app.apk"
  type: "robo"
        """.trim(), spec.trim())
    }

    @Test fun configuresNoArguments() {
        StepConfigTester(rule).configRoundTrip(FirebaseTestStep(emptyMap()))
    }

    @Test fun configuresRoboArguments() {

    }

    @Test fun configuresInstrumentationArguments() {

    }

}