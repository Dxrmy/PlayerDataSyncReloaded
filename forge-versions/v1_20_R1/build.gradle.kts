plugins {
    id("net.minecraftforge.gradle") version "6.0.36"
}

base {
    archivesName.set("PlayerDataSync")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

minecraft {
    mappings("official", "1.20.1")
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.2.0")

    implementation(project(":api"))
    implementation(project(":common"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    val expandProps = mapOf("mod_version" to project.version)
    inputs.properties(expandProps)
    filesMatching("META-INF/mods.toml") {
        expand(expandProps)
    }
}
