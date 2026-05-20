import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.zip.ZipFile

// Nested :fabric-versions:* are not root `subprojects { }` children; set --release explicitly.
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    afterEvaluate {
        tasks.register("checkFabricModMetadata") {
            group = "verification"
            description = "Ensures remapJar contains fabric.mod.json at the archive root."
            dependsOn(tasks.named("remapJar"))

            doLast {
                val outJar = tasks.named<AbstractArchiveTask>("remapJar").get().archiveFile.get().asFile
                require(outJar.exists()) { "Expected remapped Fabric jar at ${outJar.absolutePath}" }
                val hasModJson = ZipFile(outJar).use { zip -> zip.getEntry("fabric.mod.json") != null }
                require(hasModJson) {
                    "fabric.mod.json missing in ${outJar.absolutePath}. " +
                        "For Modrinth, upload THIS remapped Fabric jar (playerdatasync-fabric-*.jar), not plugin/build/libs/PlayerDataSyncReloaded-*.jar."
                }
            }
        }

        tasks.findByName("check")?.dependsOn("checkFabricModMetadata")
    }
}
