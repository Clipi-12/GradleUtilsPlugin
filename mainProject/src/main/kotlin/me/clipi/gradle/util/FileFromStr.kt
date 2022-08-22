package me.clipi.gradle.util

import org.gradle.api.Project
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
public class FileFromStr {
    public companion object {
        public fun concat(parent: File, vararg children: String): File {
            var result = parent
            children.forEach {
                result = File(result, it)
            }
            return result
        }

        public fun dirs(path: String, project: Project): Array<File> {
            return dirs(path, project.projectDir)
        }


        public fun dirs(path_: String, parent: File): Array<File> {
            var path = path_
            var res = arrayOf(parent)
            while (true) {
                val i = path.indexOf('/')
                if (i == -1) break
                val name = path.substring(0, i)
                if (name.isNotEmpty()) res = dirsFromParents(res, name)
                path = path.substring(i + 1)
            }
            res = dirsFromParents(res, path)
            return res
        }


        public fun dir(path_: String, project: Project): File {
            var path = path_
            var res: File? = null
            while (true) {
                val i = path.indexOf('/')
                if (i == -1) break

                val name = path.substring(0, i)
                if (name.isNotEmpty()) res = dirFromParent(project, res, name)
                path = path.substring(i + 1)
            }
            res = dirFromParent(project, res, path)
            return res
        }

        private fun dirFromParent(project: Project, parent: File?, childName: String): File {
            return File(parent ?: project.projectDir, childName)
        }

        private fun dirsFromParents(parents: Array<File>, childName: String): Array<File> {
            val res = mutableListOf<File>()
            if ("*" == childName) {
                for (parent in parents) {
                    for (child in parent.listFiles()!!) {
                        res.add(child)
                    }
                }
            } else {
                for (parent in parents) {
                    val child = File(parent, childName)
                    if (child.isDirectory) res.add(child)
                }
            }
            return res.toTypedArray()
        }
    }
}
