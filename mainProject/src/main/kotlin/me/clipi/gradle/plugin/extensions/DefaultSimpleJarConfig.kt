package me.clipi.gradle.plugin.extensions

import me.clipi.gradle.SimpleJar
import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.plugin.addons.CreateSimpleJar
import me.clipi.gradle.util.NotifierConfigurable
import org.gradle.api.provider.Property

public abstract class DefaultSimpleJarConfig constructor(plugin: GradleUtilsPlugin) :
    NotifierConfigurable<DefaultSimpleJarConfig>("defaultSimpleJarConfig", {
        plugin.getAddon(CreateSimpleJar::class.java).replaceDefaultJarTask(it)
    }) {

    init {
        run {
            getSources().convention(SimpleJar.defaultSources)
        }
    }

    public abstract fun getSources(): Property<Array<String>>
}