package net.pdevita.creeperheal2.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Directional
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Bed
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

object Below: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        return state.block.getRelative(BlockFace.UP).location
    }
}

object OnTopOf: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        return state.block.getRelative(BlockFace.DOWN).location
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

object TopOrBottom: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        if (state.block.getRelative(BlockFace.UP).blockData.material != Material.AIR) {
            return state.block.getRelative(BlockFace.UP).location
        } else if (state.block.getRelative(BlockFace.DOWN).blockData.material != Material.AIR) {
            return state.block.getRelative(BlockFace.DOWN).location
        }
        return null
    }
}

object Bed:FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        val bed = state.blockData as Bed
        print(bed.facing)
        if (bed.part == Bed.Part.FOOT) {
            return state.block.getRelative(bed.facing).location
        } else if (bed.part == Bed.Part.HEAD) {
            return state.block.getRelative(bed.facing.oppositeFace).location
        }
        return null
    }
}

object Door:FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        val bisected = state.blockData as Bisected
//        print(">>>>>>>" + bisected.half)
        if (bisected.half == Bisected.Half.BOTTOM) {
            return state.block.getRelative(BlockFace.UP).location
        } else if (bisected.half == Bisected.Half.TOP) {
            return state.block.getRelative(BlockFace.DOWN).location
        }
        return null
    }
}


