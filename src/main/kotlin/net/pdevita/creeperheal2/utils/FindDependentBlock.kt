package net.pdevita.creeperheal2.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.Directional
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Switch

interface FindDependentBlock {
    fun reorient(state: BlockState): Location?
}

object InFrontOf: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        if (state.blockData is Directional) {
            val directional = state.blockData as Directional
            return state.block.getRelative(directional.facing).location
        }
        return null
    }
}

object Behind: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        if (state.blockData is Directional) {
            val directional = state.blockData as Directional
            return state.block.getRelative(directional.facing.oppositeFace).location
        }
        return null
    }
}

object OnTopOf: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        return state.block.getRelative(BlockFace.UP).location
    }

}

object Vine: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        val multipleFacing = state.blockData as MultipleFacing
        for (face in multipleFacing.faces) {
            if (state.block.getRelative(face).blockData.material != Material.AIR) {
                return state.block.getRelative(face).location
            }
        }
        if (state.block.getRelative(BlockFace.UP).blockData.material != Material.AIR) {
            return state.block.getRelative(BlockFace.UP).location
        }
        return null
    }
}

object FaceAttachable: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        val faceAttachable = state.blockData as FaceAttachable
        return when (faceAttachable.attachedFace) {
            FaceAttachable.AttachedFace.CEILING -> state.block.getRelative(BlockFace.UP).location
            FaceAttachable.AttachedFace.FLOOR -> state.block.getRelative(BlockFace.DOWN).location
            FaceAttachable.AttachedFace.WALL -> Behind.reorient(state)
        }
    }
}


