# Modrinth & multi-loader distribution

## One download JAR (`build/libs/PlayerDataSyncReloaded-<version>.jar`)

The main **`PlayerDataSyncReloaded-*.jar`** (from `./gradlew :plugin:build` / `:plugin:shadowJar` plus `copyJar`) is the **Paper / Purpur / Folia** plugin (`plugin.yml` at the JAR root).

It also contains **`bundled/`** with the other platforms as **separate JAR files** (same build, same version):

| Path in archive | Use |
|-----------------|-----|
| `bundled/playerdatasync-velocity.jar` | Velocity `plugins/` |
| `bundled/playerdatasync-fabric.jar` | Fabric `mods/` or Modrinth **Fabric** file |
| `bundled/playerdatasync-forge.jar` | Forge `mods/` or Modrinth **Forge** file |
| `bundled/README.txt` | Short instructions |

Extract the file you need (ZIP tools: copy `bundled/*.jar` out). **Modrinth** does not treat nested JARs as loaders — upload the **extracted** Fabric/Forge/Velocity JAR for each loader slot.

---

## Past error: *No fabric.mod.json present for Fabric file*

That happens if you upload the **Paper** JAR (root has `plugin.yml` only) to Modrinth’s **Fabric** slot. Use **`bundled/playerdatasync-fabric.jar`** (after extraction) for Fabric.

---

## Verify Fabric payload

```bash
jar tf build/libs/PlayerDataSyncReloaded-*.jar | findstr bundled/playerdatasync-fabric
jar tf build/libs/PlayerDataSyncReloaded-*.jar | findstr bundled/README
```

Standalone Fabric build: `./gradlew :fabric-versions:v1_20_R1:build` (replace `v1_20_R1` with `v1_21_R1` or `v26_1_R1`). Runs **`checkFabricModMetadata`** on that module.
