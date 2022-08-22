package me.clipi.gradle.plugin.addons

import me.clipi.gradle.SimpleJar
import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.plugin.extensions.DefaultSimpleJarConfig

public class CreateSimpleJar(plugin: GradleUtilsPlugin) : GradleUtilsPlugin.Addon(plugin) {
    override fun apply() {
        project.afterEvaluate {
            if (config.replaceDefaultJarWithSimpleJar.orNull != true) return@afterEvaluate

            replaceDefaultJarTask(project.extensions.getByType(DefaultSimpleJarConfig::class.java))
        }
    }

    private var replaced = false
    internal fun replaceDefaultJarTask(jarConfig: DefaultSimpleJarConfig) {
        if (replaced) return
        replaced = true

        val jar = project.tasks.named("jar").get()
        val actionsOfPrevJar = jar.actions.toMutableList()
        // let's hope that if some plugin modifies the actions it doesn't use a doFirst() and that we can simply attach it to our doLast()
        actionsOfPrevJar.removeAt(0)
        jar.actions = mutableListOf()
        jar.dependsOn(project.tasks.register("simpleJar", SimpleJar::class.java) { simpleJar ->
            val classes = project.tasks.named("classes")
            simpleJar.dependsOn(classes)
            simpleJar.getSources().set(jarConfig.getSources().get())
            actionsOfPrevJar.forEach(simpleJar::doLast)
        })
    }
}
