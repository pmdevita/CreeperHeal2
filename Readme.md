# CreeperHeal2 (TBD)

All the fun of explosions with none of the cleanup!

This plugin is meant to be a spiritual successor to the original CreeperHeal plugin https://github.com/nitnelave/CreeperHeal. 
Unfortunately, it has not been updated in 3 years. Initially I was going to just fork it but the implementation seems 
far more complicated than needed so I rewrote it.


# Current Progress

## Main tasks
- [x] Support for independent blocks
- [x] Support for gravity-affected blocks
- [x] Support for top-dependent blocks
- [x] Support for side-dependent blocks
- [x] Support for unaffected dependent blocks whose parent block is affected by the explosion
- [x] Shutdown/reload safety

## Edge cases
- [x] Vines depend from top up/direction facing
- [ ] Paintings have multiple dependent blocks
- [ ] Item frames
- [ ] Chorus plants need to be rescanned to build their dependency tree
- [ ] Scaffolding need to be rescanned to build its dependency tree
- [ ] Minecart rails don't maintain orientation when replaced
- [ ] Double chest