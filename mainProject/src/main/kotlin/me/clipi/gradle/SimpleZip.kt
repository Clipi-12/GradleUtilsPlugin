package me.clipi.gradle

import groovy.lang.Closure
import me.clipi.gradle.util.FileFromStr
import me.clipi.gradle.util.StopExecution
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Zip
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.nio.file.Files

@Suppress("unused")
@DisableCachingByDefault(because = "Not worth caching")
public abstract class SimpleZip : Zip() {
    @Internal
    public abstract fun getSources(): Property<Array<String>>

    private var _sourcesAsFiles: Array<File>? = null

    @Input
    public fun getSourcesAsFiles(): Array<File> {
        if (_sourcesAsFiles == null) {
            val files = mutableListOf<File>()
            for (source in getSources().get()) {
                files.addAll(FileFromStr.dirs(source, project))
            }
            _sourcesAsFiles = files.toTypedArray()
        }
        return _sourcesAsFiles!!
    }

    init {
        run {
            setMetadataCharset("UTF-8")
            from(objectFactory.fileCollection().from(project.provider(this::getSourcesAsFiles)))
        }
    }


    override fun copy() {
        val out = archiveFile.get().asFile

        if (out.exists()) Files.delete(out.toPath())
        destinationDirectory.get().asFile.mkdirs()
        if (!out.createNewFile()) StopExecution.cause("Could not create the file $out")

        super.copy()
    }

    public fun rename(renamer: Renamer): SimpleZip {
        mainSpec.eachFile(RenamingAction(renamer))
        return this
    }

    public fun rename(renamer: (String, String) -> String?): SimpleZip {
        mainSpec.eachFile(RenamingAction(renamer))
        return this
    }

    override fun rename(renamer: Closure<*>): SimpleZip {
        if (renamer.maximumNumberOfParameters == 2) {
            mainSpec.eachFile(RenamingAction { fullName: String, fileName: String ->
                renamer.call(fullName, fileName)?.toString()
            })
            return this
        }
        super.rename(renamer)
        return this
    }

    public fun relocate(relocator: Relocator): SimpleZip {
        mainSpec.eachFile(RelocatingAction(relocator))
        return this
    }

    public fun relocate(relocator: (Array<String>) -> Array<String>?): SimpleZip {
        mainSpec.eachFile(RelocatingAction(relocator))
        return this
    }

    public fun relocate(relocator: Closure<Array<String>?>): SimpleZip {
        mainSpec.eachFile(RelocatingAction(relocator))
        return this
    }
}
