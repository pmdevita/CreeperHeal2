package net.pdevita.creeperheal2.events

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class EntityExplode(var plugin: CreeperHeal2): Listener {
    @EventHandler
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        plugin.logger.info("An explosion has happened!")
//        plugin.logger.info(event.blockList().toString())
        this.plugin.createNewExplosion(event.blockList())
        event.yield = 0F
    }
}