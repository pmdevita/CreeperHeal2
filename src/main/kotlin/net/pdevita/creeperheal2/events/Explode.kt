package net.pdevita.creeperheal2.events

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

class Explode(var plugin: CreeperHeal2): Listener {
    @EventHandler
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        plugin.debugLogger("An entity explosion has happened! ${event.entityType.toString()}")
        if (plugin.settings.types.allowExplosionEntity(event.entityType)) {
            this.plugin.createNewExplosion(event.blockList())
            event.yield = 0F
        }
    }

    @EventHandler
    fun onBlockExplodeEvent(event: BlockExplodeEvent) {
        plugin.debugLogger("A block explosion has happened! ${event.block.toString()}")
        if (plugin.settings.types.allowExplosionBlock(/*event.block.blockData.material*/)) {
            this.plugin.createNewExplosion(event.blockList())
            event.yield = 0F
        }
    }
}

