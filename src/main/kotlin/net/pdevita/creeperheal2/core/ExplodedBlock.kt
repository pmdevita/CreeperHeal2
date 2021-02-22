package net.pdevita.creeperheal2.core

import org.bukkit.Location
import org.bukkit.block.BlockState

object DependentType {
    const val NOT_DEPENDENT = 0
//    const val TOP_DEPENDENT = 1
    const val SIDE_DEPENDENT = 2
    const val GRAVITY_DEPENDENT = 3
}

class ExplodedBlock(private val explosion: Explosion, val state: BlockState) {
    var dependent = DependentType.NOT_DEPENDENT
    var dependencies = ArrayList<ExplodedBlock>()
    init {
        // Is block top dependent?
        when {
            explosion.plugin.constants.dependentBlocks.sideBlocks.containsKey(state.block.blockData.material) -> {
                dependent = DependentType.SIDE_DEPENDENT
            }
            explosion.plugin.constants.gravityBlocks.contains(state.block.blockData.material) -> {
                dependent = DependentType.GRAVITY_DEPENDENT
            }
        }
    }

    // If this block is dependent, get the block it is dependent on/its parent
    fun getParentBlockLocation(): Location? {
        if (dependent == DependentType.GRAVITY_DEPENDENT) {
            val newLocation = state.location.clone()
            newLocation.y = newLocation.y - 1
            return newLocation
        } else if (dependent == DependentType.SIDE_DEPENDENT) {
            return explosion.plugin.constants.dependentBlocks.sideBlocks[state.blockData.material]?.reorient(state)
        }
        return null
    }
}