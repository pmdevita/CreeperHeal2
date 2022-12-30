package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import net.countercraft.movecraft.craft.CraftManager
import net.countercraft.movecraft.util.MathUtils.locationNearHitBox
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

object Movecraft : BaseCompatibility {
    override val pluginName = "Movecraft"
    lateinit var creeperHeal: CreeperHeal2

    override fun setCreeperHealReference(creeperHeal2: CreeperHeal2) {
        creeperHeal = creeperHeal2
    }

    override fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {
        var counter = 0
        this.creeperHeal.debugLogger("Checking Movecraft for explosion at $center")
        for (craft in CraftManager.getInstance().getCraftsInWorld(world)) {
            this.creeperHeal.debugLogger("Checking against craft ${craft.name} at ${craft.hitBox.midPoint}")
            if (locationNearHitBox(craft.hitBox, center, 50.0)) {
                this.creeperHeal.debugLogger("Found craft ${craft.name} near explosion, masking blocks")
                val itr = blockList.iterator()
                while (itr.hasNext()) {
                    val block = itr.next()
                    if (craft.hitBox.contains(block.x, block.y, block.z)) {
                        itr.remove()
                        counter++
                    }
                }
            }
        }
        this.creeperHeal.debugLogger("Masked out $counter blocks from the explosion")
    }
}

@AutoService(BaseCompatibility::class)
class MovecraftLoaderProxy : BaseCompatibility by Movecraft
