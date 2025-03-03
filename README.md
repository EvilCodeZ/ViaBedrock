# ViaBedrock
ViaVersion addon to add support for Minecraft: Bedrock Edition servers.

ViaBedrock aims to be as compatible and accurate as possible with the Minecraft: Bedrock Edition protocol.

## Usage
**ViaBedrock is in very early stages of development and NOT intended for regular use yet.**

If you want to try it out anyway here are the steps to get it set up:

1. Download the latest [ViaBedrock dev build](https://build.lenni0451.net/job/ViaBedrock/) (Click on the **ViaProxy-ViaBedrockPlugin-x.x.x-SNAPSHOT.jar** file).
2. Download the latest [ViaProxy dev build](https://build.lenni0451.net/job/ViaProxy/) (Click on the **ViaProxy-x.x.x.jar** file).
3. Start ViaProxy once and close it again.
4. Put the ViaBedrock jar file into the `plugins` folder of ViaProxy.
5. Start ViaProxy again and select "Bedrock xxx" as the server version.

**Do not report any bugs yet. There are still a lot of things which are not implemented yet.**

If you want to talk about ViaBedrock or learn more about it you can join my [Discord](https://discord.gg/dCzT9XHEWu).

## Credits
ViaBedrock would not have been possible without the following projects:
- [ViaVersion](https://github.com/ViaVersion/ViaVersion): Provides the base for translating packets
- [CloudburstMC Protocol](https://github.com/CloudburstMC/Protocol): Documentation of the Bedrock Edition protocol
- [wiki.vg](https://wiki.vg/Bedrock_Protocol): Documentation of the Bedrock Edition protocol
- [mcrputil](https://github.com/valaphee/mcrputil): Documentation of Bedrock Edition resource pack encryption

Additionally ViaBedrock uses assets and data dumps from other projects: See the `Data Asset Sources.md` file for more information.

## Features
- [x] Pinging
- [x] Joining
- [x] Xbox Live Auth
- [x] Chat / Commands
- [x] Chunks
- [x] Chunk caching
- [x] Block updates
- [x] Biomes
- [x] Players
- [x] Entities
- [ ] Entity interactions
- [ ] Entity metadata
- [ ] Entity attributes
- [ ] Entity mounts
- [x] Client-Authoritative Movement
- [ ] Server-Authoritative Movement
- [ ] Client-Authoritative Inventory
- [ ] Server-Authoritative Inventory
- [ ] Block breaking
- [ ] Block placing
- [ ] Respawning
- [x] Dimension switching
- [ ] Form GUIs
- [ ] Scoreboard
- [x] Titles
- [ ] Bossbar
- [x] Player list
- [ ] Command suggestions
- [ ] Sounds
- [ ] Particles
- [x] Player Skins (Requires [BedrockSkinUtility](https://github.com/Camotoy/BedrockSkinUtility) mod)
- [x] Very basic resource pack conversion (Contributions welcome)
