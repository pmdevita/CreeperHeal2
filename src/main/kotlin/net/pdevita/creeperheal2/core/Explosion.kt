package net.pdevita.creeperheal2.core

import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.sync
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private object SideFaces {
    val faces = ArrayList<BlockFace>(listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH))
}

class Explosion() {
    lateinit var plugin: CreeperHeal2
    private val blockList = ArrayList<ExplodedBlock>()
    private val replaceList = LinkedList<ExplodedBlock>()
    private val gravityBlocks = ArrayList<Location>()
    private val locations = HashMap<Location, ExplodedBlock>()
    private var replaceJob: BukkitTask? = null
    private var cancelReplace = AtomicBoolean(false)


    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin

        val blockList = ArrayList<ExplodedBlock>()
        // First pass, get blocks and make changes to them individually
        for (block in initialBlockList) {
            val state = block.state
            val explodedBlock = ExplodedBlock(this, state)
            blockList.add(explodedBlock)
            locations[block.location] = explodedBlock
            // Clear containers since we keep inventory
            // Even though we are destroying the container block, this is still necessary for some reason
            if (state is Container) {
                plugin.logger.info("Container destroyed")
                state.inventory.clear()
            }
        }

        // Link dependent blocks to their parents
        for (block in blockList) {
//            plugin.logger.info(block.state.blockData.material.toString())
            // Block isn't dependent, can be replaced normally
            if (block.dependent == DependentType.NOT_DEPENDENT) {
                replaceList.add(block)
//                plugin.logger.info("Independent")
            } else {
                val parentLocation = block.getParentBlockLocation()
                val parentBlock = parentLocation?.let { locations[parentLocation] }
                if (parentBlock == null) {
                    // Parent is not part of explosion, this means it must already exist and block can be
                    // added at any point
                    replaceList.add(block)
//                    plugin.logger.info("Dependent, parent is not part of explosion")
                } else {
                    // Add block to it's parent's dependency list. Once the parent is added, it's dependencies will
                    // be added to the replaceList
                    parentBlock.dependencies.add(block)
//                    plugin.logger.info("Dependent")
//                    plugin.logger.info("${block.state.blockData.material} (${block.state.location}) is dependent on ${parentBlock.state.blockData.material} (${parentLocation})")
                }
            }
        }

        val secondaryList = ArrayList<ExplodedBlock>()

        // Third pass, prepare blocks around the exploded blocks
        // While we still have blocks to check
        while (blockList.isNotEmpty()) {
            for (explodedBlock in blockList) {
                val block = explodedBlock.state
                val upBlock = block.block.getRelative(BlockFace.UP)
                // First, check that this block is not already accounted for in the explosion
                if (!locations.containsKey(upBlock.location)) {
                    // If the above block is a gravity block, freeze it in place
                    if (plugin.constants.gravityBlocks.contains(upBlock.blockData.material)) {
                        gravityBlocks.add(upBlock.location)
                        locations[upBlock.location] = ExplodedBlock(this, block)
                        // Else, check if it is a top dependent block
                    } else if (plugin.constants.dependentBlocks.topBlocks.contains(upBlock.blockData.material)) {
                        val dependentBlock = ExplodedBlock(this, upBlock.state)
                        this.plugin.debugLogger("Found extra top dependent block ${dependentBlock.state.blockData.material}")
                        locations[upBlock.location] = dependentBlock
                        secondaryList.add(dependentBlock)
                        explodedBlock.dependencies.add(dependentBlock)
                    }
                }

                // Check side blocks
                val dependentBlocks = this.checkSides(explodedBlock)
                if (dependentBlocks.isNotEmpty()) {
                    for (dependentBlock in dependentBlocks) {
                        this.plugin.debugLogger("Found extra side dependent block ${dependentBlock.state.blockData.material}")
                        locations[dependentBlock.state.location] = dependentBlock
                    }
                    secondaryList.addAll(dependentBlocks)
                    explodedBlock.dependencies.addAll(dependentBlocks)
                }
            }
            this.blockList.addAll(blockList)
            blockList.clear()
            blockList.addAll(secondaryList)
            secondaryList.clear()
        }

