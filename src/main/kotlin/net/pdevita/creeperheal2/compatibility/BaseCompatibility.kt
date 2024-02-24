package net.pdevita.creeperheal2.compatibility

import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

interface BaseCompatibility: Listener {
    val pluginName: String /* The name of the plugin (as defined in its plugin.yml) */
    val pluginPackage: String /* The fully qualified name of the Plugin class for that plugin ex. net.pdevita.creeperheal2.CreeperHeal2 */

    fun setPluginReference(plugin: Plugin) {
        /**
         * Called at startup to pass a reference to the plugin we are providing compatibility with
         */

    }

    fun setCreeperHealReference(creeperHeal2: CreeperHeal2) {
        /**
         * Called at startup to pass a reference to the CreeperHeal2 plugin
         */
    }

    fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {
        /**
         * Passes the list of incoming blocks from the explosion. Remove any blocks that CreeperHeal should ignore.
         */
    }
}
