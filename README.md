# GradleUtilsPlugin
## A gradle plugin that brings extensions and conventions to keep the build file [DRY](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself)

This project is divided into two modules.  
The first one is a gradle plugin that includes extensions to keep your gradle build files DRY
* It includes Zip and Jar alternatives to be able to modify their behaviour (for example, getting rid of META-INF/MANIFEST.MF)
* It applies the java plugin automatically, as it is needed for almost every build these days
* It also registers the MavenCentral and Google repositories for the same reason
* It includes an addon system that can be used if needed. Some of the default addons included help with:
  * Spigot development
  * Lombok development
  * JDK requirements
  * Javadoc customization

All of the features can be configured or disabled if wanted

The second module includes gradle conventions and precompiled build scripts, so that the most repetitive tasks
can be applied without having to write them everywhere.

## Getting Started
To use this project, you will have to register the maven-repository <https://clipi-repo.herokuapp.com/>.
You will then be able to apply the plugin with the id `me.clipi.gradle`
and the conventions with `me.clipi.gradle.conventions.CONVENTION_ID`
### Gradle
##### Kotlin
`settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Clipi"
            url = uri("https://clipi-repo.herokuapp.com/")
        }
    }
}
```
`build.gradle.kts`
```kotlin
plugins {
    id("me.clipi.gradle") version "latest.release"
    id("me.clipi.gradle.conventions.CONVENTION_ID") version "latest.release"
}
```
##### Groovy
`settings.gradle`
```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = 'Clipi'
            url = uri 'https://clipi-repo.herokuapp.com/'
        }
    }
}
```
`build.gradle`
```groovy
plugins {
    id 'me.clipi.gradle' version 'latest.release'
    id 'me.clipi.gradle.conventions.CONVENTION_ID' version 'latest.release'
}
```

## License
> Lesser General Public License version 3

> GradleUtilsPlugin is free software: you can redistribute it and/or modify
> it under the terms of the GNU Lesser General Public License as published by
> the Free Software Foundation, either version 3 of the License, or
> (at your option) any later version.  
> GradleUtilsPlugin is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
> GNU Lesser General Public License for more details.  
> You should have received a copy of the GNU Lesser General Public License
> along with GradleUtilsPlugin. If not, see <https://www.gnu.org/licenses/>.

See also: [LIBRARIES.md](LIBRARIES.md)