        var blockstring = ""
        for (block in blockList) {
            blockstring += block.state.blockData.material.toString() + " "
        }
        plugin.logger.info(blockstring)

        // Ready to go, delete all the blocks
        deleteBlocks(replaceList)

        // Hand off the gravity blocks to be blocked
        plugin.gravity.addBlocks(gravityBlocks)
//        plugin.server.scheduler.runTaskLater(plugin, ReplaceLater(this), 100)
        replaceJob = sync(plugin, delayTicks = 100) {
            this.replaceBlocks()
        }
    }

    private fun checkSides(block: ExplodedBlock): ArrayList<ExplodedBlock> {
        val foundBlocks = ArrayList<ExplodedBlock>()
        // For all six faces
        for (face in SideFaces.faces) {
            val checkingBlock = block.state.block.getRelative(face)
            // Check if that block isn't already a part of the explosion
            if (!locations.containsKey(checkingBlock.location)) {
                val explodedBlock = ExplodedBlock(this, checkingBlock.state)
                // Check the block is side dependent
                if (explodedBlock.dependent == DependentType.SIDE_DEPENDENT) {
                    // Check the block is side dependent on this block
                    if (explodedBlock.getParentBlockLocation() == block.state.location) {
                        foundBlocks.add(explodedBlock)
                    }
                }
            }
        }
        return foundBlocks
    }

    private fun deleteBlocks(blockList: Collection<ExplodedBlock>) {
        // Delete blocks, accounting for dependencies first
        for (block in blockList) {
            if (block.dependencies.isNotEmpty()) {
                deleteBlocks(block.dependencies)
            }
            block.state.location.block.type = Material.AIR
            block.state.location.block.state.update(true, true)
        }
    }

    private fun replaceBlocks() {
        if (cancelReplace.get()) {
            plugin.logger.info("Canceling replacing (probably for warp)")
            return
        }

        plugin.logger.info("Replacing block")
        val currentBlock: Block

        if (replaceList.isNotEmpty()) {
            val block = replaceList.peek()
            currentBlock = block.state.location.block
            if (currentBlock.blockData.material != Material.AIR) {
                currentBlock.breakNaturally()
            }
            block.state.update(true)
            replaceList.addAll(block.dependencies)
            replaceList.remove()
        }

        if (replaceList.isEmpty()) {
            // Clean up
            plugin.gravity.removeBlocks(gravityBlocks)
            // Remove reference to self to be deleted
            plugin.removeExplosion(this)
        } else {
            // If this gets set to null, it's probably because we are warping the rest
            // Only continue if not null
            if (!cancelReplace.get()) {
                this.replaceJob = sync(plugin, delayTicks = 5) {
                    this.replaceBlocks()
                }
            } else {
                plugin.debugLogger("Cancelling normal replacement")
            }
        }
    }

    // Replace blocks ASAP (used to finish repairs if plugin is being disabled)
    fun warpReplaceBlocks(removeThis: Boolean = false) {
        plugin.debugLogger("Warp replacing...")
        cancelReplace.set(true)
        replaceJob?.cancel()

        while (replaceList.isNotEmpty()) {
            var block = replaceList.poll()
            plugin.debugLogger(block.state.blockData.material.toString())
            var currentBlock = block.state.location.block
            // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
            if (currentBlock.blockData.material != Material.AIR) {
                currentBlock.breakNaturally()
            }
            block.state.update(true)
            replaceList.addAll(block.dependencies)
        }

//        var blockstring = ""
//        for (block in replaceList) {
//            blockstring += block.state.blockData.material.toString() + " "
//        }
//        plugin.logger.info(blockstring)

        // Clean up
        plugin.gravity.removeBlocks(gravityBlocks)
        // Remove reference to self to be deleted
        if (removeThis) {
            plugin.removeExplosion(this)
        }
    }

}

