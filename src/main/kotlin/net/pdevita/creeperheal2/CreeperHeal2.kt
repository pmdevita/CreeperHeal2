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
    lateinit var constants: ConstantsManager

    constructor() : super()

    override fun onEnable() {
        super.onEnable()
        constants = ConstantsManager()
        registerEvents()
        logger.info("CreeperHeal2 Enabled")
        if (constants.dependentBlocks.topBlocks.contains(Material.WHEAT)) {
            logger.info("hell yeah dude")
        } else {
            logger.info("aww hell naw dude")
        }
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

}

