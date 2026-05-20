plugins {
    // 1.9.x requires Gradle 8.11+; wrapper stays on 8.10.2 (see gradle-wrapper.properties).
    id("fabric-loom") version "1.8.13"
}

base {
    archivesName.set("playerdatasync-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.4")
    mappings("net.fabricmc:yarn:1.21.4+build.8:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.119.2+1.21.4")

    implementation(project(":api"))
    implementation(project(":common"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
