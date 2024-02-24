package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import net.countercraft.movecraft.craft.CraftManager
import net.countercraft.movecraft.util.MathUtils.locationNearHitBox
import net.pdevita.creeperheal2.events.CHExplosionEvent
import org.bukkit.event.EventHandler

@AutoService(BaseCompatibility::class)
class Movecraft : BaseCompatibility {
    override val pluginName = "Movecraft"
    override val pluginPackage = "net.countercraft.movecraft.Movecraft"

    @EventHandler
    fun onCHExplosionEvent(event: CHExplosionEvent) {
        for (craft in CraftManager.getInstance().getCraftsInWorld(event.world)) {
            if (locationNearHitBox(craft.hitBox, event.boundary.center(), 50.0)) {
//                println("Found craft ${craft.name} near explosion, masking blocks")
                val itr = event.blockList.iterator()
                while (itr.hasNext()) {
                    val block = itr.next()
                    if (craft.hitBox.contains(block.x, block.y, block.z)) {
                        itr.remove()
//                        counter++
                    }
                }
            }
        }
    }
}
