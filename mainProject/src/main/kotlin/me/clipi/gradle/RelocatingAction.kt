package me.clipi.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.RelativePath

@Suppress("unused")
public class RelocatingAction : Action<FileCopyDetails> {
    private val renamer: (Array<String>) -> Array<String>?

    public constructor(renamer: Relocator) {
        this.renamer = renamer
    }

    public constructor(renamer: (Array<String>) -> Array<String>?) {
        this.renamer = renamer
    }

    public constructor(renamer: Closure<Array<String>?>) : this(object : Relocator {
        override fun invoke(path: Array<String>): Array<String>? {
            return renamer.call(path)
        }
    })

    override fun execute(fileCopyDetails: FileCopyDetails) {
        val path = fileCopyDetails.relativePath
        val newPath = renamer(path.segments) ?: return
        fileCopyDetails.relativePath = RelativePath(true, *newPath)
    }
}
