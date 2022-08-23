package me.clipi.gradle.plugin

import me.clipi.gradle.plugin.addons.*
import me.clipi.gradle.plugin.extensions.GradleUtilsExtension
import me.clipi.gradle.util.GradleUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.internal.JavaToolchainQueryService
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.util.function.Function
import javax.inject.Inject
import kotlin.collections.set

@Suppress("MemberVisibilityCanBePrivate")
public open class GradleUtilsPlugin : Plugin<Project> {
    @Inject
    @PublishedApi
    internal open fun getJavaToolchainQueryService(): JavaToolchainQueryService {
        throw UnsupportedOperationException()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <A : Addon> getAddon(addon: Class<A>): A = addons[addon] as A
    private val addons = HashMap<Class<out Addon>, Addon>()

    protected lateinit var extension: GradleUtilsExtension
    protected lateinit var project: Project

    @PublishedApi
    internal lateinit var theProject: Project
    internal var insideBeforeResolution = false

    override fun apply(project: Project) {
        theProject = project
        this.project = project

        project.pluginManager.apply("me.clipi.gradle.conventions.mavenlocal")
        project.pluginManager.apply(JavaPlugin::class.java)

        project.pluginManager.apply(IdeaPlugin::class.java)
        project.pluginManager.apply(EclipsePlugin::class.java)
        project.extensions.getByType(IdeaModel::class.java).module {
            it.isDownloadSources = true
            it.isDownloadJavadoc = true
        }
        project.extensions.getByType(EclipseModel::class.java).classpath {
            it.isDownloadSources = true
            it.isDownloadJavadoc = true
        }


        extension = project.extensions.create(
            GradleUtilsExtension::class.java,
            GradleUtilsExtension.name,
            GradleUtilsExtension::class.java,
            project,
            this
        )
        addon(::ResolveSpigot)
        addon(::CreateSimpleJar)
        addon(::CompilationOptions)
        GradleUtil.beforeResolution(project) {
            insideBeforeResolution = true
            addon(::ResolveAnnotationProcessorClasspath, extension.resolveAnnotationProcessorClasspath)
            addon(::AddMavenCentralAndGoogleRepos, extension.addMavenCentralAndGoogleRepos)
        }
    }

    protected fun addon(addon: Function<GradleUtilsPlugin, out Addon>, condition: Property<Boolean>): Unit =
        addon(addon, condition.get())

    protected fun addon(addon: Function<GradleUtilsPlugin, out Addon>, condition: Boolean) {
        if (condition) addon(addon)
    }

    protected fun addon(addon: Function<GradleUtilsPlugin, out Addon>) {
        val createdAddon = addon.apply(this)
        addons[createdAddon::class.java] = createdAddon
    }

    /**
     * Cannot be done with an inner class because of [KT-9860](https://youtrack.jetbrains.com/issue/KT-9860/)
     */
    public abstract class Addon(protected val outerClass: GradleUtilsPlugin) {
        protected val project: Project = outerClass.theProject
        protected val config: GradleUtilsExtension = outerClass.extension

        init {
            run {
                apply()
                if (outerClass.insideBeforeResolution) {
                    beforeResolution()
                } else {
                    GradleUtil.beforeResolution(project, this::beforeResolution)
                }
            }
        }

        protected open fun apply() {
        }

        protected open fun beforeResolution() {
        }
    }
}
