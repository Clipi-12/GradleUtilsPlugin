rootProject.name = "GradleUtilsPlugin"

include("gradle")
project(":gradle").projectDir = file("mainProject")
include("conventions")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            name = "Clipi"
            url = uri("https://clipi-repo.herokuapp.com/")
        }
    }
}
