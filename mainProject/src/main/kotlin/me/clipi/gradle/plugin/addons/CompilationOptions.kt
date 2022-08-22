package me.clipi.gradle.plugin.addons

import groovy.lang.Closure
import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.plugin.extensions.DefaultCompilationOptions
import me.clipi.gradle.util.FileFromStr
import me.clipi.gradle.util.GradleUtil
import me.clipi.gradle.util.JdkSupplier
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.GroovyBuilderScope
import org.gradle.kotlin.dsl.get
import java.io.File

public class CompilationOptions(plugin: GradleUtilsPlugin) : GradleUtilsPlugin.Addon(plugin) {
    private lateinit var compileOnly: DependencySet
    override fun apply() {
        compileOnly = project.configurations.getByName("compileOnly").dependencies
        project.tasks.named("build").get().dependsOn(project.tasks.named("javadoc").get())

        project.afterEvaluate {
            if (config.createDefaultCompilationOptions.orNull != true) return@afterEvaluate
            configureCompilation(project.extensions.getByType(DefaultCompilationOptions::class.java))
        }
    }

    public companion object {
        private fun unzippedLombokPath(project: Project): File {
            return FileFromStr.concat(project.buildDir, "dependencies", "unzipped-lombok")
        }

        private fun delombokPath(project: Project): File {
            return FileFromStr.concat(project.buildDir, "delombok")
        }
    }

    private fun configureCompilation(config: DefaultCompilationOptions) {
        if (config.addSclLombokToClasspath.get()) {
            val unzipLombok = project.tasks.register("unzip_lombok", Copy::class.java) {
                it.includeEmptyDirs = false
                it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
                project.configurations.toTypedArray().forEach innerMost@{ configuration ->
                    val configureC = { c: Configuration ->
                        c.forEach { jarFile ->
                            if (!jarFile.isFile) return@forEach
                            val zipTree = project.zipTree(jarFile).matching { f -> f.include("**/*.SCL.lombok") }
                            it.from(zipTree)
                        }
                    }
                    if (configuration.isCanBeResolved) {
                        configureC(configuration)
                    } else {
                        val tempConfig = project.configurations.create("__doNotUse_tempConfig")
                        tempConfig.extendsFrom(configuration)
                        configureC(tempConfig)
                        project.configurations.remove(tempConfig)
                    }
                }
                it.rename("(.+)\\.SCL\\.lombok$", "$1.class")
                it.into(unzippedLombokPath(project))
            }

            GradleUtil.configure(project, AbstractCompile::class.java) {
                it.dependsOn(unzipLombok)
            }

            GradleUtil.addFirst(project, compileOnly, project.files(unzippedLombokPath(project)))
        }
        if (config.delombokBeforeJavadoc.get()) {
            val javadoc = project.tasks.named("javadoc").get()

            val delombok = project.tasks.register("delombok", Delombok::class.java)

            javadoc.dependsOn(delombok)
        }


        val jdk = config.jdk.orNull
        GradleUtil.configure(project, Javadoc::class.java) {
            GradleUtil.configure(it.options) { options ->
                options.encoding = "UTF-8"
                options.memberLevel = config.javadocMemberLevel.get()
                if (options is CoreJavadocOptions) {
                    if (config.javadocLint.isPresent) options.addBooleanOption(
                        "Xdoclint:${config.javadocLint.get()}",
                        true
                    )
                    if (options is StandardJavadocDocletOptions) {
                        options.tags(
                            "apiNote:a:API Note:",
                            "implSpec:a:Implementation Requirements:",
                            "implNote:a:Implementation Note:"
                        )
                        options.isAuthor = true
                    }
                }
            }
            if (config.delombokBeforeJavadoc.get()) it.setSource(project.file(delombokPath(project)))
            it.isFailOnError = true
            if (jdk != null) it.javadocTool.set(
                JdkSupplier.getExecutable(
                    outerClass,
                    JdkSupplier.Type.JAVADOC_COMPILER,
                    jdk
                )
            )
        }
        val compiler = if (jdk == null) null else JdkSupplier.getExecutable(outerClass, JdkSupplier.Type.COMPILER, jdk)
        GradleUtil.configure(project, JavaCompile::class.java) {
            if (jdk != null) {
                it.options.isFork = true
                it.options.forkOptions.executable = compiler!!.executablePath.asFile.path
            }
            GradleUtil.configure(it.options) { options ->
                options.encoding = "UTF-8"
                options.isDebug = config.includeLinesInJar.get()
                if (config.javacLint.isPresent) options.compilerArgs.add("-Xlint:${config.javacLint.get()}")
            }
        }
        if (jdk != null) {
            val toolsJar =
                FileFromStr.concat(compiler!!.executablePath.asFile.parentFile.parentFile, "lib", "tools.jar")
            if (toolsJar.isFile) compileOnly.add(project.dependencies.create(project.files(toolsJar)))
        }

        GradleUtil.configure(project, AbstractCompile::class.java) {
            val version = config.javaCompatibilityVersion.get().toString()
            it.sourceCompatibility = version
            it.targetCompatibility = version
        }
    }

    public open class Delombok : DefaultTask() {
        private val init = object : Action<Task> {
            override fun execute(_t: Task) {
                val sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
                val antScope = GroovyBuilderScope.of(ant)
                val cp = (sourceSets["main"].runtimeClasspath + sourceSets["main"].compileClasspath).asPath
                GradleUtil.configure(antScope) { invoker ->
                    for (method in arrayOf(
                        "mkdir" to mapOf("dir" to getOutputDir()),
                        "taskdef" to mapOf(
                            "classname" to "lombok.delombok.ant.Tasks\$Delombok",
                            "classpath" to cp,
                            "name" to "delombok"
                        ),
                        "delombok" to arrayOf(
                            mapOf(
                                "verbose" to "true",
                                "encoding" to "UTF-8",
                                "to" to getOutputDir(),
                                "from" to getInputDir(),
                                "classpath" to cp
                            ),
                            object : Closure<Any?>(this, this) {
                                @Suppress("unused")
                                fun doCall() = GroovyBuilderScope.of(delegate)
                                    .invokeMethod("format", mapOf("value" to "suppressWarnings:skip"))
                            }
                        )
                    )) {
                        invoker.invokeMethod(method.first, method.second)
                    }
                }
            }
        }


        init {
            run {
                doLast(init)
            }
        }

        @InputDirectory
        public fun getInputDir(): File {
            return FileFromStr.concat(project.projectDir, "src", "main", "java")
        }

        @OutputDirectory
        public fun getOutputDir(): File {
            return delombokPath(project)
        }
    }
}
