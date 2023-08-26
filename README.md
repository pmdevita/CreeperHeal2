# CreeperHeal2 (v2.0.0) (1.13-1.19+)

[Spigot Plugin Page](https://www.spigotmc.org/resources/creeperheal2.80585/)

All the fun of explosions with none of the cleanup!

This plugin is meant to be a spiritual successor to the original CreeperHeal plugin 
https://github.com/nitnelave/CreeperHeal. Thanks to [nitnelave](https://github.com/nitnelave/) 
for the original idea and name!

# Setup/Installation

Download the latest release and place it in your plugins folder. The config.yml is generated the first 
time the plugin is run on the server. You can also look at it 
[here](https://github.com/pmdevita/CreeperHeal2/blob/master/src/main/resources/config.yml).

New versions of Minecraft past the currently supported version do work, with the exception that newly 
added blocks may not repair properly.

# Commands and Permissions

- `/creeperheal warp` - `creeperheal2.warp` Immediately heal all currently tracked explosions
- `/creeperheal stats` - `creeperheal2.stats` View stats about currently tracked explosions
- `/creeperheal cancel` - `creeperheal2.cancel` Cancel replacement of currently tracked explosions (you will lose blocks so be careful)

# Compatibility

Currently, CreeperHeal2 has official support for the following plugins:

- Movecraft [GitHub](https://github.com/APDevTeam/Movecraft) [Spigot](https://www.spigotmc.org/resources/movecraft.31321/)
- FactionsUUID [GitHub](https://github.com/drtshock/Factions) [Spigot](https://www.spigotmc.org/resources/factionsuuid.1035/)

# Development Progress

After a few years of testing and several iterations, I can confidently say this plugin 
has reached maturity for the features it supports. There are still some features missing 
that have been difficult to implement or solve, or I just haven't had time for yet.

Currently, the following blocks are not replaced properly (they will drop as items).
- Chorus plants
- Minecart rails (can be replaced fine, may not keep original orientation)

If you are able to create a structure that consistently isn't replaced properly, file a bug 
report to let me know.

## Not working

There are a couple last things that are not yet 100% working due to special implementation requirements for these blocks

- [x] Add support for entity "blocks" like Paintings, Item Frames, and Armor Stands - Added with 2.0.0!
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
- [ ] Liquids may remove certain blocks during replacement

## Special Thanks to

- My Discord server and the many users who've helped me test and fix things
- [Shynixn](https://github.com/Shynixn) for [MCCoroutine](https://github.com/Shynixn/MCCoroutine)

## Building

You'll need to setup GitHub Maven repo, there's an example in the .m2 folder, and you 
can find more instructions [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry). 
The `settings.xml` file need to go in `~/.m2/` on Mac/Linux and in `C:\Users\USERNAME\.m2\` on Windows. You need 
a Github Personal Access Token with only the `read:packages` permission.

If you are working on any of the ServiceProvider classes, disable incremental compilation. There's some 
sort of bug where it might drop one of the classes from the service manifest between builds. If you are 
swapping between incremental and full, it might also be a good idea to clean the build before building 
again.

### Adding compatibility with another plugin

This section needs to be fully fleshed out and a proper repo needs to be setup. 
The quick answer for now is, if you want to prevent CreeperHeal2 from restoring certain blocks,
create a plugin that implement the BaseCompatibility interface, and register it as a ServiceProvider
(you usually do this with AutoService). You can see a few examples in the `compatibility` package.

If you would like to pass a list of blocks for CreeperHeal2 to delete and then restore, you can do so 
through the `createNewExplosion` method on the `CreeperHeal2` plugin object, passing it your collection 
of `Block` objects. 

If you need help, feel free to reach out in discussions or the issues. I also occasionally check the 
Spigot forums.

## Bstats

![img](https://bstats.org/signatures/bukkit/CreeperHeal2.svg)
