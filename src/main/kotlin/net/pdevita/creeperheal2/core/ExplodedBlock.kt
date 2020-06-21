package net.pdevita.creeperheal2.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.data.Directional
import kotlin.math.exp

object DependentType {
    const val NOT_DEPENDENT = 0
    const val TOP_DEPENDENT = 1
    const val SIDE_DEPENDENT = 2
}

class ExplodedBlock(private val explosion: Explosion, val state: BlockState) {
    var dependent = DependentType.NOT_DEPENDENT
    var dependencies = ArrayList<ExplodedBlock>()
    init {
        // Is block top dependent?
        if (explosion.plugin.constants.dependentBlocks.topBlocks.contains(state.block.blockData.material)) {
            dependent = DependentType.TOP_DEPENDENT
        } else if (explosion.plugin.constants.dependentBlocks.sideBlocks.containsKey(state.block.blockData.material)) {
            dependent = DependentType.SIDE_DEPENDENT
        }
    }

    fun getParentBlockLocation(): Location? {
        if (dependent == DependentType.TOP_DEPENDENT) {
            val newLocation = state.location.clone()
            newLocation.y = newLocation.y - 1
            return newLocation
        } else if (dependent == DependentType.SIDE_DEPENDENT) {
            return explosion.plugin.constants.dependentBlocks.sideBlocks[state.blockData.material]?.reorient(state)
        }
        return null
    }

}