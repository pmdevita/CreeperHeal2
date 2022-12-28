# CreeperHeal2 (v1.4.0) (1.13-1.18)

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


# Development Progress

This plugin is still in beta and while I would consider it safer than the original on 
modern servers, and it supports every block except those listed below, it still may fail to 
replace some structures with 100% accuracy. Blocks that aren't replaced properly should drop as
items though.

Currently, the following blocks cannot be replaced properly (they will drop as items)
- Paintings
- Item frames
- Chorus plants
- Scaffolding
- Minecart rails (can be replaced fine, may not keep original orientation)

If you are able to create a structure that consistently isn't replaced properly, file a bug 
report to let me know.

## Not working

There are a couple last things that are not yet 100% working due to special implementation requirements for these blocks

- [ ] Add support for entity "blocks" like Paintings, Item Frames, and Armor Stands
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
- [ ] Liquids may remove certain blocks during replacement

## Special Thanks to

- My Discord server and the many users who've helped me test and fix things
- [Shynixn](https://github.com/Shynixn) for [MCCoroutine](https://github.com/Shynixn/MCCoroutine)

## Building

You'll need to setup GitHub Maven repo, there's an example in the .m2 folder and you 
can find more instructions [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry).

## Bstats

![img](https://bstats.org/signatures/bukkit/CreeperHeal2.svg)
