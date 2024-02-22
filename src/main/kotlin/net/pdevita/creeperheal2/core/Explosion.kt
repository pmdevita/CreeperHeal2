package net.pdevita.creeperheal2.core

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.data.MergeableLinkedList
import net.pdevita.creeperheal2.utils.async
import net.pdevita.creeperheal2.utils.minecraft
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.data.type.BigDripleaf
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class Explosion() {
    lateinit var plugin: CreeperHeal2
    // List of all blocks involved in explosion, dependents included
    val totalBlockList = MergeableLinkedList<ExplodedBlock>()
    val entities = MergeableLinkedList<ExplodedEntity>()
    private var replaceList = MergeableLinkedList<ExplodedBlock>()
    val gravityBlocks = HashSet<Location>()
    val locations = HashMap<Location, ExplodedBlock>()
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
    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>? = null, entities: List<Entity>? = null) : this() {
        this.plugin = plugin

        if (initialBlockList != null) {
            // List of all blocks, including dependencies, in this stage of search
            // When we search out for other dependencies, this list will be cleared to hold the next layer
            var blockList = MergeableLinkedList<ExplodedBlock>()

            // First pass, get blocks and make changes to them individually
            for (block in initialBlockList) {
                val state = block.state
                val explodedBlock = ExplodedBlock.from(this, state)

                // Filter blocks based on the black/whitelist
                if (!plugin.settings.blockList.allowMaterial(state.type)) {
                    continue
                }

                // Clear containers since we keep inventory
                // Even though we are destroying the container block, this is still necessary for some reason
                if (state is Container) {
                    // If we are disabling container saving, empty its contents
                    if (plugin.settings.general.disableContainers) {
                        // loop through all the items in the inventory
                        for (item in state.inventory.contents) {
                            if (item == null) {
                                continue
                            }
                            // spawn the item naturally in the world at the blocks location
                            block.world.dropItemNaturally(block.location, item)
                            // Had a lot of trouble just calling inventory.clear() but this works
                            state.inventory.remove(item)
                        }
                    }
                    // Clear the inventory of the remaining container block in world
                    if (state is Chest) {
                        state.blockInventory.clear()
                    } else {
                        state.inventory.clear()
                    }
                }

                blockList.add(explodedBlock)
                locations[block.location] = explodedBlock

            }

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
                        // If the above block is a gravity block, freeze it in place rather than making it a
                        // dependent block
                        if (plugin.constants.gravityBlocks.contains(upBlock.blockData.material)) {
                            gravityBlocks.add(upBlock.location)
                        }
                    }

                    // Check for blocks that depend on this one and add them to our dependencies
                    val dependentBlocks = explodedBlock.findDependentBlocks()
                    if (dependentBlocks.isNotEmpty()) {
                        for (dependentBlock in dependentBlocks) {
                            locations[dependentBlock.state.location] = dependentBlock
                        }
                        secondaryList.addAll(dependentBlocks)
//                    explodedBlock.dependencies.addAll(dependentBlocks)
                    }
                }
//            this.blockList.addAll(blockList)
                // Add total blocks to Bstats
                if (plugin.stats != null) {
                    val numOfBlocks = blockList.size
                    async(plugin) {
                        plugin.stats!!.totalBlocks.addAndGet(numOfBlocks)
                    }
                }
                // The block staging looks like this
                // Currently found dependencies -> current blocks checking for dependencies -> Final list
                totalBlockList.append(blockList)
                blockList = secondaryList
                secondaryList = MergeableLinkedList()

