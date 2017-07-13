import groovy.lang.GroovyObject
import org.gradle.kotlin.dsl.jenkinsPlugin
import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        // TODO use official 0.23.0 release when that's available
        classpath("org.jenkins-ci.tools:gradle-jpi-plugin:0.23.0-SNAPSHOT")
    }
}

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

apply {
    plugin("org.jenkins-ci.jpi")
}

group = "com.simple.jenkins"
description = "Run Firebase Test Lab tests and publish test results"

val jenkinsVersion: String by extra
val jacksonVersion: String by extra

jenkinsPlugin {
    coreVersion = jenkinsVersion
    displayName = "Firebase Test Plugin"
    url = "https://wiki.jenkins-ci.org/display/JENKINS/Firebase+Test+Plugin"
    gitHubUrl = "https://github.com/simplefinance/firebase-test-plugin"
    fileExtension = "jpi"

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

repositories {
    maven { url = uri("https://repo.jenkins-ci.org/public/") }
}

dependencies {
    kapt("net.java.sezpoz:sezpoz:1.12")

    compile(kotlin("stdlib"))
    compile(kotlin("stdlib-jre8"))
    compile(kotlin("reflect"))

    compile("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    compile("org.jenkins-ci.lib:dry-run-lib:0.1")
    compileOnly("org.jenkins-ci:symbol-annotation:1.3")

    compile("com.google.cloud:google-cloud:0.20.1-alpha")

    "jenkinsPlugins"("org.jenkins-ci.plugins:structs:1.9")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-durable-task-step:2.12")
    "jenkinsPlugins"("org.jenkins-ci.plugins:credentials:2.1.14")

    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-api:2.18")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-step-api:2.12")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-support:2.14")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-cps:2.36")
    "jenkinsPlugins"("org.jenkins-ci.plugins.workflow:workflow-job:2.13")
    "jenkinsPlugins"("org.jenkins-ci.plugins:google-oauth-plugin:0.5")

    "jenkinsTest"("org.jenkins-ci.main:jenkins-test-harness:2.23") { isTransitive = true }
    "jenkinsTest"("org.jenkins-ci.plugins.workflow:workflow-step-api:2.12:tests")
}

kapt {
    correctErrorTypes = true
}