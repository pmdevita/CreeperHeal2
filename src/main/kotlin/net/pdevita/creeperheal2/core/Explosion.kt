package net.pdevita.creeperheal2.core

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.data.MergeableLinkedList
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
import kotlin.collections.HashSet
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

private object SideFaces {
    val faces = ArrayList<BlockFace>(listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH))
}

class Explosion() {
    lateinit var plugin: CreeperHeal2
    // List of all blocks involved in explosion, dependents included
    val totalBlockList = MergeableLinkedList<ExplodedBlock>()
    private var replaceList = MergeableLinkedList<ExplodedBlock>()
    private val gravityBlocks = HashSet<Location>()
    private val locations = HashMap<Location, ExplodedBlock>()
    var boundary: Boundary? = null

    var postProcessComplete = AtomicBoolean(false)
    private var cancelReplace = AtomicBoolean(false)

    var replaceCounter = 0
    private var postProcessTask: Deferred<Unit>? = null
    private var delayJob: Deferred<Unit>? = null
    private var relinkJob: Job? = null
    private var sortJob: Deferred<Unit>? = null
    var isAdded = Mutex(true)


    // Initialize the class, create the finalBlockList of every single block that is affected in this explosion and link
    // it's dependencies
    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin

        // List of all blocks, including dependencies, in this stage of search
        // When we search out for other dependencies, this list will be cleared to hold the next layer
        var blockList = MergeableLinkedList<ExplodedBlock>()

        // First pass, get blocks and make changes to them individually
        for (block in initialBlockList) {
            val state = block.state
            val explodedBlock = ExplodedBlock(this, state)
            blockList.add(explodedBlock)
            locations[block.location] = explodedBlock
            // Clear containers since we keep inventory
            // Even though we are destroying the container block, this is still necessary for some reason
            if (state is Container) {
//                plugin.debugLogger("Container destroyed")
                if (state is Chest) {
                    state.blockInventory.clear()
                } else {
                    state.inventory.clear()
                }
            }

        }

//        var blockString = ""
//        for (block in blockList) {
//            blockString += block.state.blockData.material.toString() + " "
//        }
//        plugin.debugLogger("Initial block list $blockString")

        replaceList.addAll(MergeableLinkedList(blockList))

        // You can see the full commented version under relinkBlocks
        // This is duped because reusing the function would require needless async overhead
        // We have to block until this part is done otherwise we have two threads accessing
        // block dependencies
        // Second pass, link dependent blocks to their parents
//        val itr = replaceList.iterator()
//        while (itr.hasNext()) {
//            val block = itr.next()
//            // Block isn't dependent, can be replaced normally
//            if (block.dependent != DependentType.NOT_DEPENDENT) {
//                val parentLocation = block.getParentBlockLocation()
//                val parentBlock = parentLocation?.let { locations[parentLocation] }
//                if (parentBlock != null) {
//                    // Add block to it's parent's dependency list. Once the parent is added, it's dependencies will
//                    // be added to the replaceList
//                    parentBlock.dependencies.add(block)
//                    itr.remove()
//                }
//            }
//        }

        // Put the extra dependent blocks in here to differentiate them from the previous layer
        var secondaryList = MergeableLinkedList<ExplodedBlock>()

