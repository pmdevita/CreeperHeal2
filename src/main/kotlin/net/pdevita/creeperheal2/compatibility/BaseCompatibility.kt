package net.pdevita.creeperheal2.compatibility

import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin

interface BaseCompatibility {
    val pluginName: String /* The name of the plugin (as defined in its plugin.yml) */

    fun setPluginReference(plugin: Plugin) {
        /**
         * Called at startup to pass a reference to the plugin we are providing compatibility with
         */

    }
    fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {
        /**
         * Passes the list of incoming blocks from the explosion. Remove any blocks that CreeperHeal should ignore.
         */
    }
}