//            blockList = secondaryList
//            secondaryList = ArrayList<ExplodedBlock>()
            }

            replaceList.addAll(totalBlockList)

            // Do final linking of dependencies
            val itr = replaceList.iterator()
            while (itr.hasNext()) {
                val block = itr.next()
                // Block is dependent, should be removed and linked to parent
                if (block !is DefaultExplodedBlock) {
                    // If the parent is in the explosion, we need to link this block to it
                    // If the parent is not in the explosion, it should already exist and so we don't need to worry
                    // about placing this block after it
//                println("Non-default block in list ${block.state.blockData.material}")
                    if (block.parentInExplosion()) {
                        // Attach to it
                        // Add block to it's parent's dependency list. Once the parent is added, it's dependencies will
                        // be added to the replaceList
//                    parentBlock.dependencies.add(block)
//                    println("Attaching to parent...")
                        block.addToParents()
                        itr.remove()
                    }
                }
            }
        }

        // Add any entities to the explosion
        entities?.forEach { this.entities.add(ExplodedEntity.from(it)) }

        // Ready to go, delete all the blocks
        deleteBlocks(replaceList)
        updateBlocks(replaceList)

        // Hand off the gravity blocks to be suspended
        plugin.gravity.addBlocks(gravityBlocks)

        if (plugin.stats != null) {
            async(plugin) {
                plugin.stats!!.totalExplosions.incrementAndGet()
            }
        }
        postProcess()
    }

    constructor(
        plugin: CreeperHeal2, totalBlockList: MergeableLinkedList<ExplodedBlock>,
        replaceList: MergeableLinkedList<ExplodedBlock>, gravityBlocks: HashSet<Location>,
        locations: HashMap<Location, ExplodedBlock>, boundary: Boundary? = null,
        replaceCounter: Int, entities: MergeableLinkedList<ExplodedEntity>): this() {
        this.plugin = plugin
        this.totalBlockList.append(totalBlockList)
        this.entities.append(entities)
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

            // Start the block linking job and wait
            linkBlocks(this@Explosion.replaceList)

            relinkJob!!.join()
            if (!this@Explosion.cancelReplace.get()) {
                sortJob = async(Dispatchers.async) {
                    this@Explosion.replaceList.sortBy { it.state.y  }
                }
            }
            delayJob!!.await()
            if (!this@Explosion.cancelReplace.get()) {
                replace()
            }
        }
    }

    private fun linkBlocks(blockList: MutableList<ExplodedBlock>) {
        relinkJob = GlobalScope.launch(Dispatchers.async) {
            // Relink each block so it knows which explosion it belongs to
            totalBlockList.forEach { it.relinkExplosion(this@Explosion) }

            // Second pass, link dependent blocks to their parents
            val itr = blockList.iterator()
            while (itr.hasNext()) {
                val block = itr.next()
                // Block isn't dependent, can be replaced normally
                if (block !is DefaultExplodedBlock) {
                    // If the parent is in the explosion, we need to link this block to it
                    // If the parent is not in the explosion, it should already exist and so we don't need to worry
                    // about placing this block after it
                    if (block.parentInExplosion()) {
                        // When relinking for a merged explosion, things are a little different
                        // First, gravityBlocks are a part of location list so this block's parent might currently
                        // be suspended in gravity.
                        if (block.parentInExplosion(checkGravity = true)) {
//                        if (gravityBlocks.contains(parentLocation)) {
                            // Parent is in suspended gravity and should already exist, don't worry about linking
//                        } else if (!parentBlock.dependencies.contains(block)) {
                            // This parent doesn't contain this block in its dependencies but it should, move from
                            // the main block list to its parent's dependency list.
                            // If this got cancelled, we would lose the block so it's wrapped as so.
                            withContext(NonCancellable) {
                                block.addToParents()
                                itr.remove()
                            }
                        }
                    }
                }
            }
        }
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
                this@Explosion.boundary = Boundary.from(totalBlockList, entities)
            }
            // Post-processing on block list
            this@Explosion.postProcessTask = async(Dispatchers.async) {
                // Fix Weeping Vines and Big Dripleafs which have some odd behavior
                // Could a better optimized system for this be made?
                if (plugin.constants.version.second >= 16) {
                    for (block in totalBlockList) {
                        if (block.state.type == Material.WEEPING_VINES_PLANT) {
                            block.state.type = Material.WEEPING_VINES
                        }
                    }
                }
                if (plugin.constants.version.second >= 17) {
                    for (block in totalBlockList) {
                        if (block.state.type == Material.BIG_DRIPLEAF) {
                            val data = block.state.blockData as BigDripleaf
                            data.tilt = BigDripleaf.Tilt.NONE
                            block.state.blockData = data
                        } else if (block.state.type == Material.BIG_DRIPLEAF_STEM) {
                            block.state.type = Material.BIG_DRIPLEAF
                        }
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
                    if (replaceList.isEmpty() && entities.isEmpty()) {
                        // Cancel this explosion
                        plugin.debugLogger("Explosion was only TNT and no entities, deleting")
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
                replace()
            }
        }
    }

    // Async process to schedule block replacement
    private fun replace() {
        GlobalScope.launch(Dispatchers.async) {
            if (plugin.settings.general.turboThreshold > 0 && plugin.settings.general.turboThreshold < (totalBlockList.size - replaceCounter)) {
                plugin.debugLogger("Starting off repair in turbo")
            } else {
                plugin.debugLogger("Staring repair")
            }
//            replaceList.duplicateCheck()
            // Intermediate block list, mostly important for turboing
            val blocks = LinkedList<ExplodedBlock>()
            while (replaceList.isNotEmpty()) {
                // If turbo is enabled and we are over the threshold, turn it on
                val replaceAmount = if (plugin.settings.general.turboThreshold > 0 && plugin.settings.general.turboThreshold < (totalBlockList.size - replaceCounter)) {
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
                            block.placeBlock()
                            // Remove it from the replaceList
                            replaceList.poll()
                            // Dump its dependencies in
                            block.dependencies.forEach { if (it.canBePlaced()) replaceList.add(it)  }
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

            // Replace entities
            while (entities.isNotEmpty()) {
                val entity = entities.peek()
                withContext(Dispatchers.minecraft) {
                    if (!cancelReplace.get()) {     // Only proceed if we aren't cancelling.
                        val result = entity.placeEntity()
                        val entity = entities.poll()
                        // If we were unable to place the entity, add it to the back of the list
                        if (!result) {
                            entities.add(entity)
                        }
                    }
                }
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
            // Pull out the next block from the list
            val block = replaceList.poll()
            // Replace block
            block.placeBlock()
            // Dump its dependencies in
            block.dependencies.forEach { if (it.canBePlaced()) replaceList.add(it)  }
        }

        while (entities.isNotEmpty()) {
            val entity = entities.poll()
            entity.placeEntity()
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
        // Create new lists to store merged data
        val totalBlockList = MergeableLinkedList<ExplodedBlock>()
        val gravityBlocks = ArrayList<Location>()
        val entities = MergeableLinkedList<ExplodedEntity>()
        val locations = HashMap<Location, ExplodedBlock>()
        val replaceList = MergeableLinkedList<ExplodedBlock>()
        val replaceCounter = this.replaceCounter + other.replaceCounter
        // And merge!
        totalBlockList.append(this.totalBlockList)
        totalBlockList.append(other.totalBlockList)
        entities.append(this.entities)
        entities.append(other.entities)
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
        val gravityBlocksSet = HashSet(gravityBlocks)
        gravityBlocksSet.removeAll(removedGravity.toSet())
        var boundary: Boundary? = null
        if (this.boundary != null && other.boundary != null) {
            boundary = Boundary(max(this.boundary!!.highX, other.boundary!!.highX),
                max(this.boundary!!.highY, other.boundary!!.highY),
                max(this.boundary!!.highZ, other.boundary!!.highZ),
                min(this.boundary!!.lowX, other.boundary!!.lowX),
                min(this.boundary!!.lowY, other.boundary!!.lowY),
                min(this.boundary!!.lowZ, other.boundary!!.lowZ),
                this.boundary!!.world
            )
        }
//        plugin.debugLogger("Done, initializing from data")
        return Explosion(plugin, totalBlockList, replaceList, gravityBlocksSet, locations, boundary, replaceCounter, entities)

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
