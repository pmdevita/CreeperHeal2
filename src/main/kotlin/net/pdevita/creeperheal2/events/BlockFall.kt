package net.pdevita.creeperheal2.events

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent


class BlockFall(var plugin: CreeperHeal2): Listener {
    // Most falling blocks are handled here
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
    // Scaffolding could work here but bottom gets set to true and distance gets set to 7 so everything attached
    // to it breaks. So it doesn't really work
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    private fun onBlockFade(event: BlockFadeEvent) {
//        // Only falling blocks matter
//        if (plugin.constants.gravityBlocks.contains(event.block.blockData.material)) {
//            // Now check if it's one of ours
//            if (plugin.gravity.checkLocation(event.block.location)) {
//                // It is, cancel it
//                event.isCancelled = true
//                // Update the block to fix a visual client bug, but don't apply physics
////                event.block.state.update(false, false)
//            }
//        }
//    }
}