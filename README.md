# PlayerDataSync Reloaded

Data synchronization engine for multi-server Minecraft networks. Updated for Minecraft 26.2, Paper, Fabric, and Java 25.

Forked from [DerGamer009/PlayerDataSyncReloaded](https://github.com/DerGamer009/PlayerDataSyncReloaded).

## Features

- **Asynchronous Syncing**: Keeps main server thread unblocked during inventory and stats save/load cycles.
- **Multi-Platform Support**: Companion modules for Paper/Spigot and Fabric 26.2.
- **Comprehensive State Sync**: Synchronizes player inventory, Ender Chests, health, hunger, XP, advancements, potion effects, and PDC metadata.

## Installation

1. Place `player-data-sync-reloaded-0.11.0+26.2.jar` into `plugins/` (for Paper/Folia) or `player-data-sync-reloaded-fabric-0.11.0+26.2.jar` into `mods/` (for Fabric).
2. Configure database credentials in `config.yml`.
3. Restart your servers.

## License

[MIT License](LICENSE)
