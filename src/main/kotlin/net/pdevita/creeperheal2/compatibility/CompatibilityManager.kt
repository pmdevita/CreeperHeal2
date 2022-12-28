package net.pdevita.creeperheal2.compatibility

import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Bukkit
import org.bukkit.block.Block
import java.util.*

val COMPATIBILITY_PLUGINS = arrayOf(
    Movecraft
)

class CompatibilityManager(val plugin: CreeperHeal2) {
    val pluginList = LinkedList<BaseCompatibility>()

    fun loadCompatibilityPlugins() {
        plugin.debugLogger("Checking compatibility...")

        val serviceLoader = ServiceLoader.load(BaseCompatibility::class.java)
        plugin.debugLogger("${serviceLoader.count()}")
        for (p in Bukkit.getPluginManager().plugins) {
            plugin.debugLogger("plugin: $p")
        }

        for (compatibilityPlugin in COMPATIBILITY_PLUGINS) {
            val otherPlugin = Bukkit.getPluginManager().getPlugin(compatibilityPlugin.pluginName)
            plugin.debugLogger("Found compatibility plugin for ${compatibilityPlugin.pluginName}")
            if (otherPlugin != null) {
                plugin.debugLogger("Loading compatibility for plugin ${compatibilityPlugin.pluginName}")
                pluginList.add(compatibilityPlugin)
                assert(pluginList.last != null)
            }
        }
    }

    fun maskBlocksFromExplosion(blockList: MutableList<Block>) {
        if (pluginList.isEmpty()) {
            return
        }
        val boundary = Boundary(blockList)
        val center = boundary.center()
        val world = blockList.first().world
        for (plugin in pluginList) {
            plugin.maskBlocksFromExplosion(blockList, world, boundary, center)
        }



    }

}