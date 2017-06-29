import groovy.lang.GroovyObject
import org.gradle.kotlin.dsl.jenkinsPlugin
import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense

buildscript {
    repositories {
        jcenter()
        maven { url = uri("http://repo.jenkins-ci.org/releases/") }
        mavenLocal()
    }
    dependencies {
        // TODO use official 0.23.0 release when that's available
        classpath("org.jenkins-ci.tools:gradle-jpi-plugin:0.23.0-SNAPSHOT")
    }
}

plugins {
    kotlin("jvm")
}

apply {
    plugin("org.jenkins-ci.jpi")
}

group = "com.simple.jenkins"
description = "Run Firebase Test Lab tests and publish test results"

val jenkinsVersion: String by extra
val jacksonVersion: String by extra

configurations.all { isTransitive = true }

dependencies {
    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))

    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    compile("org.jenkins-ci.lib:dry-run-lib:0.1")
    compileOnly("org.jenkins-ci:symbol-annotation:1.3")

    "jenkinsPlugins"("org.jenkins-ci.plugins:credentials:2.1.14")
    "jenkinsPlugins"("org.jenkins-ci.plugins:structs:1.9")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-step-api:2.11")
    "jenkinsPlugins"("org.jenkins-ci.plugins:google-oauth-plugin:0.5")

    "jenkinsTest"("org.jenkins-ci.main:jenkins-test-harness:2.23")
    "jenkinsTest"("org.jenkins-ci.plugins:scm-api:2.0.7")
    "jenkinsTest"("org.jenkins-ci.plugins:script-security:1.26")
    "jenkinsTest"("org.jenkins-ci.plugins:durable-task:1.13")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-api:2.17")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-cps:2.36")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-job:2.13")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-step-api:2.11:tests")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-basic-steps:2.5")
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-scm-step:2.4")
}

jenkinsPlugin {
    coreVersion = jenkinsVersion
    displayName = "Firebase Test Plugin"
    url = "https://wiki.jenkins-ci.org/display/JENKINS/Firebase+Test+Plugin"
    gitHubUrl = "https://github.com/simplefinance/firebase-test-plugin"

    developers(delegateClosureOf<GroovyObject> {
        setProperty("tadfisher", delegateClosureOf<JpiDeveloper> {
            setProperty("name", "Tad Fisher")
            setProperty("email", "tad@simple.com")
        })
    })

    licenses(delegateClosureOf<GroovyObject> {
        setProperty("Apache License, Version 2.0", delegateClosureOf<JpiLicense> {
            setProperty("url", "https://www.apache.org/licenses/LICENSE-2.0.txt")
        })
    })
}
