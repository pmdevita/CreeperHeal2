package net.pdevita.creeperheal2

import net.pdevita.creeperheal2.commands.Commands
import net.pdevita.creeperheal2.compatibility.CompatibilityManager
import net.pdevita.creeperheal2.config.ConfigManager
import net.pdevita.creeperheal2.constants.ConstantsManager
import net.pdevita.creeperheal2.core.Explosion
import net.pdevita.creeperheal2.core.ExplosionManager
import net.pdevita.creeperheal2.core.Gravity
import net.pdevita.creeperheal2.listeners.Explode
import net.pdevita.creeperheal2.utils.Stats
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

/**
 * CreeperHeal2
 *
 * This is the main CreeperHeal2 plugin, which will be instantiated by Spigot. It's the main entry
 * point into accessing functions on the rest of the library.
 */
class CreeperHeal2 : JavaPlugin() {
    val gravity = Gravity(this)
    private var debug = false
    val constants = ConstantsManager(this)
    val manager = ExplosionManager(this)
    lateinit var settings: ConfigManager
    var stats: Stats? = null
    val compatibilityManager = CompatibilityManager(this)

    companion object {
        lateinit var instance: CreeperHeal2
    }

    init {
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        if (!File(config.currentPath).exists()) {
            saveDefaultConfig()
        }

        debug = config.getBoolean("debug")
        settings = ConfigManager(this)
        if (settings.general.bstats) {
            this.stats = Stats(this)
        }

        registerEvents()
        getCommand("creeperheal")!!.setExecutor(Commands(this))
        compatibilityManager.loadCompatibilityPlugins()
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(Explode(this), this)
    }

    /**
     * Takes a list of blocks and creates a new Explosion object.
     */
    fun createNewExplosion(blockList: List<Block>): Explosion? {
        if (blockList.isEmpty()) {
            return null
        }

        val newBlockList = LinkedList(blockList.filter { settings.blockList.allowMaterial(it.type) })

        if (newBlockList.isEmpty()) {
            return null
        }

        // Mask out blocks used in external plugins
        compatibilityManager.maskBlocksFromExplosion(newBlockList)

        if (newBlockList.isEmpty()) {
            return null
        }

        val newExplosion = Explosion(this, newBlockList)
        manager.add(newExplosion)
        return newExplosion
    }

    /**
     * Takes an entity and creates a new Explosion saving it. Only a few entity types are supported,
     * including Paintings, Hangings, and Armor Stands.
     */
    fun createNewExplosion(entity: Entity): Explosion {
        val newExplosion = Explosion(this, entities = listOf(entity))
        manager.add(newExplosion)
        return newExplosion
    }

    fun removeExplosion(explosion: Explosion) {
//        explosions.remove(explosion)
        manager.remove(explosion)
    }

    fun debugLogger(message: String) {
        if (this.debug) {
            this.logger.info(message)
        }
    }

    fun warpExplosions() {
        this.debugLogger("Running warp")
        manager.warpAll()
    }

    fun cancelExplosions() {
        manager.cancelAll()
    }

    fun checkBoundaries() {
        manager.merge()
    }

    override fun onDisable() {
        super.onDisable()
        // Quickly replace all blocks before shutdown
//        val itr = explosions.iterator()
//        while (itr.hasNext()) {
//            itr.next().warpReplaceBlocks()
//            itr.remove()
//        }
        manager.warpAll()
    }
}

