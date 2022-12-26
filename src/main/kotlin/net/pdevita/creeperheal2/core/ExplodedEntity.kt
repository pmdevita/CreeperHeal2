package net.pdevita.creeperheal2.core

import org.bukkit.Art
import org.bukkit.Location
import org.bukkit.Rotation
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Hanging
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.ItemStack
import kotlin.math.min

open class ExplodedEntity() {
    open var entityType: EntityType? = null
    lateinit var location: Location

    companion object {
        fun from(entity: Entity): ExplodedEntity {
            return when (entity) {
                is org.bukkit.entity.Painting -> ExplodedPainting(entity)
                is org.bukkit.entity.ItemFrame -> ExplodedItemFrame(entity)
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

    private fun spawnEntity(): Entity? {
        return entityType?.let { location.world?.spawnEntity(location, it) }
    }

    fun placeEntity() {
        val entity = spawnEntity()
        if (entity != null) {
            loadData(entity)
        }
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
    override var entityType: EntityType? = EntityType.PAINTING
    private lateinit var art: Art

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val painting = entity as org.bukkit.entity.Painting
        art = painting.art
        val direction = facingDirection.direction
//        val downBy = if (art.blockHeight > 1) {
//            1
//        } else {
//            0
//        }
        val downBy = min(art.blockHeight - 1, 1)
//        val rightBy = min(art.blockWidth - 1, 1)
//        val rightBy = art.blockWidth - 1
        val rightBy = if (art.blockWidth == 1) {
            0
        } else {
            min(art.blockWidth - 1, 2)
        }
        val actualLocation = Location(location.world,
            location.x + -direction.z * rightBy,
            location.y - downBy,
            location.z + direction.x * rightBy)
        println("Guessing actual painting location at $actualLocation given vector $direction $facingDirection")
        location = actualLocation

    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val painting = entity as org.bukkit.entity.Painting
        painting.setArt(art, true)
        println("setting art to ${art.name}")
        println("placed at: $location now painting at: ${painting.location}")
    }
}

class ExplodedItemFrame(entity: Entity) : ExplodedHanging(entity) {
    override var entityType: EntityType? = EntityType.ITEM_FRAME
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

