package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.util.ListBoxModel
import hudson.util.LogTaskListener
import jenkins.model.Jenkins
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import java.io.ByteArrayOutputStream
import java.util.logging.Level
import java.util.logging.Logger

class AndroidDevice @DataBoundConstructor constructor() : AbstractDescribableImpl<AndroidDevice>() {

    @set:DataBoundSetter var model by NullIfBlank()
    @set:DataBoundSetter var version by NullIfBlank()
    @set:DataBoundSetter var orientation by NullIfBlank()
    @set:DataBoundSetter var locale by NullIfBlank()

    @JsonIgnore override fun getDescriptor(): hudson.model.Descriptor<AndroidDevice> = super.getDescriptor()

    @Extension @Symbol("device") class DescriptorImpl : Descriptor<AndroidDevice>() {

        companion object {
            val mapper = ObjectMapper(YAMLFactory()).apply { registerKotlinModule() }
            val deviceReader: ObjectReader = mapper.readerFor(Model::class.java)
            val localeReader: ObjectReader = mapper.readerFor(Locale::class.java)
            val versionReader: ObjectReader = mapper.readerFor(Version::class.java)
        }

        val models: List<Model> by lazy { updateModelInfo() }
        val locales: List<Locale> by lazy { updateLocaleInfo() }
        val versions: List<Version> by lazy { updateVersionInfo() }

        val gcloud: String by lazy { GcloudSdkLocator.locateAny() }

        override fun getDisplayName(): String = "Target device"

        @Suppress("unused")
        fun doFillModelItems(): ListBoxModel = ListBoxModel().apply {
            addAll(models.sortedBy { it.displayName }.map { it.option })
        }

        @Suppress("unused")
        fun doFillVersionItems(@QueryParameter("model") model: String): ListBoxModel = ListBoxModel().apply {
            models.find { m -> m.codename == model }?.supportedVersionIds?.forEach { id ->
                versions.find { it.id == id }?.let { v -> add(v.option) }
            }
        }

        @Suppress("unused")
        fun doFillLocaleItems(): ListBoxModel = ListBoxModel().apply {
            addAll(locales.sortedBy { it.displayName }.map { it.option })
        }

        @Suppress("unused")
        fun doFillOrientationItems(): ListBoxModel = ListBoxModel().apply {
            add("- default -", "")
            add("Portrait", "portrait")
            add("Landscape", "landscape")
        }

        private fun gcloud(args: String): String {
            Jenkins.getInstance().getComputer("")?.node?.let {
                val launcher = it.createLauncher(LogTaskListener(Logger.getGlobal(), Level.INFO))
                val output = ByteArrayOutputStream()
                launcher.launch()
                        .cmdAsSingleString("$gcloud $args")
                        .stdout(output)
                        .join()
                return output.toString()
            }
            return ""
        }

        private fun updateModelInfo(): List<Model> {
            val output = gcloud("firebase test android models list --quiet --format=yaml")
            return deviceReader.readValues<Model>(output).readAll()
        }

        private fun updateLocaleInfo(): List<Locale> {
            val output = gcloud("firebase test android locales list --quiet --format=yaml")
            return localeReader.readValues<Locale>(output).readAll()
        }

        private fun updateVersionInfo(): List<Version> {
            val output = gcloud("firebase test android versions list --quiet --format=yaml")
            return versionReader.readValues<Version>(output).readAll()
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Model(val id: String, val brand: String, val codename: String, val form: String,
                         val manufacturer: String, val name: String, val screenDensity: Int, val screenX: Int,
                         val screenY: Int, val supportedAbis: List<String>, val supportedVersionIds: List<String>,
                         val tags: List<String> = emptyList()) {
            val displayName = "$brand $name ($codename)" + if (tags.contains("default")) { " [default]" } else { "" }
            val option = ListBoxModel.Option(displayName, id, tags.contains("default"))
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Locale(val id: String, val name: String, val region: String?, val tags: List<String> = emptyList()) {
            val displayName = "$id: $name" +
                    if (region != null) { " - $region" } else { "" } +
                    if (tags.contains("default")) { " [default]" } else { "" }
            val option = ListBoxModel.Option(displayName, id, tags.contains("default"))
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Version(val id: String, val versionString: String, val codeName: String, val apiLevel: Int,
                           val tags: List<String> = emptyList()) {
            val displayName = "$id: $versionString $codeName" +
                    if (tags.contains("default")) { " [default]" } else { "" }
            val option = ListBoxModel.Option(displayName, id, tags.contains("default"))
        }
    }
}
