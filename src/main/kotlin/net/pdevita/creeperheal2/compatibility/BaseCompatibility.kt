package net.pdevita.creeperheal2.compatibility

import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

interface BaseCompatibilityCompanion {
    val pluginName: String
}


interface BaseCompatibility {
    val pluginName: String
    fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {

    }
}

//
//open class BaseCompatibility(val plugin: Plugin) {
//    companion object: BaseCompatibilityCompanion {
//        override val pluginName: String
//            get() = TODO("Not yet implemented")
//    }
//
//    open fun maskBlocksFromExplosion(blockList: MutableList<Block>, world: World, boundary: Boundary, center: Location) {
//
//    }
//}