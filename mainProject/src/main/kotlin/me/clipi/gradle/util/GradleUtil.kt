package me.clipi.gradle.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import java.util.regex.Pattern

public class GradleUtil {
    public companion object {
        public fun beforeResolution(project: Project, beforeResolution: Runnable) {
            project.gradle.addListener(object : DependencyResolutionListener {
                override fun beforeResolve(resolvableDependencies: ResolvableDependencies) {
                    project.gradle.removeListener(this)
                    beforeResolution.run()
                }

                override fun afterResolve(resolvableDependencies: ResolvableDependencies) {
                }
            })
        }

        public fun addFirst(project: Project, addTo: DependencySet, dependency: Any) {
            val tempConfig = project.configurations.create("__doNotUse_tempConfig")
            tempConfig.dependencies.add(project.dependencies.create(dependency))
            tempConfig.dependencies.addAll(addTo)
            addTo.clear()
            addTo.addAll(tempConfig.dependencies)
            project.configurations.remove(tempConfig)
        }

        public fun first(pattern: Pattern, string: String): String {
            val matcher = pattern.matcher(string)
            matcher.find()
            return matcher.group(1)
        }

        public fun getOsMavenExecutableExtension(): String {
            return if (System.getProperty("os.name").contains("windows")) ".cmd" else ""
        }

        public fun <S : Any> configure(obj: S, action: Action<S>) {
            action.execute(obj)
        }

        public fun <S> configure(obj: S, action: (S) -> Unit) {
            action(obj)
        }

        public fun <T : Task> configure(project: Project, taskType: Class<T>, action: (T) -> Unit) {
            project.tasks.withType(taskType, action)
        }

        public fun <T : Task> configure(project: Project, taskType: Class<T>, action: Action<T>) {
            project.tasks.withType(taskType, action)
        }
    }
}
