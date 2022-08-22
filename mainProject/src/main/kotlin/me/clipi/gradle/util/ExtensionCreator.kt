package me.clipi.gradle.util

import me.clipi.gradle.plugin.GradleUtilsPlugin

public interface ExtensionCreator {
    public val name: String
    public fun create(plugin: GradleUtilsPlugin)
}