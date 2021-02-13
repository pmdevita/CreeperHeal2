package net.pdevita.creeperheal2.core

import kotlinx.coroutines.*
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.async
import net.pdevita.creeperheal2.utils.minecraft
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Container
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private object SideFaces {
    val faces = ArrayList<BlockFace>(listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH))
}

class Explosion() {
    lateinit var plugin: CreeperHeal2
    // List of all blocks involved in explosion, dependents included
    private val totalBlockList = ArrayList<ExplodedBlock>()
    private val replaceList = LinkedList<ExplodedBlock>()
    private val gravityBlocks = ArrayList<Location>()
    private val locations = HashMap<Location, ExplodedBlock>()
    var boundary: Boundary? = null

    var postProcessComplete = AtomicBoolean(false)
    private var cancelReplace = AtomicBoolean(false)

    private var startDelay = 0
    private var blockDelay = 0
    private var postProcessTask: Deferred<Unit>? = null
    private var delayJob: Deferred<Unit>? = null
    private var relinkJob: Deferred<Unit>? = null


    // Initialize the class, create the finalBlockList of every single block that is affected in this explosion and link
    // it's dependencies
    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin
        this.startDelay = plugin.settings.general.initialDelay
        this.blockDelay = plugin.settings.general.betweenBlocksDelay

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
        // Final full list of all independent blocks (with dependents linked to them)
        val finalBlockList = linkBlocks(blockList)

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

        // Print all blocks in the list
//        var blockstring = ""
//        for (block in blockList) {
//            blockstring += block.state.blockData.material.toString() + " "
//        }
//        plugin.logger.info(blockstring)

        // Sort by Y and put in replaceList
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

    constructor(
        plugin: CreeperHeal2, totalBlockList: ArrayList<ExplodedBlock>, replaceList: ArrayList<ExplodedBlock>,
        gravityBlocks: ArrayList<Location>, locations: HashMap<Location, ExplodedBlock>, boundary: Boundary? = null): this() {
        this.plugin = plugin
        this.totalBlockList.addAll(totalBlockList)
        this.gravityBlocks.addAll(gravityBlocks)
        this.locations.putAll(locations)
        this.boundary = boundary
        this.postProcessComplete.set(true)
        this.startDelay = plugin.settings.general.initialDelay
        this.blockDelay = plugin.settings.general.betweenBlocksDelay

        GlobalScope.launch(Dispatchers.async) {
            // Delay before starting heal
            delayJob = async(Dispatchers.async) {
                delay((startDelay * 1000).toLong())
            }
            this@Explosion.relinkJob = async(Dispatchers.async) {
                println("Replacelist before ${this@Explosion.replaceList.size}")
                this@Explosion.replaceList.addAll(linkBlocks(replaceList, true).sortedBy { it.state.y })
                println("Relinked blocks in merged explosion, ${this@Explosion.replaceList.size} total root parents")
            }
            relinkJob!!.await()
            delayJob!!.await()
            if (!this@Explosion.cancelReplace.get()) {
                println("Replacing from inside a merged explosion")
                newReplace()
            }
        }
    }

