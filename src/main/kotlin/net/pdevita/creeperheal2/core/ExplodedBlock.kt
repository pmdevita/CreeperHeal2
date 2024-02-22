package net.pdevita.creeperheal2.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random

private object SideFaces {
    val faces = java.util.ArrayList<BlockFace>(
        listOf(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.SOUTH
        )
    )
}

open class ExplodedBlock(protected var explosion: Explosion, val state: BlockState) {
    var dependencies = ArrayList<ExplodedBlock>()

    companion object {
        fun from(explosion: Explosion, state: BlockState): ExplodedBlock {
            return when {
                explosion.plugin.constants.dependentBlocks.sideBlocks.containsKey(state.block.blockData.material) -> {
                    SideExplodedBlock(explosion, state)
                }
                explosion.plugin.constants.gravityBlocks.contains(state.block.blockData.material) -> {
                    GravityExplodedBlock(explosion, state)
                }
                explosion.plugin.constants.multiBlocks.blocks.containsKey(state.block.blockData.material) -> {
                    MultiParentExplodedBlock(explosion, state)
                }
                else -> DefaultExplodedBlock(explosion, state)
            }
        }
    }

    fun relinkExplosion(newExplosion: Explosion) {
        this.explosion = newExplosion;
    }

    // Normal blocks don't have any parent blocks they rely on
    open fun getParentBlockLocation(): Location? {
        return null
    }

    // Does this block rely on the block in this location?
    open fun dependsOn(location: Location): Boolean {
        return getParentBlockLocation() == location
    }

    // Is this block's parent in the location list?
    open fun parentInExplosion(checkGravity: Boolean = false): Boolean {
        return false
    }

    // Add this block to its parent(s) dependencies
    open fun addToParents() {

    }

    // Find blocks that depend on this block
    // If this block is pulled out of the explosion, this is the list of blocks
    // next to it that also need to be removed.
    open fun findDependentBlocks(): LinkedList<ExplodedBlock> {
        val foundBlocks = LinkedList<ExplodedBlock>()
        // For all six faces
        for (face in SideFaces.faces) {
            val checkingBlock = this.state.block.getRelative(face)

            if (checkingBlock.state.blockData.material == Material.AIR) {
                continue
            }

            // Check if that block isn't already a part of the explosion
            if (!this.explosion.locations.containsKey(checkingBlock.location) && !this.explosion.gravityBlocks.contains(checkingBlock.location)) {
                val explodedBlock = ExplodedBlock.from(this.explosion, checkingBlock.state)
                // Check the block is side dependent on this block
                if (explodedBlock.dependsOn(this.state.location)) {
                    foundBlocks.add(explodedBlock)
//                    println("Block of type ${explodedBlock.state.blockData.material} is dependent on this block!")
                } else {
//                    println("Block of type ${explodedBlock.state.blockData.material} is not dependent on this block")
                }
            }
        }

        return foundBlocks
    }

    // Alert a block that it's parent was placed (used in some subtypes)
    open fun parentWasPlaced(parent: ExplodedBlock) {

    }

    // If a block requires special conditions for placement, this can be overridden to allow that to happen first
    open fun canBePlaced(): Boolean {
        return true
    }

    fun placeBlock() {
        val currentBlock = this.state.location.block

        // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
        if (currentBlock.blockData.material != Material.AIR) {
//            plugin.debugLogger("Breaking ${currentBlock.blockData.material} to place a block")
            currentBlock.breakNaturally()
        }
        this.state.update(true)

        // Get any entities inside t
        val entities = currentBlock.location.world?.getNearbyEntities(state.block.boundingBox)
        if (entities != null) {
            for (entity in entities) {
                // println("Checking to entity $entity at ${entity.location} out of the way of block ${state.blockData.material} ${state.block.boundingBox}")
                // println("Bounding box Height: ${entity.boundingBox.height} Max Y: ${entity.boundingBox.maxY} Min Y: ${entity.boundingBox.minY}")

                // If an entity has a Y equal to or greater than the placing block, we should probably teleport up
                // If there is enough space below this block, we can leave the entity there
                // println("bruh wtf ${entity.location.y >= state.location.y} ${hasValidSpaceBelow(state.location, entity)}")
                if (entity.location.y >= state.location.y || !hasValidSpaceBelow(state.location, entity)) {
                    val oneBlockUp = currentBlock.getRelative(BlockFace.UP).location.clone()
                    oneBlockUp.direction = entity.location.direction
                    // println("Teleporting $entity up")
                    entity.teleport(oneBlockUp)
                }
            }
        }

        // Play pop sound at the location of the new block
        this.state.location.world?.playSound(this.state.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .05F, .9F + Random.Default.nextFloat() * .3F)

        for (dependency in this.dependencies) {
            dependency.parentWasPlaced(this)
        }
    }

