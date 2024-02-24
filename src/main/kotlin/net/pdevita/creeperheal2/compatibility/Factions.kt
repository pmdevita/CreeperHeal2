package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import com.massivecraft.factions.Board
import com.massivecraft.factions.FLocation
import com.massivecraft.factions.FactionsPlugin
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.events.CHExplosionEvent
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin

@AutoService(BaseCompatibility::class)
class Factions : BaseCompatibility {
    override val pluginName = "Factions"
    override val pluginPackage = "com.massivecraft.factions.FactionsPlugin"
    private lateinit var factionPlugin: FactionsPlugin
    private lateinit var creeperHeal2: CreeperHeal2
    var territory = false
    var wilderness = false
    var warzone = false

    override fun setPluginReference(plugin: Plugin) {
        factionPlugin = plugin as FactionsPlugin
    }

    override fun setCreeperHealReference(creeperHeal2: CreeperHeal2) {
        this.creeperHeal2 = creeperHeal2
        // Unlike the settings, these need to be true if we ignore them, and false if we repair,
        // so we negate them here
        territory = !creeperHeal2.config.getBoolean("factions.territory", true)
        wilderness = !creeperHeal2.config.getBoolean("factions.wilderness", false)
        warzone = !creeperHeal2.config.getBoolean("factions.warzone", false)
    }

    @EventHandler
    fun onCHExplosionEvent(event: CHExplosionEvent) {
//        var counter = 0
        creeperHeal2.debugLogger("Masking blocks for Factions")
        val itr = event.blockList.iterator()
        while (itr.hasNext()) {
            val block = itr.next()
            val faction = Board.getInstance().getFactionAt(FLocation(block.location))
            // If explosions are allowed in territory and we don't repair in territory
            // or if faction is a warzone and we don't repair warzones
            // or if faction is a wilderness and we don't repair wildernesses
            val shouldRemove = (!faction.noExplosionsInTerritory() && territory)
                    || (faction.isWarZone && warzone)
                    || (faction.isWilderness && wilderness)
            // then remove the block
            if (shouldRemove) {
                itr.remove()
//                counter++
            }
        }
//        println("Masked out $counter blocks from the explosion")
    }

}
