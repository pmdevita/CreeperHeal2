# CreeperHeal2 (v2.1.0) (1.13-1.20+)

[Spigot Plugin Page](https://www.spigotmc.org/resources/creeperheal2.80585/) | [Dokka](https://pmdevita.github.io/CreeperHeal2/dokka) | [Javadocs](https://pmdevita.github.io/CreeperHeal2/javadoc)

All the fun of explosions with none of the cleanup!

This plugin is meant to be a spiritual successor to the original CreeperHeal plugin 
https://github.com/nitnelave/CreeperHeal. Thanks to [nitnelave](https://github.com/nitnelave/) 
for the original idea and name!

Features:
- Support for every normal explosion type and custom ones
- Smart block replacement 
- Multi-threaded optimizations
- Support for Factions, Movecraft, Cannons, and more
- Lots of configuration options

## What is smart block replacement?

CreeperHeal2 always repairs blocks in a "safe" order. For example, if you had a sign attached to 
the side of a block, CreeperHeal2 will always replace the block before the sign, ensuring the sign 
is never in an invalid state where it could be "popped" by a block update.

CreeperHeal2 also detects any blocks that would be destroyed or otherwise affected by an explosion. 
Taking the example of the block and sign again, if only the block was in the explosion, normally the 
sign would pop off once the block was removed. CreeperHeal2 will automatically detect this and save 
the sign.

CreeperHeal2 also detects if two explosions happen next to each other. With the sign and block example, 
if both were involved in two side-by-side explosions, there's a chance the sign's explosion would restore 
it before the block's! CreeperHeal2 solves this issue by merging all nearby explosions, allowing them 
to restore in a proper order together.

The result of all of this is that CreeperHeal2 is able to save and restore nearly any structure perfectly! 
Give it a try!

# Setup/Installation

Download the latest release and place it in your plugins folder. The config.yml is generated the first 
time the plugin is run on the server. You can also look at it 
[here](https://github.com/pmdevita/CreeperHeal2/blob/master/src/main/resources/config.yml).

CreeperHeal2 is generally future-proof, but some blocks in new versions may not repair in the proper order 
without an update.

# Commands and Permissions

- `/creeperheal warp` - `creeperheal2.warp` Immediately heal all currently tracked explosions
- `/creeperheal stats` - `creeperheal2.stats` View stats about currently tracked explosions
- `/creeperheal cancel` - `creeperheal2.cancel` Cancel replacement of currently tracked explosions (you will lose blocks so be careful!)

# Compatibility

Currently, CreeperHeal2 has official support for the following plugins:

- Movecraft [GitHub](https://github.com/APDevTeam/Movecraft) [Spigot](https://www.spigotmc.org/resources/movecraft.31321/)
- FactionsUUID (MassiveCraft/drtshock) [GitHub](https://github.com/drtshock/Factions) [Spigot](https://www.spigotmc.org/resources/factionsuuid.1035/)

And has tested compatibility with

- Cannons (enable `custom` explosions)
- Towny

## Not working

There are a couple last things that are not yet 100% working due to special implementation 
requirements for these blocks. They're not game breaking, but they're the few things 
preventing the plugin from being completely perfect.

- [x] Add support for entity "blocks" like Paintings, Item Frames, and Armor Stands - Added with 2.0.0!
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
- [ ] Liquids may remove certain blocks during replacement

## Special Thanks to

- My Discord server and the many users who've helped me test and fix things
- [Shynixn](https://github.com/Shynixn) for [MCCoroutine](https://github.com/Shynixn/MCCoroutine)
- Previous contributors and sponsors!

## Building

CreeperHeal2 comes with a public PAT to authenticate to GitHub maven repos, so there shouldn't be much 
involved in setup.

If you are working on any of the ServiceProvider classes, disable incremental compilation. There's some 
sort of bug where it might drop one of the classes from the service manifest between builds. If you are 
swapping between incremental and full, it might also be a good idea to clean the build before building 
again.

### Adding compatibility with another plugin

CreeperHeal2 now broadcasts a new event called CHExplosionEvent 
[Dokka](https://pmdevita.github.io/CreeperHeal2/dokka/-creeper-heal2/net.pdevita.creeperheal2.events/-c-h-explosion-event/index.html) 
[Javadocs](https://pmdevita.github.io/CreeperHeal2/javadoc/net/pdevita/creeperheal2/events/CHExplosionEvent.html). 
Plugins can listen to this event and remove any blocks they don't want CreeperHeal2 touching.

If you need help, feel free to reach out in discussions or the issues. I also occasionally check the 
Spigot forums.

## Bstats

![img](https://bstats.org/signatures/bukkit/CreeperHeal2.svg)
