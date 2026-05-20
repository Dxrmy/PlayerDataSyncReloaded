pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
    }
}

rootProject.name = "PlayerDataSyncReloaded"

include("api")
include("common")
include("plugin")
include("velocity")

// Version modules (Paper NMS handlers + Fabric/Forge adapters per line)
val versionModules = listOf(
    "v1_20_R1", "v1_21_R1", "v26_1_R1"
)

versionModules.forEach {
    include("versions:$it")
    include("fabric-versions:$it")
    include("forge-versions:$it")
}
