package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent
import com.palmergames.bukkit.towny.event.damage.TownBlockExplosionTestEvent
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.events.CHExplosionEvent
import org.bukkit.event.EventHandler

@AutoService(BaseCompatibility::class)
class Towny : BaseCompatibility {
//    override val pluginName = "TownyBUTDONTLOAD"
    override val pluginName = "Disabled"
//    override val pluginPackage = "com.palmergames.bukkit.towny.Towny"
    override val pluginPackage = "Disabled"

    val townyAPI: TownyAPI = TownyAPI.getInstance()

    @EventHandler
    fun onCHExplosionEvent(event: CHExplosionEvent) {
        val itr = event.blockList.iterator()
        while (itr.hasNext()) {
            val block = itr.next()
            val town = townyAPI.getTown(block.location)

            if (town != null && town.hasActiveWar()) {
                itr.remove()
            }
        }
    }

    @EventHandler
    fun onTownyExplosion(event: TownyExplodingBlocksEvent) {
        CreeperHeal2.instance.debugLogger("TownyExplodingBlocksEvent! ${event.townyFilteredBlockList}")
    }

    @EventHandler
    fun onTownyExplosionTest(event: TownBlockExplosionTestEvent) {
        CreeperHeal2.instance.debugLogger("TownBlockExplosionTestEvent! ${event.townBlock} ${event.town}")
    }

}
