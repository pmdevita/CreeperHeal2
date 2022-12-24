package net.pdevita.creeperheal2

import net.pdevita.creeperheal2.commands.Commands
import net.pdevita.creeperheal2.config.ConfigManager
import net.pdevita.creeperheal2.constants.ConstantsManager
import net.pdevita.creeperheal2.core.Explosion
import net.pdevita.creeperheal2.core.ExplosionManager
import net.pdevita.creeperheal2.core.Gravity
import net.pdevita.creeperheal2.events.Explode
import net.pdevita.creeperheal2.utils.Stats
import org.bukkit.block.Block
import org.bukkit.plugin.java.JavaPlugin

class CreeperHeal2 : JavaPlugin() {
    val gravity = Gravity(this)
    private var debug = false
    val constants = ConstantsManager()
    val manager = ExplosionManager(this)
    lateinit var settings: ConfigManager
    var stats: Stats? = null

    override fun onEnable() {
        super.onEnable()
        debug = config.getBoolean("debug")
        settings = ConfigManager(this)
        if (settings.general.bstats) {
            this.stats = Stats(this)
        }

        registerEvents()
        getCommand("creeperheal")!!.setExecutor(Commands(this))
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(Explode(this), this)
    }

    fun createNewExplosion(blockList: List<Block>): Explosion? {
        if (blockList.isEmpty()) {
//            debugLogger("Explosion with no blocks")
            return null
        }

        val newList = ArrayList<Block>()
        for (block in blockList) {
            if (settings.materials.allowMaterial(block.type))
                newList.add(block)
        }
        if (newList.isEmpty())
            return null

        val newExplosion = Explosion(this, newList)
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

