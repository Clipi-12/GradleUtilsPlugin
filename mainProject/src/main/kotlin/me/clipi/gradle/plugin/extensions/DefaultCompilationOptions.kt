package me.clipi.gradle.plugin.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.jvm.toolchain.JavaLanguageVersion

public abstract class DefaultCompilationOptions {
    init {
        run {
            addSclLombokToClasspath.convention(false)
            delombokBeforeJavadoc.convention(false)
            includeLinesInJar.convention(true)
            jdk.set(JavaLanguageVersion.of(8))
            javaCompatibilityVersion.convention(JavaVersion.VERSION_1_8)
            javacLint.set("all")
            javadocLint.set("all,-reference")
            javadocMemberLevel.convention(JavadocMemberLevel.PROTECTED)
        }
    }

    public abstract val addSclLombokToClasspath: Property<Boolean>
    public abstract val delombokBeforeJavadoc: Property<Boolean>
    public abstract val includeLinesInJar: Property<Boolean>
    public abstract val jdk: Property<JavaLanguageVersion>
    public abstract val javaCompatibilityVersion: Property<JavaVersion>
    public abstract val javacLint: Property<String>
    public abstract val javadocLint: Property<String>
    public abstract val javadocMemberLevel: Property<JavadocMemberLevel>
}