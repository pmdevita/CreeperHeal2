package net.pdevita.creeperheal2.core

import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.events.BlockFall
import net.pdevita.creeperheal2.events.EntityExplode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

// Manage all known sand blocks left behind in explosions and block them from falling

class Gravity(val plugin: CreeperHeal2) {
    var blocks = HashSet<Location>()    // We use a HashSet of Location objects to quickly identify sand blocks from explosions
    private var registered: BlockFall? = null

    fun addBlocks(locations: List<Location>) {
        blocks.addAll(locations)
        updateEventRegistration()
    }
    fun removeBlocks(locations: List<Location>) {
        blocks.removeAll(locations)
        updateEventRegistration()
    }
    fun checkLocation(location: Location): Boolean {
        return blocks.contains(location)
    }
    private fun updateEventRegistration() {
        // If we have blocks to watch and aren't registered, register
        if (blocks.isNotEmpty() and (registered == null)) {
            plugin.logger.info("Registered BlockFall")
            registered = BlockFall(plugin)
            plugin.server.pluginManager.registerEvents(registered!!, plugin)
        }
        // If we have no blocks to watch and we are registered, unregister
        if (blocks.isEmpty() and (registered != null)) {
            plugin.logger.info("Unregistered BlockFall")
            registered?.let { HandlerList.unregisterAll(it) }
            registered = null
        }
    }
}