    private fun linkBlocks(blockList: List<ExplodedBlock>, avoidDuplicates: Boolean = false): ArrayList<ExplodedBlock> {
        val finalBlockList = ArrayList<ExplodedBlock>()

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
                    if (avoidDuplicates) {
                        // When relinking for a merged explosion, things are a little different
                        // First, gravityBlocks are a part of location list and we might have just decided to place it
                        // on there. Corroborate with the gravity list
                        if (gravityBlocks.contains(parentLocation)) {
                            finalBlockList.add(block)
                        } else if (!parentBlock.dependencies.contains(block)) {
                            parentBlock.dependencies.add(block)
                        }
                    } else {
                        // Normal linking, duplicate check would never be true and just waste time here
                        parentBlock.dependencies.add(block)
                    }
                }
            }
        }
        return finalBlockList

    }

    private fun calcBoundary(): Boundary {
        println("Calculating boundary with ${totalBlockList.size} blocks")
        var location = totalBlockList[0].state.location
        val boundary = Boundary(location.blockX, location.blockY, location.blockZ,
            location.blockX, location.blockY, location.blockZ)
        for (block in totalBlockList) {
            location = block.state.location
            if (boundary.highX < location.blockX) {
                boundary.highX = location.blockX
            } else if (boundary.lowX > location.blockX) {
                boundary.lowX = location.blockX
            }
            if (boundary.highY < location.blockY) {
                boundary.highY = location.blockY
            } else if (boundary.lowY > location.blockY) {
                boundary.lowY = location.blockY
            }
            if (boundary.highZ < location.blockZ) {
                boundary.highZ = location.blockZ
            } else if (boundary.lowZ > location.blockZ) {
                boundary.lowZ = location.blockZ
            }
        }
        plugin.debugLogger("Explosion within coords: ${boundary.highX}, ${boundary.highY}, ${boundary.highZ} and ${boundary.lowX}, ${boundary.lowY}, ${boundary.lowZ}")
        return boundary
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
            // If TNT exploding is on, don't remove the TNT
            if (block.state.blockData.material == Material.TNT && plugin.settings.general.explodeTNT) {
                continue
            }
            block.state.location.block.setType(Material.AIR, false)
            block.state.location.block.state.update(true, false)
        }
    }

    private fun updateBlocks(blockList: Collection<ExplodedBlock>) {
        for (block in blockList) {
            if (block.dependencies.isNotEmpty()) {
                updateBlocks(block.dependencies)
            }
            // If TNT exploding is on, don't update the TNT
            if (block.state.blockData.material == Material.TNT && plugin.settings.general.explodeTNT) {
                continue
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
                // Fix Weeping Vine plants being weird
                for (block in totalBlockList) {
                    if (block.state.type == Material.WEEPING_VINES_PLANT) {
                        block.state.type = Material.WEEPING_VINES
                    }
                }
                // If TNT exploding is on, remove the TNT and dump it's dependents back into the tree
                if (plugin.settings.general.explodeTNT) {
                    val itr = replaceList.iterator()
                    val newBlocks = LinkedList<ExplodedBlock>() // Store dependencies here intermittently
                    while (itr.hasNext()) {
                        val block = itr.next()
                        if (block.state.blockData.material == Material.TNT) {
                            newBlocks.addAll(block.dependencies)
                            itr.remove()
                        }
                    }
                    replaceList.addAll(newBlocks)
                    totalBlockList.removeAll { it.state.blockData.material == Material.TNT }
                }
            }
            // Calc boundaries so we can combine with intersecting explosions
            val calcBoundary = async(Dispatchers.async) {
                this@Explosion.boundary = this@Explosion.calcBoundary()
//                if (plugin.debug) {
//                    val world = totalBlockList[0].state.world
//                    withContext(Dispatchers.minecraft) {
//                        var location = Location(world, boundary.highX.toDouble(), boundary.highY.toDouble(), boundary.highZ.toDouble())
//                        location.block.type = Material.GOLD_BLOCK
//                        location = Location(world, boundary.lowX.toDouble(), boundary.lowY.toDouble(), boundary.lowZ.toDouble())
//                        location.block.type = Material.GOLD_BLOCK
//                    }
//                }
            }

            this@Explosion.postProcessTask!!.await()
            calcBoundary.await()
            this@Explosion.postProcessComplete.set(true)
            async(Dispatchers.minecraft) {
                plugin.checkBoundaries()
            }
            delayJob!!.join()
            println("Postprocess and delay done, cancelReplace is ${cancelReplace.get()}")
            if (!this@Explosion.cancelReplace.get()) {
                newReplace()
            }
        }
        plugin.debugLogger("returning from new replace")
    }

    // Async process to schedule block replacement
    private fun newReplace() {
        println("Starting async replacement with ${replaceList.size} in replaceList")
        GlobalScope.launch(Dispatchers.async) {
            while (replaceList.isNotEmpty()) {
                val block = replaceList.peek()
                withContext(Dispatchers.minecraft) {
                    if (!cancelReplace.get()) {     // Only proceed if we aren't cancelling.
                        replaceBlock(block)
                        replaceList.poll()
//                        if (block.dependencies.isNotEmpty()) {
//                            plugin.debugLogger("Merged in ${block.dependencies.size} dependencies")
//                        }
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

    private fun replaceBlock(block: ExplodedBlock) {
        val currentBlock = block.state.location.block

        // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
        if (currentBlock.blockData.material != Material.AIR) {
            plugin.debugLogger("Breaking a block to place a block")
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
            if (block.dependencies.isNotEmpty()) {
                plugin.debugLogger("Merged in ${block.dependencies.size} dependencies")
            }
            replaceList.addAll(block.dependencies)
        }

        // Clean up
        plugin.gravity.removeBlocks(gravityBlocks)

    }

    fun cancel() {
        cancelReplace.set(true)
        relinkJob?.cancel()
        // Block until post-processing is complete
        if (!postProcessComplete.get()) {
            runBlocking {
                postProcessTask?.await()
            }
        }
        if (relinkJob != null) {
            runBlocking {
                relinkJob!!.cancel()
            }
        }
        println("Cancelled ${cancelReplace.get()}")
    }

    operator fun plus(other: Explosion): Explosion {
        // Stop the original explosion objects from repairing
        this.cancel()
        other.cancel()
        val totalBlockList = ArrayList<ExplodedBlock>()
        val gravityBlocks = ArrayList<Location>()
        val locations = HashMap<Location, ExplodedBlock>()
        val replaceList = ArrayList<ExplodedBlock>()
        totalBlockList.addAll(this.totalBlockList)
        totalBlockList.addAll(other.totalBlockList)
        gravityBlocks.addAll(this.gravityBlocks)
        gravityBlocks.addAll(other.gravityBlocks)
        locations.putAll(this.locations)
        locations.putAll(other.locations)
        replaceList.addAll(this.replaceList)
        replaceList.addAll(other.replaceList)
        // Recalculate gravity blocks
        val removedGravity = getRemovedGravity(gravityBlocks, totalBlockList)
        plugin.gravity.removeBlocks(removedGravity)
        gravityBlocks.removeAll(removedGravity)
        var boundary: Boundary? = null
        if (this.boundary != null && other.boundary != null) {
            boundary = Boundary(max(this.boundary!!.highX, other.boundary!!.highX),
                max(this.boundary!!.highY, other.boundary!!.highY),
                max(this.boundary!!.highZ, other.boundary!!.highZ),
                min(this.boundary!!.lowX, other.boundary!!.lowX),
                min(this.boundary!!.lowY, other.boundary!!.lowY),
                min(this.boundary!!.lowZ, other.boundary!!.lowZ))
        }

        return Explosion(plugin, totalBlockList, replaceList, gravityBlocks, locations, boundary)

    }

    fun getRemovedGravity(gravityBlocks: ArrayList<Location>, totalBlockList: ArrayList<ExplodedBlock>): ArrayList<Location> {
        val removed = ArrayList<Location>()
        for (gravityBlock in gravityBlocks) {
            var contains = false
            for (block in totalBlockList) {
                if (block.state.location == gravityBlock) {
                    contains = true
                    break
                }
            }
            if (contains) {
                removed.add(gravityBlock)
            }
        }
        return removed
    }

}

class ExplosionMapping(var explosion: Explosion) {
    val indices = ArrayList<Int>()
}