    private fun hasValidSpaceBelow(location: Location, entity: Entity): Boolean {
        // If an entity has their feet below the newly placed block, check if leaving them there will still be valid
        var entityHeight = entity.boundingBox.height
        // Players can be compressed into a height of 1
        if (entity is Player) {
            entityHeight = 1.0
        }
        val blocksBelow = ceil(entityHeight).toInt() + 1   // How many blocks below we need to check, with an extra just in case.
        var spaceBelow = 0.0
        println("Checking $blocksBelow blocks below...")
        for (i in 1..blocksBelow) {
            val locationBelow = location.clone()
            locationBelow.y -= i
            val blockBelow = locationBelow.block
            println("Bounding box for ${blockBelow.blockData.material} ${blockBelow.boundingBox} $locationBelow")
            if (blockBelow.blockData.material == Material.AIR) {
                spaceBelow += 1
            } else {
                println("final block gives ${locationBelow.y + 1 - blockBelow.boundingBox.maxY} of space")
                spaceBelow += locationBelow.y + 1 - blockBelow.boundingBox.maxY
                break
            }
        }
        return spaceBelow >= entityHeight
    }
}

class DefaultExplodedBlock(explosion: Explosion, state: BlockState): ExplodedBlock(explosion, state) {

}


open class SideExplodedBlock(explosion: Explosion, state: BlockState): ExplodedBlock(explosion, state) {
    override fun getParentBlockLocation(): Location? {
        return explosion.plugin.constants.dependentBlocks.sideBlocks[state.blockData.material]?.reorient(state)
    }

    override fun addToParents() {
        this.explosion.locations[getParentBlockLocation()]?.dependencies?.add(this)
    }

    override fun parentInExplosion(checkGravity: Boolean): Boolean {
        val parentLocation = getParentBlockLocation()
        // Block is in explosion locations and not in gravityBlocks (if checkGravity is enabled)
        return parentLocation in this.explosion.locations && !(checkGravity and this.explosion.gravityBlocks.contains(parentLocation))
    }
}

class GravityExplodedBlock(explosion: Explosion, state: BlockState): SideExplodedBlock(explosion, state) {
    override fun getParentBlockLocation(): Location {
        val newLocation = state.location.clone()
        newLocation.y = newLocation.y - 1
        return newLocation
    }
}

class MultiParentExplodedBlock(explosion: Explosion, state: BlockState) : ExplodedBlock(explosion, state) {
    val parents = HashSet<ExplodedBlock>()

    fun getDependentBlocksLocation(): List<Location>? {
//        println("multiblock has dependents ${explosion.plugin.constants.multiBlocks.blocks[state.blockData.material]?.getDependents(state)}")

        return explosion.plugin.constants.multiBlocks.blocks[state.blockData.material]?.getDependents(state)
    }

    fun getParentBlocksLocation(): List<Location>? {
        return explosion.plugin.constants.multiBlocks.blocks[state.blockData.material]?.getParents(state)
    }

    override fun dependsOn(location: Location): Boolean {
        return getDependentBlocksLocation()?.contains(location) ?: false
    }

    override fun addToParents() {
        getParentBlocksLocation()?.forEach {
            val parentBlock = this.explosion.locations[it]
            if (parentBlock != null) {
                parentBlock.dependencies.add(this)
                parents.add(parentBlock)
            }
        }
    }

    override fun parentInExplosion(checkGravity: Boolean): Boolean {
        val parentLocations = getParentBlocksLocation()
//        println("hiya, parentlocations for multiblock $parentLocations")
        // Block is in explosion locations and not in gravityBlocks (if checkGravity is enabled)
        if (parentLocations != null) {
            for (parentLocation in parentLocations) {
                if (parentLocation in this.explosion.locations && !(checkGravity and this.explosion.gravityBlocks.contains(parentLocation))) {
                    return true
                }
            }
        }
        return false
    }

    override fun parentWasPlaced(parent: ExplodedBlock) {
        parents.remove(parent)
    }

    override fun canBePlaced(): Boolean {
        return parents.isEmpty()
    }
}
