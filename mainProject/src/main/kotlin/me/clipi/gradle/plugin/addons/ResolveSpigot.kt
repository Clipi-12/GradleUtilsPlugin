package me.clipi.gradle.plugin.addons

import me.clipi.gradle.plugin.GradleUtilsPlugin
import me.clipi.gradle.plugin.extensions.ResolveSpigotConfig
import me.clipi.gradle.util.*
import org.gradle.api.artifacts.DependencySet
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.process.ExecResult
import java.io.*
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

public class ResolveSpigot(plugin: GradleUtilsPlugin) : GradleUtilsPlugin.Addon(plugin) {
    public companion object {
        private const val buildTools =
            "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
        private val buildToolsUrl = URL(buildTools)

        private fun noDotVersion(version: String): String {
            return version.replace('.', '_')
        }
    }

    private lateinit var _buildToolsPath: File

    public fun getBuildToolsPath(): File {
        val path = _buildToolsPath
        if (!path.isFile) {
            if (!path.parentFile.isDirectory && !path.parentFile.mkdirs()) throw IOException("Could not download BuildTools.jar")

            buildToolsUrl.openStream().use { path.outputStream().use { os -> it.copyTo(os) } }
        }
        return path
    }


    private fun getTmpPathToVersion(version: String, create: Boolean): File {
        val result = File(getBuildToolsPath().parentFile, "__uniqueDirectoryName_${noDotVersion(version)}")
        if (create) {
            result.deleteRecursively()
            result.mkdirs()
        }
        return result
    }

    private lateinit var mavenLocal: File
    private lateinit var compileOnlyDep: DependencySet

    override fun apply() {
        _buildToolsPath = FileFromStr.concat(project.buildDir, "tmp", "BuildToolsTmpDir", "BuildTools.jar")

        mavenLocal = File(project.repositories.mavenLocal().url)
        compileOnlyDep = project.configurations.getByName("compileOnly").dependencies
    }

    override fun beforeResolution() {
        val spigotVersions = config.spigotVersions.get()
        if (spigotVersions.isEmpty()) return
        val config = project.extensions.getByType(ResolveSpigotConfig::class.java)

        val queue = mutableListOf<Callable<Any?>>()
        val successes = Vector<File>()
        val failures = Vector<Failure>()

        for (version in spigotVersions) {
            val craftBukkit = SpigotURLs.craftBukkitName(version)
            val spigotDir = FileFromStr.concat(mavenLocal, "org", "spigotmc", "spigot", craftBukkit)
            if (spigotDir.isDirectory) {
                println("Skipping the download of Spigot $version as it is already in the MavenLocal repository")
            } else {
                getBuildToolsPath()
                println("Queueing the download of Spigot $version")
                queue.add(Download(version, successes, failures))
            }
        }

        if (queue.isEmpty()) {
            println("All spigot dependencies were resolved and don\'t need to be downloaded")
        } else {
            println("Waiting until all versions of Spigot are successfully downloaded")

            val submitter = if (config.getAllowMultiThreadDownload().get())
                Executors.newFixedThreadPool(queue.size) else
                Executors.newSingleThreadExecutor()
            val futures = submitter.invokeAll(queue)
            submitter.shutdown()
            futures.forEach { f -> f.get() }
            print("All queued downloads of Spigot have finished")

            if (failures.isEmpty()) {
                println(" successfully")
                if (config.getDeleteTmpDirs().get()) _buildToolsPath.parentFile.deleteRecursively()
            } else {
                println()
                System.err.println("All queued downloads of Spigot have finished")
                System.err.println("Some of the downloads threw exceptions")
                System.err.println("You may visit https://www.spigotmc.org/wiki/buildtools/#issues-and-common-concerns")
                if (config.getDeleteTmpDirs().get()) successes.forEach(File::deleteRecursively)
                val path = File(getTmpPathToVersion("\$version$", false), "BuildTools.log.txt").absolutePath
                System.err.println("You may view the logs of those downloads under $path")
                failures.forEach(Failure::rethrowIt)
                failures.forEach(Failure::stopExec)
            }
        }

        for (version in spigotVersions) {
            val craftBukkit = SpigotURLs.craftBukkitName(version)
            val customDep = project.configurations.create("__doNotUse_SpigotVer_${noDotVersion(version)}")
            customDep.dependencies.add(project.dependencies.create("org.spigotmc:spigot:${craftBukkit}"))
            compileOnlyDep.addLater(project.provider { project.dependencies.create(project.files(customDep)) })
        }
    }

