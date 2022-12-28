package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import net.countercraft.movecraft.craft.CraftManager
import net.countercraft.movecraft.util.MathUtils.locationNearHitBox
import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

object Movecraft : BaseCompatibility {
    override val pluginName = "Movecraft"
    override fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {
        var counter = 0
        for (craft in CraftManager.getInstance().getCraftsInWorld(world)) {
            if (locationNearHitBox(craft.hitBox, center, 50.0)) {
                println("Found craft ${craft.name} near explosion, masking blocks")
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
        println("Masked out $counter blocks from the explosion")
    }
}

@AutoService(BaseCompatibility::class)
class MovecraftLoaderProxy : BaseCompatibility by Movecraft
