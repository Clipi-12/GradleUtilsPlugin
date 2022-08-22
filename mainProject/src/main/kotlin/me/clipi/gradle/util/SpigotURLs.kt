package me.clipi.gradle.util

import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

internal class SpigotURLs {
    companion object {

        /**
         * Spigot will always return a SHA-1 represented as a 40 characters long hex string,
         * even when the commit ID starts with a 0
         *
         * [Example](https://hub.spigotmc.org/versions/1.12.json)
         */
        @Suppress("GrazieInspection")
        private val commitID = Pattern.compile("[\\da-f]{40}")
        private val craftBukkitCommit = Pattern.compile("\"CraftBukkit\": ?\"(${commitID})\"")
        private val craftBukkitVersion = Pattern.compile("<version>((?:(?!</version>).)+)</version>")
        private val javaMajor = Pattern.compile("\"javaVersions\": ?\\[(\\d{2,}), ?(\\d{2,})]")

        private val json = HashMap<String, String>()
        private fun infoAsJson(version: String): String {
            return json.computeIfAbsent(
                version
            ) { k -> URL(versionToJson(k)).openStream().bufferedReader().use { it.readText() } }
        }

        private val craftBukkitName = HashMap<String, String>()
        fun craftBukkitName(version: String): String {
            return craftBukkitName.computeIfAbsent(version) { k ->
                val infoAsJson = infoAsJson(k)
                val commit = GradleUtil.first(craftBukkitCommit, infoAsJson)
                val infoAsPom = URL(commitToPom(commit)).openStream().bufferedReader().use { it.readText() }
                return@computeIfAbsent GradleUtil.first(craftBukkitVersion, infoAsPom)
            }
        }

        fun version(version: String): Int {
            val infoAsJson = infoAsJson(version)
            val matcher = javaMajor.matcher(infoAsJson)
            return if (matcher.find()) getLts(matcher) else 8
        }

        /**
         * [LTS versions](https://www.oracle.com/java/technologies/java-se-support-roadmap.html#:~:text=product%20offerings%20include%3A-,Oracle%20Java%20SE%20Support%20Roadmap,-*%E2%80%A0)
         */
        private fun getLts(matcher: Matcher): Int {
            val min = matcher.group(1).toInt() - 44
            val max = matcher.group(2).toInt() - 44

            // LTS versions in descending order
            val result: OptionalInt =
                Arrays.stream(intArrayOf(21, 17, 11, 8, 7)).filter { lts -> lts in min..max }.findFirst()
            return result.orElse(max)
        }

        private fun versionToJson(version: String): String {
            return "https://hub.spigotmc.org/versions/${version}.json"
        }

        private fun commitToPom(commit: String): String {
            return "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/raw/pom.xml?at=${commit}"
        }
    }
}
