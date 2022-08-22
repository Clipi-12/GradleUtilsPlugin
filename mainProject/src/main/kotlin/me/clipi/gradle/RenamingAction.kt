package me.clipi.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails

@Suppress("unused")
public class RenamingAction : Action<FileCopyDetails> {
    private val renamer: (String, String) -> String?

    public constructor(renamer: Renamer) {
        this.renamer = renamer
    }

    public constructor(renamer: (String, String) -> String?) {
        this.renamer = renamer
    }

    public constructor(renamer: Closure<String?>) : this(object : Renamer {
        override fun invoke(fullName: String, fileName: String): String? {
            return renamer.call(fullName, fileName)
        }
    })

    override fun execute(fileCopyDetails: FileCopyDetails) {
        val path = fileCopyDetails.relativePath
        val newName = renamer(path.pathString, path.lastName) ?: return
        fileCopyDetails.relativePath = path.replaceLastName(newName)
    }
}
