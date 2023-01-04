package net.pdevita.creeperheal2.core

import org.bukkit.Art
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

open class ExplodedEntity() {
    open val entityType: EntityType? = null
    lateinit var location: Location

    companion object {
        fun from(entity: Entity): ExplodedEntity {
            return when (entity) {
                is Painting -> ExplodedPainting(entity)
                is GlowItemFrame -> ExplodedGlowItemFrame(entity)
                is ItemFrame -> ExplodedItemFrame(entity)
                is ArmorStand -> ExplodedArmorStand(entity)
                else -> throw java.lang.Exception("$entity is not a supported exploded entity")
            }
        }
    }

    constructor(entity: Entity): this() {
        saveData(entity)
        entity.remove()
    }

    open fun saveData(entity: Entity) {
        location = entity.location
    }

    fun spawnEntity(location: Location): Entity? {
        val entityClass = entityType?.entityClass!!
        val currentBlock = location.block

        // If block isn't air, it's likely a player put it there. Just break it off normally to give it back to them
        if (currentBlock.blockData.material != Material.AIR) {
            currentBlock.breakNaturally()
        }

        return location.world?.spawn(location, entityClass) { loadData(it) }
    }

    fun placeEntity(): Boolean {
        try {
            spawnEntity(location)
        } catch (e: IllegalArgumentException) {
            println("IllegalArgumentException, could not place painting $location ${location.block}")
            return false
        }
        return true
    }

    open fun loadData(entity: Entity) {

    }

}

open class ExplodedHanging(entity: Entity): ExplodedEntity(entity) {
    lateinit var facingDirection: BlockFace

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val hanging = entity as Hanging
        facingDirection = hanging.facing
    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val hanging = entity as Hanging
        hanging.setFacingDirection(facingDirection)
    }
}

class ExplodedPainting(entity: Entity) : ExplodedHanging(entity) {
    override val entityType: EntityType = EntityType.PAINTING
    private lateinit var art: Art

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val painting = entity as Painting
        art = painting.art
        location.subtract(getPaintingOffset(facingDirection, art))
    }

    // Painting data probably kept somewhere
    private fun getPaintingOffset(direction: BlockFace, art: Art): Vector {
        val vector = Vector(0, 0, 0)
        if (art.blockHeight % 2 == 0) {
            vector.y = 1.0
        }
        if (art.blockWidth >= 2) {
            if (direction == BlockFace.SOUTH) {
                vector.x = 1.0
            } else if (direction == BlockFace.WEST) {
                vector.z = 1.0
            }
        }
        return vector
    }

    override fun loadData(entity: Entity) {
        // Use the normally spawned painting to figure out how far
        // it will move after we set the art
        super.loadData(entity)
        val painting = entity as Painting
        painting.setArt(art, true)
    }
}

open class ExplodedItemFrame(entity: Entity) : ExplodedHanging(entity) {
    override val entityType: EntityType? = EntityType.ITEM_FRAME
    private lateinit var itemStack: ItemStack
    private lateinit var rotation: Rotation

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val itemFrame = entity as ItemFrame
        itemStack = itemFrame.item
        rotation = itemFrame.rotation
    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val itemFrame = entity as ItemFrame
        itemFrame.setItem(itemStack)
        itemFrame.rotation = rotation
    }
}

class ExplodedGlowItemFrame(entity: Entity): ExplodedItemFrame(entity) {
    override val entityType: EntityType = EntityType.GLOW_ITEM_FRAME
}

class ExplodedArmorStand(entity: Entity): ExplodedEntity(entity) {
    override val entityType: EntityType = EntityType.ARMOR_STAND

    private lateinit var bodyPose: EulerAngle
    private lateinit var headPose: EulerAngle
    private lateinit var leftLegPose: EulerAngle
    private lateinit var leftArmPose: EulerAngle
    private lateinit var rightLegPose: EulerAngle
    private lateinit var rightArmPose: EulerAngle
    private var hasArms: Boolean = false
    private var noBasePlate: Boolean = false
    private var small: Boolean = false

    private lateinit var armor: Array<ItemStack>
    private lateinit var mainHand: ItemStack
    private lateinit var offHand: ItemStack

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val armorStand = entity as ArmorStand
        if (armorStand.equipment != null) {
            armor = armorStand.equipment!!.armorContents
            mainHand = armorStand.equipment!!.itemInMainHand
            offHand = armorStand.equipment!!.itemInOffHand
        }
        bodyPose = armorStand.bodyPose
        headPose = armorStand.headPose
        leftLegPose = armorStand.leftLegPose
        leftArmPose = armorStand.leftArmPose
        rightArmPose = armorStand.rightArmPose
        rightLegPose = armorStand.rightLegPose
        hasArms = armorStand.hasArms()
        noBasePlate = armorStand.hasBasePlate()
        small = armorStand.isSmall
    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val armorStand = entity as ArmorStand
        armorStand.equipment!!.armorContents = armor
        armorStand.equipment!!.setItemInMainHand(mainHand)
        armorStand.equipment!!.setItemInOffHand(offHand)
        armorStand.headPose = headPose
        armorStand.bodyPose = bodyPose
        armorStand.leftArmPose = leftArmPose
        armorStand.leftLegPose = leftLegPose
        armorStand.rightArmPose = rightArmPose
        armorStand.rightLegPose = rightLegPose
        armorStand.setArms(hasArms)
        armorStand.setBasePlate(noBasePlate)
        armorStand.isSmall = small
    }

}
