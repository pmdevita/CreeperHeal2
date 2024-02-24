package net.pdevita.creeperheal2.core

import net.pdevita.creeperheal2.data.MergeableLinkedList
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.BoundingBox


class Boundary (var highX: Int, var highY: Int, var highZ: Int, var lowX: Int, var lowY: Int, var lowZ: Int, var world: World) {

    companion object {
        fun from(explosionBlocks: MergeableLinkedList<ExplodedBlock>, explosionEntities: MergeableLinkedList<ExplodedEntity>): Boundary {
            val firstLocation = if (explosionBlocks.size > 0) {
                explosionBlocks.peek().state.location
            } else {
                explosionEntities.peek().location
            }
            return Boundary(firstLocation, explosionBlocks, explosionEntities)
        }
    }

    constructor(initialLocation: Location, explosionBlocks: MergeableLinkedList<ExplodedBlock>, explosionEntities: MergeableLinkedList<ExplodedEntity>) :
            this(initialLocation.blockX, initialLocation.blockY, initialLocation.blockZ, initialLocation.blockX, initialLocation.blockY, initialLocation.blockZ, initialLocation.world!!) {
        addLocations(explosionBlocks)
        addLocations(explosionEntities)
        increaseBoundaries()
    }


    constructor(explosionBlocks: MergeableLinkedList<ExplodedBlock>) : this(0, 0, 0, 0, 0, 0, explosionBlocks.peek().state.world) {
        val location = explosionBlocks.peek().state.location
        this.highX = location.blockX
        this.lowX = location.blockX
        this.highY = location.blockY
        this.lowY = location.blockY
        this.highZ = location.blockZ
        this.lowZ = location.blockZ

        addLocations(explosionBlocks)
        increaseBoundaries()
    }

    constructor(blockList: List<Block>) : this(0, 0, 0, 0, 0, 0, blockList.first().world) {
        setInitialLocation(blockList.first().state.location)
        addLocations(blockList)
        increaseBoundaries()
    }

   private  fun addLocations(blockList: List<Block>) {
        for (block in blockList) {
            addLocation(block.state.location)
        }
    }

    @JvmName("addLocationsForExplodedBlocks")
    private fun addLocations(blockList: List<ExplodedBlock>) {
        for (block in blockList) {
            addLocation(block.state.location)
        }
    }

    private fun addLocations(entityList: MergeableLinkedList<ExplodedEntity>) {
        for (entity in entityList) {
            addLocation(entity.location)
        }
    }

    private fun addLocation(location: Location) {
        if (this.highX < location.blockX) {
            this.highX = location.blockX
        } else if (this.lowX > location.blockX) {
            this.lowX = location.blockX
        }
        if (this.highY < location.blockY) {
            this.highY = location.blockY
        } else if (this.lowY > location.blockY) {
            this.lowY = location.blockY
        }
        if (this.highZ < location.blockZ) {
            this.highZ = location.blockZ
        } else if (this.lowZ > location.blockZ) {
            this.lowZ = location.blockZ
        }
    }

    private fun setInitialLocation(location: Location) {
        this.highX = location.blockX
        this.lowX = location.blockX
        this.highY = location.blockY
        this.lowY = location.blockY
        this.highZ = location.blockZ
        this.lowZ = location.blockZ
    }

    private fun increaseBoundaries() {
        // Increase boundaries by one so we intersect neighboring explosions
        this.highX += 1
        this.highY += 1
        this.highZ += 1
        this.lowX -= 1
        this.lowY -= 1
        this.lowZ -= 1
    }

    // Written like this to shave off a few instructions (Thanks Narelenus)
    private fun noIntersection(lowA: Int, highA: Int, lowB: Int, highB: Int): Boolean {
        return lowA > highB || highA < lowB
    }

    fun overlaps(other: Boundary): Boolean {
        return !(noIntersection(lowX, highX, other.lowX, other.highX)
                || noIntersection(lowY, highY, other.lowY, other.highY)
                || noIntersection(lowZ, highZ, other.lowZ, other.highZ))
                && world == other.world
    }

    fun toBoundingBox(): BoundingBox {
        return BoundingBox(lowX.toDouble(), lowY.toDouble(), lowZ.toDouble(), highX.toDouble(), highY.toDouble(), highZ.toDouble())
    }

    fun centerAsVector(): Triple<Int, Int, Int> {
        return Triple(
            ((highX-lowX)/2)+lowX,
            ((highY-lowY)/2)+lowY,
            ((highZ-lowZ)/2)+lowZ,
        )
    }
    fun center(): Location {
        return Location(
            world,
            ((highX-lowX)/2.0)+lowX,
            ((highY-lowY)/2.0)+lowY,
            ((highZ-lowZ)/2.0)+lowZ,
        )
    }

    override fun toString(): String {
        return "Boundary($lowX, $lowY, $lowZ - $highX, $highY, $highZ)"
    }

}