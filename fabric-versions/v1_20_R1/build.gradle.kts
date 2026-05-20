plugins {
    // Keep the same Loom version as other :fabric-versions:* modules — mixing Loom 1.6 + 1.8 breaks remapJar (ClassCastException).
    id("fabric-loom") version "1.8.13"
}

base {
    archivesName.set("playerdatasync-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")

    implementation(project(":api"))
    implementation(project(":common"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
