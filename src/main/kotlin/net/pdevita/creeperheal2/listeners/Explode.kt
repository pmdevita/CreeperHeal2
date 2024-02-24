package net.pdevita.creeperheal2.listeners

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent

class Explode(var plugin: CreeperHeal2): Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
//        plugin.debugLogger("An entity explosion has happened! ${event.entityType.toString()}")
        if (plugin.settings.types.allowExplosionEntity(event.entityType)) {
            if (event.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
                this.plugin.createNewExplosion(event.blockList())
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockExplodeEvent(event: BlockExplodeEvent) {
//        plugin.debugLogger("A block explosion has happened! ${event.block.toString()}")
        if (plugin.settings.types.allowExplosionBlock(/*event.block.blockData.material*/)) {
            if (event.block.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
                this.plugin.createNewExplosion(event.blockList())
            }
        }
    }

    @EventHandler()
    fun onHangingBreakEvent(event: HangingBreakEvent) {
        if (plugin.settings.general.entityType && event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
            if (event.cause == HangingBreakEvent.RemoveCause.EXPLOSION) {
                if (event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
                    if (event is HangingBreakByEntityEvent) {
                        plugin.debugLogger("${event.entity} ${event.entity.type} at ${event.entity.location} exploded by ${event.remover}")
                    } else {
                        plugin.debugLogger("${event.entity} ${event.entity.type} at ${event.entity.location} exploded")
                    }
                }
                plugin.createNewExplosion(event.entity)
                event.isCancelled = true
            }
        }
    }

    @EventHandler()
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (plugin.settings.general.entityType && event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
            if (event.entityType == EntityType.ARMOR_STAND) {
                if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                ) {
                    val armorStand = event.entity as ArmorStand
                    // You should be able to just check if it was dealt a killing blow but ehhhhh idk don't work
                    // println("${armorStand.health} - ${event.finalDamage}")
                    // if ((armorStand.health - event.finalDamage).roundToInt() < 0) {
                    plugin.createNewExplosion(armorStand)
                    event.isCancelled = true
                    // }
                }
            }
        }
    }
}

