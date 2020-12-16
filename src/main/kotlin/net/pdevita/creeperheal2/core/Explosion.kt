package net.pdevita.creeperheal2.core

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.DispatcherContainer
import net.pdevita.creeperheal2.utils.async
import net.pdevita.creeperheal2.utils.minecraft
import net.pdevita.creeperheal2.utils.sync
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Bed
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.exp
import kotlin.random.Random

private object SideFaces {
    val faces = ArrayList<BlockFace>(listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH))
}

class Explosion() {
    lateinit var plugin: CreeperHeal2
    private val totalBlockList = ArrayList<ExplodedBlock>()
    private val replaceList = LinkedList<ExplodedBlock>()
    private val gravityBlocks = ArrayList<Location>()
    private val locations = HashMap<Location, ExplodedBlock>()

    private var postProcessComplete = AtomicBoolean(false)
    private var cancelReplace = AtomicBoolean(false)

    private var startDelay = 0;
    private var blockDelay = 0;
    private var postProcessTask: Deferred<Unit>? = null
    private var delayJob: Deferred<Unit>? = null


    // Initialize the class, create the finalBlockList of every single block that is affected in this explosion and link
    // it's dependencies
    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin
        this.startDelay = plugin.settings.general.initialDelay;
        this.blockDelay = plugin.settings.general.betweenBlocksDelay;

        // In case someone warps in the middle of preprocessing
        // Final full list of all independent blocks (with dependents linked to them)
        val finalBlockList = ArrayList<ExplodedBlock>()
        // List of all blocks, including dependencies, in this stage of search
        // When we search out for other dependencies, this list will be cleared to hold the next layer
        var blockList = ArrayList<ExplodedBlock>()

        // First pass, get blocks and make changes to them individually
        for (block in initialBlockList) {
            val state = block.state
            val explodedBlock = ExplodedBlock(this, state)
            blockList.add(explodedBlock)
            locations[block.location] = explodedBlock
            // Clear containers since we keep inventory
            // Even though we are destroying the container block, this is still necessary for some reason
            if (state is Container) {
                plugin.debugLogger("Container destroyed")
                if (state is Chest) {
                    state.blockInventory.clear()
                } else {
                    state.inventory.clear()
                }
            }
        }

        // Second pass, link dependent blocks to their parents
        for (block in blockList) {
            // Block isn't dependent, can be replaced normally
            if (block.dependent == DependentType.NOT_DEPENDENT) {
                finalBlockList.add(block)
            } else {
                val parentLocation = block.getParentBlockLocation()
                val parentBlock = parentLocation?.let { locations[parentLocation] }
                if (parentBlock == null) {
                    // Parent is not part of explosion, this means it must already exist and block can be
                    // added at any point
                    finalBlockList.add(block)
                } else {
                    // Add block to it's parent's dependency list. Once the parent is added, it's dependencies will
                    // be added to the replaceList
                    parentBlock.dependencies.add(block)
                }
            }
        }

        // Put the extra dependent blocks in here to differentiate them from the previous layer
        var secondaryList = ArrayList<ExplodedBlock>()

        // Third pass, prepare blocks around the exploded blocks
        // While we still have blocks to check
        while (blockList.isNotEmpty()) {
            // Check them
            for (explodedBlock in blockList) {
                val block = explodedBlock.state
                // A few different kinds of blocks could be above, check that first
                val upBlock = block.block.getRelative(BlockFace.UP)
                // First, check that the above block is not already accounted for in the explosion
                if (!locations.containsKey(upBlock.location)) {
                    // If the above block is a gravity block, freeze it in place
                    if (plugin.constants.gravityBlocks.contains(upBlock.blockData.material)) {
                        gravityBlocks.add(upBlock.location)
                        locations[upBlock.location] = ExplodedBlock(this, block)
                        // Else, check if it is a top dependent block, add it to the bottom's dependencies
                    } else if (plugin.constants.dependentBlocks.topBlocks.contains(upBlock.blockData.material)) {
                        val dependentBlock = ExplodedBlock(this, upBlock.state)
                        this.plugin.debugLogger("Found extra top dependent block ${dependentBlock.state.blockData.material}")
                        locations[upBlock.location] = dependentBlock
                        secondaryList.add(dependentBlock)
                        explodedBlock.dependencies.add(dependentBlock)
                    }
                }

                // Check for side-dependent blocks and add them to our dependencies
                val dependentBlocks = this.checkSides(explodedBlock)
                if (dependentBlocks.isNotEmpty()) {
                    for (dependentBlock in dependentBlocks) {
                        this.plugin.debugLogger("Found extra side dependent block ${dependentBlock.state.blockData.material}")
                        locations[dependentBlock.state.location] = dependentBlock
                    }
                    secondaryList.addAll(dependentBlocks)
                    explodedBlock.dependencies.addAll(dependentBlocks)
                }

                val multiBlockLocation = this.checkMultiBlock(explodedBlock)
                if (multiBlockLocation != null) {
                    val multiBlock = ExplodedBlock(this, multiBlockLocation.block.state)
                    this.plugin.debugLogger("MultiBlock ${multiBlock.state.blockData.material}")
                    locations[multiBlockLocation] = multiBlock
                    secondaryList.add(multiBlock)
                    explodedBlock.dependencies.add(multiBlock)
                }
            }
//            this.blockList.addAll(blockList)
            if (plugin.stats != null) {
                val numOfBlocks = blockList.size
                async(plugin) {
                    plugin.stats!!.totalBlocks.addAndGet(numOfBlocks)
                }
            }
            totalBlockList.addAll(blockList)
            blockList = secondaryList
            secondaryList = ArrayList()

//            blockList = secondaryList
//            secondaryList = ArrayList<ExplodedBlock>()
        }

//        var blockstring = ""
//        for (block in blockList) {
//            blockstring += block.state.blockData.material.toString() + " "
//        }
//        plugin.logger.info(blockstring)

