package net.pdevita.creeperheal2.core

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container

class Explosion() {
    lateinit var plugin: CreeperHeal2
    val blockList = ArrayList<ExplodedBlock>()
    val replaceList = ArrayList<ExplodedBlock>()
    val gravityBlocks = ArrayList<Location>()
    val locations = HashMap<Location, ExplodedBlock>()

    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin
        // Version extracting test, just here for convenience
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        version = splitVersion[1]
        plugin.logger.info(version)
        // First pass, get blocks and make changes to them individually
        for (block in initialBlockList) {
            val state = block.state
            val explodedBlock = ExplodedBlock(this, state)
            blockList.add(explodedBlock)
            locations[block.location] = explodedBlock
            // Clear containers since we keep inventory
            if (state is Container) {
                plugin.logger.info("Container destroyed")
                state.inventory.clear()
            }
            // Don't update the first round so crops/redstone don't pop off
//            block.type = Material.AIR
        }

        // Link dependent blocks to their parents
        for (block in blockList) {
//            plugin.logger.info(block.state.blockData.material.toString())
            // Block isn't dependent, can be replaced normally
            if (block.dependent == DependentType.NOT_DEPENDENT) {
                replaceList.add(block)
//                plugin.logger.info("Independent")
            } else {
                var parentLocation = block.getParentBlockLocation()
                var parentBlock = parentLocation?.let { locations[parentLocation] }
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

        // Second pass, prepare blocks around the exploded blocks
        for (explodedBlock in blockList) {
            val block = explodedBlock.state
            // Check above for a GravityBlock
            val loc = Location(block.world, block.x.toDouble(), (block.y + 1).toDouble(), block.z.toDouble())
            if (plugin.constants.gravityBlocks.contains(loc.block.blockData.material) and !locations.containsKey(loc)) {
                plugin.logger.info("Found gravity block $loc")
                gravityBlocks.add(loc)
            }
            // Force block updates so unaffected blocks like liquids react
//            val replacedBlock = block.location.block;
//            replacedBlock.setType(Material.AIR)
//            block.location.block.state.update(true, true)
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
//        plugin.server.scheduler.runTaskLater(plugin, TestReplaceLater(this), 100)
        plugin.server.scheduler.runTaskLater(plugin, ReplaceLater(this), 100)
    }

    fun deleteBlocks(blockList: ArrayList<ExplodedBlock>) {
        // Delete blocks, accounting for dependencies first
        for (block in blockList) {
            if (block.dependencies.isNotEmpty()) {
                deleteBlocks(block.dependencies)
            }
            block.state.location.block.type = Material.AIR
            block.state.location.block.state.update(true, true)
        }
    }

    fun replaceBlocks() {
        plugin.logger.info("Replacing blocks")
        var currentBlock: Block
//        val iterator = replaceList.listIterator()
//        while (iterator.hasNext()) {
        if (replaceList.isNotEmpty()) {
            val block = replaceList[0]
            replaceList.removeAt(0)
            currentBlock = block.state.location.block
            if (currentBlock.blockData.material != Material.AIR) {
                currentBlock.breakNaturally()
            }
            block.state.update(true)
//            for (dependency in block.dependencies) {
//                iterator.add(dependency)
//            }
            replaceList.addAll(block.dependencies)
        }
//        for (block in blockList) {
//            block.update(true)
//        }
        if (replaceList.isEmpty()) {
            // Clean up
            plugin.gravity.removeBlocks(gravityBlocks)
            // Remove reference to self to be deleted
            plugin.removeExplosion(this)
        } else {
            plugin.server.scheduler.runTaskLater(plugin, ReplaceLater(this), 5)
        }
    }

    fun testReplaceBlocks() {
        plugin.logger.info("Test Replacing blocks")
        var currentBlock: Block
        for (block in blockList) {
            if (block.state.blockData.material == Material.WHEAT) {
                currentBlock = block.state.location.block
                if (currentBlock.blockData.material != Material.AIR) {
                    currentBlock.breakNaturally()
                }
                block.state.update(true, false)
            }
        }
    }

}

class TestReplaceLater(private var explosion: Explosion): Runnable {
    override fun run() {
        explosion.testReplaceBlocks()
    }
}

class ReplaceLater(private var explosion: Explosion): Runnable {
    override fun run() {
        explosion.replaceBlocks()
    }
}