    private inner class Download(
        private val version: String,
        private val successes: Vector<File>,
        private val failures: Vector<Failure>
    ) : Callable<Any?> {

        override fun call(): Any? {
            run()
            return null
        }

        private fun run() {
            println("Starting the download of Spigot $version")
            val workingDir = getTmpPathToVersion(version, true)
            val stderr = SavedOS()
            val shouldStopExec: (ExecResult) -> Boolean = lambda@{ result: ExecResult ->
                if (result.exitValue == 0) return@lambda false
                System.err.println("The download of $version threw an exception. Rethrowing it when every download has finished")
                failures.add(Failure(result, stderr))
                return@lambda true
            }
            val buildToolsResult = project.javaexec {
                it.workingDir = workingDir
                it.mainClass.set("-jar")
                it.args = mutableListOf(
                    getBuildToolsPath().path,
                    "--rev",
                    version,
                    "--generate-source",
                    "--generate-docs"
                )
                it.executable = JdkSupplier.getExecutablePath(
                    outerClass,
                    JdkSupplier.Type.LAUNCHER,
                    JavaLanguageVersion.of(SpigotURLs.version(version))
                )
                it.isIgnoreExitValue = true
                it.errorOutput = stderr
            }
            if (shouldStopExec(buildToolsResult)) return

            val craftBukkit = SpigotURLs.craftBukkitName(version)
            val exportClassifiers = { classifier: String, newArtifactName: String ->
                project.exec {
                    it.executable = FileFromStr.concat(workingDir.listFiles(FilenameFilter { dir, name ->
                        if (name == null || dir == null) return@FilenameFilter false
                        name.contains("maven") && File(dir, name).isDirectory
                    })?.get(0) ?: throw IOException(), "bin", "mvn${GradleUtil.getOsMavenExecutableExtension()}").path
                    it.workingDir = workingDir
                    it.args = mutableListOf(
                        "install:install-file",
                        "-Dfile=${
                            FileFromStr.concat(
                                workingDir,
                                "Spigot",
                                "Spigot-API",
                                "target",
                                "spigot-api-${craftBukkit}-${classifier}.jar"
                            )
                        }",
                        "-DgroupId=org.spigotmc",
                        "-DartifactId=${newArtifactName}",
                        "-Dversion=${craftBukkit}",
                        "-Dpackaging=jar",
                        "-Dclassifier=${classifier}"
                    )
                    it.isIgnoreExitValue = true
                    it.errorOutput = stderr
                }
            }

            if (shouldStopExec(exportClassifiers("sources", "spigot-api"))) return
            if (shouldStopExec(exportClassifiers("javadoc", "spigot-api"))) return
            if (shouldStopExec(exportClassifiers("sources", "spigot"))) return
            if (shouldStopExec(exportClassifiers("javadoc", "spigot"))) return

            successes.add(workingDir)
        }
    }

    private class Failure(private val result: ExecResult, private val stderr: SavedOS) {

        fun rethrowIt() {
            result.rethrowFailure()
        }

        fun stopExec() {
            stderr.throwIt()
        }
    }

    private class SavedOS : BufferedOutputStream(System.err) {
        private val baos = ByteArrayOutputStream(8192)

        @Synchronized
        override fun write(b: Int) {
            super.write(b)
            baos.write(b)
        }

        @Synchronized
        override fun write(b: ByteArray, off: Int, len: Int) {
            super.write(b, off, len)
            baos.write(b, off, len)
        }

        fun throwIt() {
            super.write(baos.toByteArray())
            super.flush()
            StopExecution.cause(baos.toString())
        }
    }
}
