package me.clipi.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePluginExtension

public abstract class SimpleJar : SimpleZip() {
    public companion object {
        public val defaultSources: Array<String> = arrayOf("build/classes/*/main", "src/main/resources")
    }

    private val extension: BasePluginExtension

    init {
        archiveExtension.convention("jar")
        archiveExtension.set("jar")

        extension = project.extensions.getByType(BasePluginExtension::class.java)
        destinationDirectory.set(extension.libsDirectory)

        run {
            getSources().convention(defaultSources)
        }
    }

    override fun getDestinationDirectory(): DirectoryProperty {
        val result = super.getDestinationDirectory()
        result.convention(extension.libsDirectory)
        return result
    }
}
