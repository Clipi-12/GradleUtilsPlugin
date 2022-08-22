package me.clipi.gradle.util

import me.clipi.gradle.plugin.GradleUtilsPlugin
import org.gradle.api.Action
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.*
import org.gradle.jvm.toolchain.internal.DefaultJavaToolchainService
import org.gradle.jvm.toolchain.internal.JavaToolchain

public class JdkSupplier {
    public class Type<T> private constructor(
        private val jdk: (JavaToolchainService, Action<in JavaToolchainSpec>) -> Provider<T>,
        private val downloadedJdk: (JavaToolchain) -> T
    ) {
        public companion object {
            public val LAUNCHER: Type<JavaLauncher> =
                Type(JavaToolchainService::launcherFor, JavaToolchain::getJavaLauncher)
            public val COMPILER: Type<JavaCompiler> =
                Type(JavaToolchainService::compilerFor, JavaToolchain::getJavaCompiler)
            public val JAVADOC_COMPILER: Type<JavadocTool> =
                Type(JavaToolchainService::javadocToolFor, JavaToolchain::getJavadocTool)
        }

        @PublishedApi
        internal fun getJdk(service: JavaToolchainService, config: Action<in JavaToolchainSpec>): T {
            return jdk(service, config).get()
        }

        @PublishedApi
        internal fun getDownloadedJdk(service: JavaToolchain): T {
            return downloadedJdk(service)
        }
    }

    public companion object {
        public fun <T : Any> getExecutable(
            plugin: GradleUtilsPlugin,
            type: Type<T>,
            version: JavaLanguageVersion
        ): T {
            val config: Action<in JavaToolchainSpec> = Action<JavaToolchainSpec> { t -> t.languageVersion.set(version) }

            val service =
                plugin.theProject.extensions.getByType(JavaToolchainService::class.java) as DefaultJavaToolchainService
            var jdk = type.getJdk(service, config)
            if (!(jdk.callPrivateMethod("getMetadata") as JavaToolchain).isJdk) {
                jdk = type.getDownloadedJdk(
                    plugin.getJavaToolchainQueryService().callPrivateMethod(
                        "downloadToolchain",
                        service.callPrivateMethod("configureToolchainSpec", config)
                    ) as JavaToolchain
                )
            }
            return jdk
        }

        public fun <T : Any> getExecutablePath(
            plugin: GradleUtilsPlugin,
            type: Type<T>,
            version: JavaLanguageVersion
        ): String {
            return getExecutable(plugin, type, version).callPrivateMethod("getExecutablePath").toString()
        }
    }
}
