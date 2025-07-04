pluginManagement {
    val ktor_version: String by settings
    val kotlin_version: String by settings

    plugins {
        plugins {
            kotlin("jvm") version kotlin_version
            id("io.ktor.plugin") version ktor_version
            id("org.jetbrains.kotlin.plugin.serialization") version kotlin_version
        }
    }
}

rootProject.name = "pro.aibar.SweatSketchService"
include("src:main")
findProject(":src:main")?.name = "main"
//include("src:test")
//findProject(":src:test")?.name = "test"