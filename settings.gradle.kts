

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
    }
}

buildscript {
    configurations.all {
        resolutionStrategy {
            force("org.ow2.asm:asm:9.9.1")
            force("org.ow2.asm:asm-analysis:9.9.1")
            force("org.ow2.asm:asm-commons:9.9.1")
            force("org.ow2.asm:asm-tree:9.9.1")
            force("org.ow2.asm:asm-util:9.9.1")
        }
    }
}

rootProject.name = "PlayerDataSyncReloaded"

include("api")
include("common")
include("plugin")
include("velocity")

// Version modules (Paper NMS handlers + Fabric/Forge adapters per line)
val versionModules = listOf(
    "v26_1_R1"
)

versionModules.forEach {
    include("versions:$it")
    include("fabric-versions:$it")
}
