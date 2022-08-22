package me.clipi.gradle.plugin.extensions

import me.clipi.gradle.DefaultCompilation
import me.clipi.gradle.DefaultSimpleJar
import me.clipi.gradle.SpigotResolver
import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.util.DelegatedMultiProperty
import me.clipi.gradle.util.DelegatedProperty
import me.clipi.gradle.util.NotifierConfigurable
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

@Suppress("SpellCheckingInspection")
public open class GradleUtilsExtension(private val project: Project, private val plugin: GradleUtilsPlugin) :
    NotifierConfigurable<GradleUtilsExtension>(name, {
        it.spigotVersions.orNull
        it.replaceDefaultJarWithSimpleJar.orNull
        it.createDefaultCompilationOptions.orNull
    }) {

    public companion object {
        internal const val name = "gradleUtils"
    }


    public val replaceDefaultJarWithSimpleJar: Property<Boolean>
    public val createDefaultCompilationOptions: Property<Boolean>
    public val spigotVersions: DelegatedMultiProperty<String, SetProperty<String>, Set<String>>
    public var resolveAnnotationProcessorClasspath: Property<Boolean>
    public var addMavenCentralAndGoogleRepos: Property<Boolean>

    init {
        replaceDefaultJarWithSimpleJar = run {
            DelegatedProperty(project.objects.property(Boolean::class.java), this::onSetRDJWSJ)
        }
        createDefaultCompilationOptions = run {
            DelegatedProperty(project.objects.property(Boolean::class.java), this::onSetCDCO)
        }
        spigotVersions = run {
            DelegatedMultiProperty(project.objects.setProperty(String::class.java), this::onSetSV)
        }
        resolveAnnotationProcessorClasspath = project.objects.property(Boolean::class.java)
        addMavenCentralAndGoogleRepos = project.objects.property(Boolean::class.java)

        replaceDefaultJarWithSimpleJar.convention(false)
        createDefaultCompilationOptions.convention(true)
        spigotVersions.convention(setOf())
        resolveAnnotationProcessorClasspath.convention(true)
        addMavenCentralAndGoogleRepos.convention(true)
    }

    private fun onSetCDCO(value: Boolean?) {
        if (value == true) DefaultCompilation.create(plugin)
    }

    private fun onSetRDJWSJ(value: Boolean?) {
        if (value == true) DefaultSimpleJar.create(plugin)
    }

    private fun onSetSV(value: Set<String>?) {
        if (value?.isNotEmpty() == true) SpigotResolver.create(plugin)
    }
}