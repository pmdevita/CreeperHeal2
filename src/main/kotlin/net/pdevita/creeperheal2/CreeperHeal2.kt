package net.pdevita.creeperheal2

import net.pdevita.creeperheal2.commands.Commands
import net.pdevita.creeperheal2.config.ConfigManager
import net.pdevita.creeperheal2.constants.ConstantsManager
import net.pdevita.creeperheal2.core.Explosion
import net.pdevita.creeperheal2.core.ExplosionMapping
import net.pdevita.creeperheal2.core.Gravity
import net.pdevita.creeperheal2.events.Explode
import net.pdevita.creeperheal2.utils.Stats
import org.bukkit.block.Block
import org.bukkit.plugin.java.JavaPlugin

class CreeperHeal2 : JavaPlugin() {
    private val explosions: ArrayList<Explosion> = ArrayList()
    val gravity = Gravity(this)
    private var debug = false
    val constants = ConstantsManager(this)
    lateinit var settings: ConfigManager
    var stats: Stats? = null

    override fun onEnable() {
        super.onEnable()
        // Init config file
        saveDefaultConfig()
        reloadConfig()
        debug = config.getBoolean("debug")
        settings = ConfigManager(this, config)
        if (settings.general.bstats) {
            this.stats = Stats(this)
        }

        registerEvents()
        getCommand("ch")!!.setExecutor(Commands(this))
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(Explode(this), this)
    }

    fun createNewExplosion(blockList: List<Block>) {
        if (blockList.isEmpty()) {
            debugLogger("Explosion with no blocks")
            return
        }
        explosions.add(Explosion(this, blockList))
    }

    fun removeExplosion(explosion: Explosion) {
        explosions.remove(explosion)
    }

    fun debugLogger(message: String) {
        if (this.debug) {
            this.logger.info(message)
        }
    }

    fun warpExplosions() {
        this.debugLogger("Running warp")
        val itr = explosions.iterator()
        while (itr.hasNext()) {
            itr.next().warpReplaceBlocks()
            itr.remove()
        }
    }

    fun checkBoundaries() {
        // Check current explosions against each other to determine if they should be merged
        val newExplosions = ArrayList<ExplosionMapping>(explosions.map { ExplosionMapping(it) })
        debugLogger("Comparing ${newExplosions.size} explosions (${explosions.size})")
        for (i in 0 until newExplosions.size) {
            for (j in i+1 until newExplosions.size) {
                if (newExplosions[j] == newExplosions[i]) {
                    debugLogger("The explosions are the same! Don't merge!")
                    continue
                }
                val overlap = newExplosions[j].explosion.boundary?.let { newExplosions[i].explosion.boundary?.overlaps(it) }
                if (overlap == true && newExplosions[i].explosion.postProcessComplete.get() && newExplosions[j].explosion.postProcessComplete.get()) {
                    debugLogger("Merging explosions $i $j")

                    // Combine them (and cancel the originals) and create a new explosion mapping
                    val newExplosion = newExplosions[i].explosion + newExplosions[j].explosion
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
                } else {
                    debugLogger("Not merging $i and $j $overlap ${newExplosions[i].explosion.postProcessComplete.get()} ${newExplosions[j].explosion.postProcessComplete.get()}")
                }
            }
        }
        explosions.clear()
        explosions.addAll(newExplosions.map { it.explosion }.distinct())
    }

    override fun onDisable() {
        super.onDisable()
        // Quickly replace all blocks before shutdown
        val itr = explosions.iterator()
        while (itr.hasNext()) {
            itr.next().warpReplaceBlocks()
            itr.remove()
        }

    }
}

