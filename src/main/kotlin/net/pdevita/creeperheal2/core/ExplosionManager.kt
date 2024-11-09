package net.pdevita.creeperheal2.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.async
import org.bukkit.Location
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ExplosionManager(val plugin: CreeperHeal2) {
    private val explosionsLock = ReentrantLock()
    private val explosions = ArrayList<Explosion>()
    private val addedExplosions = ConcurrentLinkedQueue<Explosion>()
    private val removedExplosions = ConcurrentLinkedQueue<Explosion>()

    internal fun add(explosion: Explosion) {
        addedExplosions.add(explosion)
        GlobalScope.launch (Dispatchers.async) {
            synchronized(addedExplosions) {
                if (addedExplosions.isNotEmpty()) {
                    explosionsLock.withLock {
//                        plugin.debugLogger("Adding ${addedExplosions.size} explosions from add queue")
                        while (addedExplosions.isNotEmpty()) {
                            val new = addedExplosions.poll()
                            explosions.add(new)
                            new.isAdded.unlock()
                        }
                    }
                }
            }
        }
    }

    internal fun remove(explosion: Explosion) {
        removedExplosions.add(explosion)
        GlobalScope.launch (Dispatchers.async) {
            synchronized(removedExplosions) {
                if (removedExplosions.isNotEmpty()) {
                    explosionsLock.withLock {
                        while (removedExplosions.isNotEmpty()) {
                            explosions.remove(removedExplosions.poll())
                        }
                    }
                }
            }
        }
    }

    fun size(): Int {
        explosionsLock.withLock {
            return explosions.size
        }
    }

    fun getExplosions(): ArrayList<Explosion> {
        explosionsLock.withLock {
            return ArrayList(explosions)
        }
    }


    fun warpAll() {
        explosionsLock.withLock {
            val itr = explosions.iterator()
            while (itr.hasNext()) {
                itr.next().warpReplaceBlocks()
                itr.remove()
            }
        }
    }

    fun cancelAll() {
        explosionsLock.withLock {
            val itr = explosions.iterator()
            while (itr.hasNext()) {
                itr.next().cancel()
                itr.remove()
            }
        }
    }

    internal fun merge() {
        explosionsLock.withLock {
            if (explosions.size < 2) {
                return@withLock
            }
            // Check current explosions against each other to determine if they should be merged
            val newExplosions = ArrayList<ExplosionMapping>(explosions.map { ExplosionMapping(it) })
            plugin.debugLogger("Comparing ${newExplosions.size} explosions")
            // Just to start the while loop
            var didMerge = true
            while (didMerge) {
                didMerge = false
                for (i in 0 until newExplosions.size) {
                    for (j in i + 1 until newExplosions.size) {
                        if (newExplosions[j] == newExplosions[i]) {
//                    debugLogger("The explosions are the same! Don't merge!")
                            continue
                        }
                        val overlap =
                            newExplosions[j].explosion.boundary?.let { newExplosions[i].explosion.boundary?.overlaps(it) }
                        if (overlap == true && newExplosions[i].explosion.postProcessComplete.get() && newExplosions[j].explosion.postProcessComplete.get()) {
                            plugin.debugLogger("Merging explosions $i $j")
                            didMerge = true
                            val a = newExplosions[i].explosion
                            val b = newExplosions[j].explosion

                            // Combine them (and cancel the originals) and create a new explosion mapping
                            val newExplosion = a + b
                            val newExplosionMapping = ExplosionMapping(newExplosion)


                            // Combine their index lists and add the new ones for our current indices
                            newExplosionMapping.indices.addAll(newExplosions[i].indices)
                            newExplosionMapping.indices.addAll(newExplosions[j].indices)
                            newExplosionMapping.indices.add(i)
                            newExplosionMapping.indices.add(j)

                            // Replace all matching indexes with this object
                            for (k in newExplosionMapping.indices) {
                                newExplosions[k] = newExplosionMapping
                            }
                        }
                    }
                }
            }
            explosions.clear()
            explosions.addAll(newExplosions.map { it.explosion }.distinct())
        }
    }

    fun isLocationInExplosion(loc: Location): Boolean {
        for (explosion in explosions) {
            if (explosion.boundary?.inBoundary(loc) == true) {
                return true;
            }
        }
        return false;
    }
}