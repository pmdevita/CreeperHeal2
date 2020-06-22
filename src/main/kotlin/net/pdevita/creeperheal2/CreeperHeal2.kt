package net.pdevita.creeperheal2

import net.pdevita.creeperheal2.constants.ConstantsManager
import net.pdevita.creeperheal2.core.Explosion
import net.pdevita.creeperheal2.core.Gravity
import net.pdevita.creeperheal2.events.EntityExplode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.plugin.java.JavaPlugin

class CreeperHeal2 : JavaPlugin {
    private val explosions: MutableList<Explosion> = ArrayList()
    val gravity = Gravity(this)
    var debug = false
    lateinit var constants: ConstantsManager

    constructor() : super()

    override fun onEnable() {
        super.onEnable()
        // Init config file
        saveDefaultConfig()
        reloadConfig()
        debug = config.getBoolean("debug")

        constants = ConstantsManager()
        registerEvents()
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(EntityExplode(this), this)
    }

    fun createNewExplosion(blockList: List<Block>) {
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

}

