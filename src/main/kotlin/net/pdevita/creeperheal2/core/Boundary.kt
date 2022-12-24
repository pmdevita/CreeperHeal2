package net.pdevita.creeperheal2.core

import net.pdevita.creeperheal2.data.MergeableLinkedList
import org.bukkit.util.BoundingBox


class Boundary (var highX: Int, var highY: Int, var highZ: Int, var lowX: Int, var lowY: Int, var lowZ: Int) {
    constructor(explosionBlocks: MergeableLinkedList<ExplodedBlock>) : this(0, 0, 0, 0, 0, 0) {
//        try {
            var location = explosionBlocks.peek().state.location
            this.highX = location.blockX
            this.lowX = location.blockX
            this.highY = location.blockY
            this.lowY = location.blockY
            this.highZ = location.blockZ
            this.lowZ = location.blockZ

            for (block in explosionBlocks) {
                location = block.state.location
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
//        } catch (e: java.lang.IndexOutOfBoundsException) {
//            plugin.debugLogger("Couldn't calc boundaries because there are no blocks")
//            return null
//        }
//        plugin.debugLogger("Explosion within coords: ${this.highX}, ${this.highY}, ${this.highZ} and ${this.lowX}, ${this.lowY}, ${this.lowZ}")
        // Increase boundaries by one
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
    }

    fun toBoundingBox(): BoundingBox {
        return BoundingBox(lowX.toDouble(), lowY.toDouble(), lowZ.toDouble(), highX.toDouble(), highY.toDouble(), highZ.toDouble())
    }

}