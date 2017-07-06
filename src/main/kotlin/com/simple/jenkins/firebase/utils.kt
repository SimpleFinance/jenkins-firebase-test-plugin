package com.simple.jenkins.firebase

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NullIfBlank : ReadWriteProperty<Any?, String?> {
    private var value: String? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        this.value = if (value.isNullOrBlank()) null else value
    }
}