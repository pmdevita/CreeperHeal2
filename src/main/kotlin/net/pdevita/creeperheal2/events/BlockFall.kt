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
        // Only falling blocks matter
        if (event.entityType == EntityType.FALLING_BLOCK && event.to == Material.AIR) {
            val block = event.block
            // Double check this falling block is indeed a material that can fall
            if (plugin.constants.gravityBlocks.contains(block.blockData.material)) {
                // Now check if it's one of ours
                if (plugin.gravity.checkLocation(block.location)) {
                    // It is, cancel it
                    event.isCancelled = true
                    // Update the block to fix a visual client bug, but don't apply physics
                    event.block.state.update(false, false)
                }
            }
        }
    }
}