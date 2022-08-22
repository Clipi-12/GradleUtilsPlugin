import co.uzzu.dotenv.gradle.DotEnvProperty
import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.ajoberstar.gradle.git.publish.tasks.GitPublishPush
import org.ajoberstar.gradle.git.publish.tasks.GitPublishReset
import java.io.IOException

plugins {
    id("org.ajoberstar.git-publish")
    id("co.uzzu.dotenv.gradle")
}

val outRepo: Provider<Directory> = layout.buildDirectory.dir("out-repo")
val localRepoName = "LocalDiskToGit"

allprojects {
    this@allprojects.pluginManager.apply(LifecycleBasePlugin::class.java)
    this@allprojects.pluginManager.apply(MavenPublishPlugin::class.java)

    this@allprojects.extensions.configure<PublishingExtension>("publishing") {
        repositories {
            maven {
                name = localRepoName
                url = uri(outRepo)
            }
        }
    }
}

fun appendIfDoesntExist(fileName: String, key: String, value: String) {
    val file = File(projectDir, fileName)
    file.createNewFile()
    if (!file.isFile) throw IOException("Could not create file $file")
    val lines = file.readLines()
    if (lines.any { line -> line.startsWith(key) }) return
    var content = "$key$value"
    if (lines.isNotEmpty() && lines.last().isNotBlank()) content = "\r\n$content"
    file.appendText(content)
}

fun getEnv(key: String) = ((env as ExtensionAware).extensions.getByName(key) as DotEnvProperty).value
appendIfDoesntExist(".env", "GIT_USER=", "YOUR_GIT_USER")
appendIfDoesntExist(".env", "GIT_PASS=", "YOUR_TOKEN")
appendIfDoesntExist(".env.template", "GIT_USER=", "YOUR_GIT_USER")
appendIfDoesntExist(".env.template", "GIT_PASS=", "YOUR_TOKEN")
appendIfDoesntExist(".gitignore", ".env", "")
appendIfDoesntExist("gradle.properties", "GIT_REPO_URI=", "https://github.com/OwnerAccount/Repository.git")
System.setProperty("org.ajoberstar.grgit.auth.username", getEnv("GIT_USER"))
System.setProperty("org.ajoberstar.grgit.auth.password", getEnv("GIT_PASS"))


extensions.configure<GitPublishExtension>("gitPublish") {
    repoUri.set(property("GIT_REPO_URI") as String)

    branch.set("main")

    contents {
        from(outRepo) {
            into(".m2")
        }
    }

    preserve {
        include("**")
    }
}

val prePush = "publishAllPublicationsTo${localRepoName}Repository"
tasks.getByName(prePush) {
    dependsOn(this@getByName.project.allprojects.map { p -> p.tasks.withType(Javadoc::class.java) }
        .fold(setOf<Javadoc>()) { a, b -> a + b })
    dependsOn(this@getByName.project.allprojects.mapNotNull { p -> p.tasks.getByName("build") }
        .fold(setOf<Task>()) { a, b -> a + b })
    dependsOn(this@getByName.project.subprojects.mapNotNull { p -> p.tasks.getByName(prePush) }
        .fold(setOf<Task>()) { a, b -> a + b })
    finalizedBy(tasks.withType(GitPublishPush::class.java))
}

tasks.withType(GitPublishReset::class.java) {
    dependsOn(tasks.getByName(prePush))
}
