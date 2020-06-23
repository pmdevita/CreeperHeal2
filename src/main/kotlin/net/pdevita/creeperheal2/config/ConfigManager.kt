package net.pdevita.creeperheal2.config

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType

class ConfigManager(private val plugin: CreeperHeal2, private val config: FileConfiguration) {
    val general = General(config)
    val types = ExplosionTypes(config)
}

class General(private val config: FileConfiguration) {
    val initialDelay = config.getInt("initial-delay", 45)
    val betweenBlocksDelay = config.getInt("between-blocks-delay", 20)
}

class ExplosionTypes(private val config: FileConfiguration) {
    private val tnt = config.getBoolean("types.tnt")
    private val creeper = config.getBoolean("types.creeper")
    // Dragon fireballs do not do any impact damage so even though it is
    // considered an explosion, it's not really something we need to worry about
//    val endDragon = config.getBoolean("types.end_dragon")
    private val ghast = config.getBoolean("types.ghast")
    private val wither = config.getBoolean("types.wither")
    private val endCrystal = config.getBoolean("types.ender-crystal")
    private val minecartTnt = config.getBoolean("types.minecart-tnt")
    private val bed = config.getBoolean("types.bed")

    fun allowExplosionEntity(entity: EntityType): Boolean {
        return when(entity) {
            EntityType.CREEPER -> creeper
            EntityType.PRIMED_TNT -> tnt
            EntityType.FIREBALL -> ghast
            EntityType.WITHER_SKULL -> wither
            EntityType.ENDER_CRYSTAL -> endCrystal
            EntityType.MINECART_TNT -> minecartTnt
            else -> false
        }
    }

    fun allowExplosionBlock(block: Material): Boolean {
        // Only beds cause this?
        return bed
    }

}

