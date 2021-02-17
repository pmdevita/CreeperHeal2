package net.pdevita.creeperheal2.config

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType

class ConfigManager(private val plugin: CreeperHeal2, config: FileConfiguration) {
    val general = General(config)
    val types = ExplosionTypes(config)
}

class General(config: FileConfiguration) {
    val initialDelay = config.getInt("initial-delay", 45)
    val betweenBlocksDelay = config.getInt("between-blocks-delay", 20)
    val bstats = config.getBoolean("bstats", true)
    val explodeTNT = config.getBoolean("explode-tnt", true)
    val turboThreshold = config.getInt("turbo-threshold", 5000)
    val turboAmount = config.getInt("turbo-amount", 3)
}

class ExplosionTypes(config: FileConfiguration) {
    private val tnt = config.getBoolean("types.tnt", false)
    private val creeper = config.getBoolean("types.creeper", true)
    // Dragon fireballs do not do any impact damage so even though it is
    // considered an explosion, it's not really something we need to worry about
//    val endDragon = config.getBoolean("types.end_dragon")
    private val ghast = config.getBoolean("types.ghast", false)
    private val wither = config.getBoolean("types.wither", false)
    private val endCrystal = config.getBoolean("types.ender-crystal", false)
    private val minecartTnt = config.getBoolean("types.minecart-tnt", false)
    private val bed = config.getBoolean("types.bed", false)

    fun allowExplosionEntity(entity: EntityType): Boolean {
        return when(entity) {
            EntityType.CREEPER -> creeper
            EntityType.PRIMED_TNT -> tnt
            EntityType.FIREBALL -> ghast
            EntityType.WITHER -> wither
            EntityType.WITHER_SKULL -> wither
            EntityType.ENDER_CRYSTAL -> endCrystal
            EntityType.MINECART_TNT -> minecartTnt
            else -> false
        }
    }

    fun allowExplosionBlock(/*block: Material*/): Boolean {
        // Only beds cause this?
        return bed
    }

}

