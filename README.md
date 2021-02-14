# CreeperHeal2 (v1.2.0) (1.13-1.16)

[Spigot Plugin Page](https://www.spigotmc.org/resources/creeperheal2.80585/)

All the fun of explosions with none of the cleanup!

This plugin is meant to be a spiritual successor to the original CreeperHeal plugin 
https://github.com/nitnelave/CreeperHeal. Thanks to [nitnelave](https://github.com/nitnelave/) 
for the original idea and name!

# Setup/Installation

Download the latest release and place it in your plugins folder. The config.yml is generated the first 
time the plugin is run with the server. You can also look at it 
[here](https://github.com/pmdevita/CreeperHeal2/blob/master/src/main/resources/config.yml).

New versions of Minecraft past the currently supported version do work, with the exception that newly 
added blocks may not repair properly.

# Development Progress

This plugin is still in beta and while I would consider it safer than the original on 
modern servers, and it supports every block except those listed below, it still may fail to 
replace some structures with 100% accuracy. Blocks that aren't replaced properly should drop as
items though.

Currently, the following blocks can not be replaced properly (they will drop as items)
- Paintings
- Item frames
- Chorus plants
- Scaffolding
- Minecart rails (can be replaced fine, may not keep original orientation)

If you are able to create a structure that consistently isn't replaced properly, file a bug 
report to let me know.

## Main tasks
- [x] Support for independent blocks
- [x] Support for gravity-affected blocks
- [x] Support for top-dependent blocks
- [x] Support for side-dependent blocks
- [x] Support for unaffected dependent blocks whose parent block is affected by the explosion
- [x] Shutdown/reload safety
- [x] Polish (Configuration options, replacing from bottom up, move entities out of the way, popping sound)
- [x] Add 1.13/1.14 support 

## Edge cases
- [x] Vines depend from top up/direction facing
- [x] Double chests
- [ ] Paintings have multiple dependent blocks
- [ ] Item frames
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Scaffolding need to be rescanned to build its dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
- [ ] Liquids may remove certain blocks during replacement
