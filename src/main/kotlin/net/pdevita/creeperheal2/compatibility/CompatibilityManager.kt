package net.pdevita.creeperheal2.compatibility

import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.core.Boundary
import net.pdevita.creeperheal2.events.CHExplosionEvent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import java.util.*

class CompatibilityManager(val plugin: CreeperHeal2) {
    fun loadCompatibilityPlugins() {
        val serviceLoader = ServiceLoader.load(BaseCompatibility::class.java, plugin.javaClass.classLoader)
        plugin.debugLogger("Found ${serviceLoader.count()} compatibility plugin(s)")

        for (compatibilityPlugin in serviceLoader) {
            val otherPlugin = Bukkit.getPluginManager().getPlugin(compatibilityPlugin.pluginName) ?: continue
            if (otherPlugin.javaClass.name != compatibilityPlugin.pluginPackage) {
                continue
            }
            plugin.debugLogger("Loading compatibility for plugin ${compatibilityPlugin.pluginName} ${otherPlugin.javaClass.name}")
            try {
                compatibilityPlugin.setPluginReference(otherPlugin)
                compatibilityPlugin.setCreeperHealReference(plugin)
                plugin.server.pluginManager.registerEvents(compatibilityPlugin, plugin)
            } catch (e: NoClassDefFoundError) {
                plugin.debugLogger("NoClassDefFoundError")
                continue
            }
        }
    }

    fun maskBlocksFromExplosion(blockList: MutableList<Block>) {
        val boundary = Boundary(blockList)
        val world = blockList.first().world

        val event = CHExplosionEvent(blockList, world, boundary)
        plugin.debugLogger("Calling CHExplosionEvent in $world with $boundary")
        Bukkit.getPluginManager().callEvent(event)
    }
}