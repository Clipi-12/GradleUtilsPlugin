package me.clipi.gradle.plugin.extensions

import org.gradle.api.provider.Property

public abstract class ResolveSpigotConfig {
    init {
        run {
            getAllowMultiThreadDownload().convention(true)
            getDeleteTmpDirs().convention(true)
        }
    }

    public abstract fun getAllowMultiThreadDownload(): Property<Boolean>
    public abstract fun getDeleteTmpDirs(): Property<Boolean>
}