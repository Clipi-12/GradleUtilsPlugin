package me.clipi.gradle.plugin.addons

import me.clipi.gradle.plugin.GradleUtilsPlugin

public class AddMavenCentralAndGoogleRepos(plugin: GradleUtilsPlugin) : GradleUtilsPlugin.Addon(plugin) {
    override fun apply() {
        project.repositories.mavenCentral()
        project.repositories.google()
    }
}
