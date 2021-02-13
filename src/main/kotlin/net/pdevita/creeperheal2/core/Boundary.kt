package net.pdevita.creeperheal2.core

import kotlin.properties.Delegates

class Boundary (var highX: Int, var highY: Int, var highZ: Int, var lowX: Int, var lowY: Int, var lowZ: Int) {

    // Written like this to shave off a few instructions (Thanks Narelenus)
    private fun noIntersection(lowA: Int, highA: Int, lowB: Int, highB: Int): Boolean {
        return lowA > highB || highA < lowB
    }

    fun overlaps(other: Boundary): Boolean {
        return !(noIntersection(lowX, highX, other.lowX, other.highX)
                || noIntersection(lowY, highY, other.lowY, other.highY)
                || noIntersection(lowZ, highZ, other.lowZ, other.highZ))
    }

}