package net.pdevita.creeperheal2.events

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent


class BlockFall(var plugin: CreeperHeal2): Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun onEntityChangeBlockEvent(event: EntityChangeBlockEvent) {
        if (event.entityType == EntityType.FALLING_BLOCK && event.to == Material.AIR) {
            val block = event.block
//            plugin.logger.info("Falling block check " + block.blockData.material)
            if (plugin.constants.gravityBlocks.contains(block.blockData.material)) {
                // Now check if it's one of ours
                if (plugin.gravity.checkLocation(block.location)) {
//                    plugin.logger.info("Cancelling falling block " + block.blockData.material)
                    event.isCancelled = true
                    //Update the block to fix a visual client bug, but don't apply physics
                    event.block.state.update(false, false)
                }
            }
        }
    }
}