package me.clipi.gradle

import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.plugin.extensions.DefaultCompilationOptions
import me.clipi.gradle.plugin.extensions.DefaultSimpleJarConfig
import me.clipi.gradle.plugin.extensions.ResolveSpigotConfig
import me.clipi.gradle.util.ExtensionCreator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import java.net.URI

public fun RepositoryHandler.clipi() {
    maven {
        it.name = "Clipi"
        it.url = URI("https://clipi-repo.herokuapp.com/")
    }
}

public fun Project.gradleProp(property: String): String = property(property) as String

/**
 * Configures the [defaultCompilationOptions][me.clipi.gradle.plugin.extensions.DefaultCompilationOptions] extension.
 */
public fun Project.defaultCompilationOptions(configure: Action<DefaultCompilationOptions>): Unit =
    (this as ExtensionAware).extensions.configure(DefaultCompilation.name, configure)

internal object DefaultCompilation : ExtensionCreator {

    override val name = "defaultCompilationOptions"

    override fun create(plugin: GradleUtilsPlugin) {
        plugin.theProject.extensions.create(name, DefaultCompilationOptions::class.java)
    }
}

/**
 * Configures the [defaultSimpleJarConfig][me.clipi.gradle.plugin.extensions.DefaultSimpleJarConfig] extension.
 */
public fun Project.defaultSimpleJarConfig(configure: Action<DefaultSimpleJarConfig>): Unit =
    (this as ExtensionAware).extensions.configure(DefaultSimpleJar.name, configure)

internal object DefaultSimpleJar : ExtensionCreator {

    override val name = "defaultSimpleJarConfig"

    override fun create(plugin: GradleUtilsPlugin) {
        plugin.theProject.extensions.create(name, DefaultSimpleJarConfig::class.java, plugin)
    }
}

/**
 * Configures the [spigotResolver][me.clipi.gradle.plugin.extensions.ResolveSpigotConfig] extension.
 */
public fun Project.spigotResolver(configure: Action<ResolveSpigotConfig>): Unit =
    (this as ExtensionAware).extensions.configure(SpigotResolver.name, configure)

internal object SpigotResolver : ExtensionCreator {

    override val name = "spigotResolver"

    override fun create(plugin: GradleUtilsPlugin) {
        plugin.theProject.extensions.create(name, ResolveSpigotConfig::class.java)
    }
}