        // Third pass, prepare blocks around the exploded blocks
        // While we still have blocks to check
        while (blockList.isNotEmpty()) {
            // Check them
            for (explodedBlock in blockList) {
                val block = explodedBlock.state
                // A few different kinds of blocks could be above, check that first
                val upBlock = block.block.getRelative(BlockFace.UP)
                // First, check that the above block is not already accounted for in the explosion
                if (!locations.containsKey(upBlock.location) && !gravityBlocks.contains(upBlock.location)) {
                    // If the above block is a gravity block, freeze it in place
                    if (plugin.constants.gravityBlocks.contains(upBlock.blockData.material)) {
                        gravityBlocks.add(upBlock.location)
//                        locations[upBlock.location] = ExplodedBlock(this, block)
                    }
                }

                // Check for side-dependent blocks and add them to our dependencies
                val dependentBlocks = this.checkSides(explodedBlock)
                if (dependentBlocks.isNotEmpty()) {
                    for (dependentBlock in dependentBlocks) {
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
            totalBlockList.append(blockList)
            blockList = secondaryList
            secondaryList = MergeableLinkedList()

//            blockList = secondaryList
//            secondaryList = ArrayList<ExplodedBlock>()
        }

        val itr = replaceList.iterator()
        while (itr.hasNext()) {
            val block = itr.next()
            // Block is dependent, should be removed and linked to parent
            if (block.dependent != DependentType.NOT_DEPENDENT) {
                val parentLocation = block.getParentBlockLocation()
                val parentBlock = parentLocation?.let { locations[parentLocation] }
                if (parentBlock != null) {
                    // Add block to it's parent's dependency list. Once the parent is added, it's dependencies will
                    // be added to the replaceList
                    parentBlock.dependencies.add(block)
                    itr.remove()
                }
            }
        }

        // Print all blocks in the list
//        var blockString = ""
//        for (block in totalBlockList) {
//            blockString += block.state.blockData.material.toString() + " "
//        }
//        plugin.debugLogger(blockString)

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
        plugin: CreeperHeal2, totalBlockList: MergeableLinkedList<ExplodedBlock>, replaceList: MergeableLinkedList<ExplodedBlock>,
        gravityBlocks: HashSet<Location>, locations: HashMap<Location, ExplodedBlock>, boundary: Boundary? = null, replaceCounter: Int): this() {
        this.plugin = plugin
        this.totalBlockList.append(totalBlockList)
        this.gravityBlocks.addAll(gravityBlocks)
        this.locations.putAll(locations)
        this.replaceList = replaceList
        this.boundary = boundary
        this.replaceCounter = replaceCounter
        this.postProcessComplete.set(true)

        GlobalScope.launch(Dispatchers.async) {
            // Delay before starting heal
            delayJob = async(Dispatchers.async) {
                delay((plugin.settings.general.initialDelay * 1000).toLong())
            }

            linkBlocks(this@Explosion.replaceList)

            relinkJob!!.join()
            if (!this@Explosion.cancelReplace.get()) {
                sortJob = async(Dispatchers.async) {
                    this@Explosion.replaceList.sortBy { it.state.y  }
                }
            }
            delayJob!!.await()
            if (!this@Explosion.cancelReplace.get()) {
                newReplace()
            }
        }
    }

    private fun linkBlocks(blockList: MutableList<ExplodedBlock>) {
        relinkJob = GlobalScope.launch(Dispatchers.async) {
            // Second pass, link dependent blocks to their parents
            val itr = blockList.iterator()
            while (itr.hasNext()) {
                val block = itr.next()
                // Block isn't dependent, can be replaced normally
                if (block.dependent != DependentType.NOT_DEPENDENT) {
                    val parentLocation = block.getParentBlockLocation()
                    val parentBlock = parentLocation?.let { locations[parentLocation] }
                    if (parentBlock != null) {
                        // When relinking for a merged explosion, things are a little different
                        // First, gravityBlocks are a part of location list and we might have just decided to place it
                        // on there. Corroborate with the gravity list
                        if (gravityBlocks.contains(parentLocation)) {
                            // Leave in blockList as Independent
                        } else if (!parentBlock.dependencies.contains(block)) {
                            withContext(NonCancellable) {
                                // Move from blockList to it's parent dependency list
                                parentBlock.dependencies.add(block)
                                itr.remove()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calcBoundary(): Boundary? {
        val boundary = Boundary(0, 0, 0, 0, 0, 0)
        try {
            var location = totalBlockList.peek().state.location
            boundary.highX = location.blockX
            boundary.lowX = location.blockX
            boundary.highY = location.blockY
            boundary.lowY = location.blockY
            boundary.highZ = location.blockZ
            boundary.lowZ = location.blockZ

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
        } catch (e: java.lang.IndexOutOfBoundsException) {
            plugin.debugLogger("Couldn't calc boundaries because there are no blocks")
            return null
        }
//        plugin.debugLogger("Explosion within coords: ${boundary.highX}, ${boundary.highY}, ${boundary.highZ} and ${boundary.lowX}, ${boundary.lowY}, ${boundary.lowZ}")
        // Increase boundaries by one
        boundary.highX += 1
        boundary.highY += 1
        boundary.highZ += 1
        boundary.lowX -= 1
        boundary.lowY -= 1
        boundary.lowZ -= 1
        return boundary
    }


    private fun checkSides(block: ExplodedBlock): ArrayList<ExplodedBlock> {
        val foundBlocks = ArrayList<ExplodedBlock>()
        // For all six faces
        for (face in SideFaces.faces) {
            val checkingBlock = block.state.block.getRelative(face)
            // Check if that block isn't already a part of the explosion
            if (!locations.containsKey(checkingBlock.location) && !gravityBlocks.contains(checkingBlock.location)) {
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
//        if (foundBlocks.size > 0) {
//            var blockString = ""
//            for (block in foundBlocks) {
//                blockString += block.state.blockData.material.toString() + " "
//            }
//            plugin.debugLogger("Found extra blocks $blockString")
//        }
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
                delay((plugin.settings.general.initialDelay * 1000).toLong())
            }
            // Calc boundaries so we can combine with intersecting explosions
            val calcBoundary = async(Dispatchers.async) {
                this@Explosion.boundary = this@Explosion.calcBoundary()
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
                    if (replaceList.isEmpty()) {
                        // Cancel this explosion
                        plugin.debugLogger("Explosion was only TNT, deleting")
                        calcBoundary.cancel()
                        this@Explosion.cancel()
                        this@Explosion.deleteExplosion()
                    } else {
                        totalBlockList.removeAll { it.state.blockData.material == Material.TNT }
                    }
                }
            }


            this@Explosion.postProcessTask!!.await()
            calcBoundary.await()
            this@Explosion.postProcessComplete.set(true)
            if (!this@Explosion.cancelReplace.get()) {
                isAdded.withLock {
                    plugin.checkBoundaries()
                }
            }
            if (!this@Explosion.cancelReplace.get()) {
                sortJob = async(Dispatchers.async) {
                    this@Explosion.replaceList.sortBy { it.state.y  }
                }
            }
            delayJob!!.join()
            if (!this@Explosion.cancelReplace.get()) {
                newReplace()
            }
        }
    }

    // Async process to schedule block replacement
    private fun newReplace() {
        if (plugin.settings.general.turboThreshold > 0 && plugin.settings.general.turboThreshold < (totalBlockList.size - replaceCounter)) {
            plugin.debugLogger("Starting off repair in turbo")
        } else {
            plugin.debugLogger("Staring repair")
        }
        GlobalScope.launch(Dispatchers.async) {
            var replaceAmount = 1
//            replaceList.duplicateCheck()
            // Intermediate block list, mostly important for turboing
            val blocks = LinkedList<ExplodedBlock>()
            while (replaceList.isNotEmpty()) {
                // If turbo is enabled and we are over the threshold, turn it on
                replaceAmount = if (plugin.settings.general.turboThreshold > 0 && plugin.settings.general.turboThreshold < (totalBlockList.size - replaceCounter)) {
                    when (plugin.settings.general.turboType) {
                        0 -> plugin.settings.general.turboAmount
                        1 -> round((plugin.settings.general.turboPercentage / 100.0) * (totalBlockList.size - replaceCounter)).toInt().coerceIn(1..plugin.settings.general.turboCap)
                        else -> plugin.settings.general.turboAmount
                    }
                } else {
                    1
                }
                // Peek N number of blocks to replace
                val itr = replaceList.iterator()
                for (i in 0 until replaceAmount) {
                    if (itr.hasNext()) {
                        blocks.add(itr.next())
                    } else {
                        break
                    }
                }
//                plugin.debugLogger("Placing ${blocks.size} blocks")
                withContext(Dispatchers.minecraft) {
                    if (!cancelReplace.get()) {     // Only proceed if we aren't cancelling.
                        for (block in blocks) {
                            // Replace block
                            replaceBlock(block)
                            // Remove it from the replaceList
                            replaceList.poll()
                            // Dump its dependencies in
                            replaceList.addAll(block.dependencies)
                        }
                    }
                }
                // Increment the replaceCounter
                replaceCounter += blocks.size
                // Clear the intermediate block list
                blocks.clear()
                // If we are cancelling, exit, otherwise, keep going
                if (!cancelReplace.get()) {
                    delayJob = async(Dispatchers.async) {
                        delay((plugin.settings.general.betweenBlocksDelay / 20 * 1000).toLong())
                    }
                    delayJob!!.join()
                    // Delay is kinda long, could be good to check again
                    if (cancelReplace.get()) break
                } else {
                    break
                }
            }

            // Clean up
            // Remove reference to self to be deleted
//            if (!cancelReplace) {
                deleteExplosion()
                plugin.debugLogger("Finished repairing explosion")
//            }
        }
    }

    private fun deleteExplosion() {
        GlobalScope.launch(Dispatchers.minecraft) {
            plugin.gravity.removeBlocks(gravityBlocks)
            plugin.removeExplosion(this@Explosion)
        }
    }


    private fun replaceBlock(block: ExplodedBlock) {
        val currentBlock = block.state.location.block

        // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
        if (currentBlock.blockData.material != Material.AIR) {
//            plugin.debugLogger("Breaking ${currentBlock.blockData.material} to place a block")
            currentBlock.breakNaturally()
        } else {
            // If the block is air, teleport any entities in it up one to get them out of the way of the new block
            val entities = currentBlock.location.world?.getNearbyEntities(currentBlock.location, .4, .5, .4)
            if (entities != null) {
                for (entity in entities) {
                    val newLocation = currentBlock.getRelative(BlockFace.UP).location.clone()
                    newLocation.direction = entity.location.direction
                    entity.teleport(newLocation)
                }
            }
        }
        block.state.update(true)
        // Play pop sound at the location of the new block
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
        if (relinkJob != null) {
            runBlocking {
                relinkJob!!.join()
            }
        }
        if (sortJob != null) {
            runBlocking {
                sortJob!!.await()
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
                relinkJob!!.cancelAndJoin()
            }
        }
        if (sortJob != null) {
            runBlocking {
                sortJob!!.await()
            }
        }
    }

    operator fun plus(other: Explosion): Explosion {
        // Stop the original explosion objects from repairing
//        plugin.debugLogger("Adding two explosions")
        this.cancel()
        other.cancel()
        val totalBlockList = MergeableLinkedList<ExplodedBlock>()
        val gravityBlocks = ArrayList<Location>()
        val locations = HashMap<Location, ExplodedBlock>()
        val replaceList = MergeableLinkedList<ExplodedBlock>()
        val replaceCounter = this.replaceCounter + other.replaceCounter
        totalBlockList.append(this.totalBlockList)
        totalBlockList.append(other.totalBlockList)
        gravityBlocks.addAll(this.gravityBlocks)
        gravityBlocks.addAll(other.gravityBlocks)
        locations.putAll(this.locations)
        locations.putAll(other.locations)
        replaceList.append(this.replaceList)
        replaceList.append(other.replaceList)
//        replaceList.duplicateCheck()
        // Recalculate gravity
//        plugin.debugLogger("Finished merging basic data, cleaning gravity blocks")
        val removedGravity = getRemovedGravity(gravityBlocks, locations)
        plugin.gravity.removeBlocks(removedGravity)
        val gravityBlocksSet = HashSet<Location>(gravityBlocks)
        gravityBlocksSet.removeAll(removedGravity)
        var boundary: Boundary? = null
        if (this.boundary != null && other.boundary != null) {
            boundary = Boundary(max(this.boundary!!.highX, other.boundary!!.highX),
                max(this.boundary!!.highY, other.boundary!!.highY),
                max(this.boundary!!.highZ, other.boundary!!.highZ),
                min(this.boundary!!.lowX, other.boundary!!.lowX),
                min(this.boundary!!.lowY, other.boundary!!.lowY),
                min(this.boundary!!.lowZ, other.boundary!!.lowZ))
        }
//        plugin.debugLogger("Done, initializing from data")
        return Explosion(plugin, totalBlockList, replaceList, gravityBlocksSet, locations, boundary, replaceCounter)

    }

    private fun getRemovedGravity(gravityBlocks: ArrayList<Location>, locations: HashMap<Location, ExplodedBlock>): ArrayList<Location> {
        val removed = ArrayList<Location>()
        for (gravityBlock in gravityBlocks) {
            if (gravityBlock in locations) {
                removed.add(gravityBlock)
            }
        }
        return removed
    }

}

class ExplosionMapping(var explosion: Explosion) {
    val indices = ArrayList<Int>()
}
