package net.pdevita.creeperheal2.core

import javafx.geometry.Side
import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container

private object SideFaces {
    val faces = ArrayList<BlockFace>(listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH))
}

class Explosion() {
    lateinit var plugin: CreeperHeal2
    val blockList = ArrayList<ExplodedBlock>()
    val replaceList = ArrayList<ExplodedBlock>()
    val gravityBlocks = ArrayList<Location>()
    val locations = HashMap<Location, ExplodedBlock>()

    constructor(plugin: CreeperHeal2, initialBlockList: List<Block>) : this() {
        this.plugin = plugin
        // Version extracting test, just here for convenience/testing
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        version = splitVersion[1]
        plugin.logger.info(version)

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
//        plugin.server.scheduler.runTaskLater(plugin, TestReplaceLater(this), 100)
        plugin.server.scheduler.runTaskLater(plugin, ReplaceLater(this), 100)
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

    private fun deleteBlocks(blockList: ArrayList<ExplodedBlock>) {
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