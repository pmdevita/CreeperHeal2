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
import org.bukkit.block.data.type.Piston
import org.bukkit.block.data.type.Scaffolding

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
        // Try to attach to a vine above first
        if (state.block.getRelative(BlockFace.UP).blockData.material == Material.VINE) {
            return state.block.getRelative(BlockFace.UP).location
        }
        // Otherwise try attaching to one of it's attached faces
        val multipleFacing = state.blockData as MultipleFacing
        for (face in multipleFacing.faces) {
            if (state.block.getRelative(face).blockData.material != Material.AIR) {
                return state.block.getRelative(face).location
            }
        }
        // Otherwise attach to block above
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
        if (bisected.half == Bisected.Half.BOTTOM) {
            return state.block.getRelative(BlockFace.UP).location
        } else if (bisected.half == Bisected.Half.TOP) {
            return state.block.getRelative(BlockFace.DOWN).location
        }
        return null
    }
}

object Piston:FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        if (state.blockData is Piston) {
            val piston = state.blockData as Piston
            if (piston.isExtended) {
                return state.block.getRelative(piston.facing).location
            }
        }
        return null
    }
}

object Scaffolding:FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        if (state.blockData is Scaffolding) {
            val scaffolding = state.blockData as Scaffolding
            // Bottom is kinda weird, you would think it would be true
            // if it was on the bottom or was dependent on the below block
            if (scaffolding.isBottom) {
                // Find a nearby scaffolding that is a lower distance than this one
                for (blockFace in listOf(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)) {
                    val block = state.block.getRelative(blockFace)
                    if (block.blockData is Scaffolding) {
                        if ((block.blockData as Scaffolding).distance < scaffolding.distance) {
                            return block.location
                        }
                    }
                }
                return null
            } else {
                // It's on top of a block, return that block
                return state.block.getRelative(BlockFace.DOWN).location
            }
        }
        return null
    }
}


