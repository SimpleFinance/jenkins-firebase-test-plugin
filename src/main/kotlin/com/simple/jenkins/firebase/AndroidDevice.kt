package com.simple.jenkins.firebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import java.io.BufferedReader
import java.io.InputStreamReader

class AndroidDevice @DataBoundConstructor constructor() : AbstractDescribableImpl<AndroidDevice>() {

    @set:DataBoundSetter var model by NullIfBlank()
    @set:DataBoundSetter var version by NullIfBlank()
    @set:DataBoundSetter var orientation by NullIfBlank()
    @set:DataBoundSetter var locale by NullIfBlank()

    @JsonIgnore override fun getDescriptor(): hudson.model.Descriptor<AndroidDevice> = super.getDescriptor()

    @Extension @Symbol("device") class DescriptorImpl : Descriptor<AndroidDevice>() {

        companion object {
            val mapper = ObjectMapper(YAMLFactory()).apply { registerKotlinModule() }
            val deviceReader: ObjectReader = mapper.readerFor(Device::class.java)
            val localeReader: ObjectReader = mapper.readerFor(Locale::class.java)
        }

        val devices: List<Device> by lazy { updateDeviceInfo() }
        val locales: List<Locale> by lazy { updateLocaleInfo() }

        override fun getDisplayName(): String = "Target device"

        @Suppress("unused")
        fun doFillModelItems(): ListBoxModel = ListBoxModel().apply {
            add("", "")
            addAll(devices.sortedBy { it.displayName }.map { it.option })
        }

        @Suppress("unused")
        fun doFillVersionItems(@QueryParameter("model") model: String): ListBoxModel = ListBoxModel().apply {
            devices.find { it.codename == model }?.supportedVersionIds?.forEach { add(it) }
        }

        @Suppress("unused")
        fun doFillLocaleItems(): ListBoxModel = ListBoxModel().apply {
            add("", "")
            addAll(locales.sortedBy { it.displayName }.map { it.option })
        }

        @Suppress("unused")
        fun doFillOrientationItems(): ListBoxModel = ListBoxModel().apply {
            add("", "")
            add("Portrait", "portrait")
            add("Landscape", "landscape")
        }

        @Suppress("unused")
        fun doCheckModel(model: String?): FormValidation = FormValidation.validateRequired(model)

        @Suppress("unused")
        fun doCheckVersion(version: String?): FormValidation = FormValidation.validateRequired(version)

        private fun updateDeviceInfo(): List<Device> {
            val proc = Runtime.getRuntime().exec("gcloud firebase test android models list --quiet --format=yaml")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val output = reader.lineSequence()
                    .dropWhile { it.trim() != "---" }
                    .joinToString("\n")
            val iterator: MappingIterator<Device> = deviceReader.readValues(output)
            return iterator.readAll()
        }

        private fun updateLocaleInfo(): List<Locale> {
            val proc = Runtime.getRuntime().exec("gcloud firebase test android locales list --quiet --format=yaml")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val output = reader.lineSequence()
                    .dropWhile { it.trim() != "---" }
                    .joinToString("\n")
            val iterator: MappingIterator<Locale> = localeReader.readValues(output)
            return iterator.readAll()
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Device(val brand: String, val codename: String, val form: String, val id: String,
                          val manufacturer: String, val name: String, val screenDensity: Int, val screenX: Int,
                          val screenY: Int, val supportedAbis: List<String>, val supportedVersionIds: List<String>) {
            val displayName = "$brand $name ($codename)"
            val option = ListBoxModel.Option(displayName, codename)
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Locale(val id: String, val name: String, val region: String?) {
            val displayName = "$id: $name" + region?.let { " - $it" }
            val option = ListBoxModel.Option(displayName, id)
        }
    }
}