        // Sort by Z and put in replaceList
        finalBlockList.sortBy { it.state.y }    // I think this needs to be done before removal in order to remove in the right order?
        replaceList.addAll(finalBlockList)

        // Ready to go, delete all the blocks
        deleteBlocks(replaceList)
        updateBlocks(replaceList)

        // Hand off the gravity blocks to be blocked
        plugin.gravity.addBlocks(gravityBlocks)
//        plugin.server.scheduler.runTaskLater(plugin, ReplaceLater(this), 100)
//            replaceJob = sync(plugin, delayTicks = plugin.settings.general.initialDelay * 20L) {
//                this.replaceBlocks()
//            }

        if (plugin.stats != null) {
            async(plugin) {
                plugin.stats!!.totalExplosions.incrementAndGet()
            }
        }
        postProcess()

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

    private fun checkMultiBlock(block: ExplodedBlock): Location? {
        if (plugin.constants.multiBlocks.blocks.containsKey(block.state.blockData.material)) {
            val location = plugin.constants.multiBlocks.blocks[block.state.blockData.material]?.reorient(block.state)
            if (!locations.containsKey(location)) {
                return location
            }
        }
        return null
    }

    private fun deleteBlocks(blockList: Collection<ExplodedBlock>) {
        // Delete blocks, accounting for dependencies first
        for (block in blockList) {
            if (block.dependencies.isNotEmpty()) {
                deleteBlocks(block.dependencies)
            }

//            if (plugin.constants.multiBlocks.blocks.containsKey(block.state.blockData.material)) {
                block.state.location.block.setType(Material.AIR, false)
                block.state.location.block.state.update(true, false)
//            } else {
//                block.state.location.block.type = Material.AIR
//                block.state.location.block.state.update(true, true)
//            }
        }
    }

    private fun updateBlocks(blockList: Collection<ExplodedBlock>) {
        for (block in blockList) {
            if (block.dependencies.isNotEmpty()) {
                updateBlocks(block.dependencies)
            }
            block.state.location.block.setType(Material.STONE, false)
            block.state.location.block.setType(Material.AIR, true)
            block.state.location.block.state.update(true, true)
        }
    }


    // Further processing on the block list after the initial block list creation
    private fun postProcess() {
        GlobalScope.launch(Dispatchers.async) {
            // Delay before starting heal
            delayJob = async(Dispatchers.async) {
                delay((startDelay * 1000).toLong())
            }
            // Post-processing on block list
            this@Explosion.postProcessTask = async(Dispatchers.async) {
                for (block in totalBlockList) {
                    if (block.state.type == Material.WEEPING_VINES_PLANT) {
                        block.state.type = Material.WEEPING_VINES
                    }
                }
            }
            this@Explosion.postProcessComplete.set(true)
            delayJob!!.join()
            this@Explosion.postProcessTask!!.await()
            if (!this@Explosion.cancelReplace.get()) {
                newReplace()
            }
        }
        plugin.debugLogger("returning from new replace")
    }

    // Async process to schedule block replacement
    private fun newReplace() {
        GlobalScope.launch(Dispatchers.async) {
            while (replaceList.isNotEmpty()) {
                val block = replaceList.peek()
                withContext(Dispatchers.minecraft) {
                    if (!cancelReplace.get()) {     // Only proceed if we aren't cancelling.
                        replaceBlock(block)
                        replaceList.poll()
                        replaceList.addAll(block.dependencies)
                    }
                }
                if (!cancelReplace.get()) {
                    delayJob = async(Dispatchers.async) {
                        delay((blockDelay / 20 * 1000).toLong())
                    }
                    delayJob!!.join()
                } else {
                    break
                }
            }

            // Clean up
            // Remove reference to self to be deleted
//            if (!cancelReplace) {
                plugin.gravity.removeBlocks(gravityBlocks)
                plugin.removeExplosion(this@Explosion)
//            }
        }
    }

    private fun replaceBlock(block: ExplodedBlock, warp: Boolean = false) {
        val currentBlock = block.state.location.block

        // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
        if (currentBlock.blockData.material != Material.AIR) {
            currentBlock.breakNaturally()
        } else {
            val entities = currentBlock.location.world?.getNearbyEntities(currentBlock.location, .4, .5, .4)
            if (entities != null) {
                for (entity in entities) {
                    entity.teleport(currentBlock.getRelative(BlockFace.UP).location)
                }
            }
        }
        block.state.update(true)
        block.state.location.world?.playSound(block.state.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .05F, .9F + Random.Default.nextFloat() * .3F)
    }



    // Replace blocks ASAP (used to finish repairs if plugin is being disabled)
    fun warpReplaceBlocks() {
        plugin.debugLogger("Warp replacing...")
        // Signal to async that it needs to stop and sync tasks that they should no longer proceed
        cancelReplace.set(true)
        // Block until post-processing is complete
        if (!postProcessComplete.get()) {
            runBlocking {
                postProcessTask?.await()
            }
        }


        while (replaceList.isNotEmpty()) {
            val block = replaceList.peek()
            replaceBlock(block)
            replaceList.poll()
            replaceList.addAll(block.dependencies)
        }

        // Clean up
        plugin.gravity.removeBlocks(gravityBlocks)

    }

}

