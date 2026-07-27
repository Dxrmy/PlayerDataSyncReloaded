// Fabric line aligned with Paper `v26_1_R1` (game stack still 1.21.x); bump MC/Fabric when you target a newer client.
plugins {
    id("fabric-loom") version "1.9.2"
}

base {
    archivesName.set("playerdatasync-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1")

    implementation(project(":api"))
    implementation(project(":common"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
