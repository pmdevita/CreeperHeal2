# CreeperHeal2 (v1.0 Beta) (1.15)

All the fun of explosions with none of the cleanup!

This plugin is meant to be a spiritual successor to the original CreeperHeal plugin https://github.com/nitnelave/CreeperHeal. 
Unfortunately, it has not been updated in 3 years. Initially I was going to just fork it but the implementation seems 
far more complicated than needed so I rewrote it.

# Setup/Installation

Download the latest release and place it in your plugins folder. The config.yml is generated the first 
time the plugin is run with the server. You can also look at it 
[here](https://github.com/pmdevita/CreeperHeal2/blob/master/src/main/resources/config.yml).

# Warning

This plugin is still in beta and while I would consider it safer than the original on 
modern servers and it supports every block except those listed below, it still may fail to 
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

# Development progress

## Main tasks
- [x] Support for independent blocks
- [x] Support for gravity-affected blocks
- [x] Support for top-dependent blocks
- [x] Support for side-dependent blocks
- [x] Support for unaffected dependent blocks whose parent block is affected by the explosion
- [x] Shutdown/reload safety
- [x] Polish (Configuration options, replacing from bottom up, move entities out of the way, popping sound)
- [ ] Add 1.13/1.14 support 

## Edge cases
- [x] Vines depend from top up/direction facing
- [x] Double chests
- [ ] Paintings have multiple dependent blocks
- [ ] Item frames
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Scaffolding need to be rescanned to build its dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
