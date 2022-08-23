package me.clipi.gradle.plugin.addons

import me.clipi.gradle.plugin.GradleUtilsPlugin

public class ResolveAnnotationProcessorClasspath(plugin: GradleUtilsPlugin) : GradleUtilsPlugin.Addon(plugin) {
    override fun apply() {
        // Include "compileOnly" and "implementation" dependencies as classpath when using an annotation processor
        val annotationProcessor = config("annotationProcessor")
        val testAnnotationProcessor = config("testAnnotationProcessor")
        val compileOnly = config("compileOnly")
        val implementation = config("implementation")
        val testCompileOnly = config("testCompileOnly")
        val testImplementation = config("testImplementation")
        annotationProcessor.extendsFrom(implementation, compileOnly)
        testAnnotationProcessor.extendsFrom(testImplementation, testCompileOnly)
    }

    private fun config(name: String) = project.configurations.named(name).get()
}
