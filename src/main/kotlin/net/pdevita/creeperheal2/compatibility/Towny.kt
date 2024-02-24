package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import com.palmergames.bukkit.towny.TownyAPI
import net.pdevita.creeperheal2.events.CHExplosionEvent
import org.bukkit.event.EventHandler

@AutoService(BaseCompatibility::class)
class Towny : BaseCompatibility {
    override val pluginName = "Towny"
    override val pluginPackage = "com.palmergames.bukkit.towny.Towny"
    @EventHandler
    fun onCHExplosionEvent(event: CHExplosionEvent) {
        val itr = event.blockList.iterator()
        while (itr.hasNext()) {
            val block = itr.next()
            val town = TownyAPI.getInstance().getTown(block.location)

            if (town != null) {
                itr.remove()
            }
        }
    }

}
