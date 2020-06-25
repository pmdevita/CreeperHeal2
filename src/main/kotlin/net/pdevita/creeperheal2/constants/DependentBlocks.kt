package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.*
import org.bukkit.Material
import java.util.*
import kotlin.collections.ArrayList

// List of blocks that need to be attached to a block in this version of MC

class DependentBlocks(private val version: ArrayList<Int>) {
    val topBlocks: EnumSet<Material> = EnumSet.noneOf(Material::class.java)
    val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap(Material::class.java)

    init {
        if (version[1] >= 13) {
            this.getVersionBlocks(version[1], 13, Blocks13())
        }
        if (version[1] >= 14) {
            this.getVersionBlocks(version[1], 14, Blocks14())
        }
        if (version[1] >= 15) {
            this.getVersionBlocks(version[1], 15, Blocks15())
        }
    }

    private fun getVersionBlocks(spigotVersion: Int, thisVersion: Int, blocks: VersionBlocks) {
        topBlocks.addAll(blocks.topBlocks)
        sideBlocks.putAll(blocks.sideBlocks)
        if (spigotVersion == thisVersion) {
            blocks.versionTopBlocks()?.let { topBlocks.addAll(it) }
            blocks.versionSideBlocks()?.let { sideBlocks.putAll(it) }
        }
    }

}

open class VersionBlocks {
    // Any block that needs a block below it to exist
    // Their parent is not dependent on where the block is facing
    // Crops, doors, signs
    open val topBlocks: ArrayList<Material> = ArrayList()
    // Any block that is placed on the side of a block
    // These blocks are dependent on the block opposite of where they are facing
    // Buttons, wall signs,
    open val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap(org.bukkit.Material::class.java)
    // Version specific top blocks
    open fun versionTopBlocks(): ArrayList<Material>? { return null }
    // Version specific side blocks
    open fun versionSideBlocks(): EnumMap<Material, FindDependentBlock>? { return null }
}

class Blocks13: VersionBlocks() {
    override val topBlocks = ArrayList<Material>(listOf(
            Material.ACACIA_DOOR,
            Material.ACACIA_PRESSURE_PLATE,
            Material.ACACIA_SAPLING,
            Material.ACTIVATOR_RAIL,
            Material.ALLIUM,
            Material.ATTACHED_MELON_STEM,
            Material.ATTACHED_PUMPKIN_STEM,
            Material.AZURE_BLUET,
            Material.BEETROOTS,
            Material.BIRCH_DOOR,
            Material.BIRCH_PRESSURE_PLATE,
            Material.BIRCH_SAPLING,
            Material.BLACK_BANNER,
            Material.BLACK_CARPET,
            Material.BRAIN_CORAL,
            Material.BRAIN_CORAL_FAN,
            Material.BLUE_BANNER,
            Material.BLUE_ORCHID,
            Material.BROWN_BANNER,
            Material.BROWN_CARPET,
            Material.BROWN_MUSHROOM,
            Material.BUBBLE_CORAL,
            Material.BUBBLE_CORAL_FAN,
            Material.CACTUS,
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.CREEPER_HEAD,
            Material.CYAN_BANNER,
            Material.CYAN_CARPET,
            Material.COMPARATOR,
            Material.DANDELION,
            Material.DARK_OAK_DOOR,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.DARK_OAK_SAPLING,
            Material.DEAD_BRAIN_CORAL,
            Material.DEAD_BRAIN_CORAL_FAN,
            Material.DEAD_BUBBLE_CORAL,
            Material.DEAD_BUBBLE_CORAL_FAN,
            Material.DEAD_BUSH,
            Material.DEAD_FIRE_CORAL,
            Material.DEAD_FIRE_CORAL_FAN,
            Material.DEAD_HORN_CORAL,
            Material.DEAD_HORN_CORAL_FAN,
            Material.DEAD_TUBE_CORAL,
            Material.DEAD_TUBE_CORAL_FAN,
            Material.DETECTOR_RAIL,
            Material.DRAGON_HEAD,
            Material.FERN,
            Material.FIRE,
            Material.FIRE_CORAL,
            Material.FIRE_CORAL_FAN,
            Material.GRAY_BANNER,
            Material.GRAY_CARPET,
            Material.GRASS,
            Material.GREEN_BANNER,
            Material.GREEN_CARPET,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.HORN_CORAL,
            Material.HORN_CORAL_FAN,
            Material.JUNGLE_DOOR,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.JUNGLE_SAPLING,
            Material.KELP,
            Material.LARGE_FERN,
            Material.LIGHT_BLUE_BANNER,
            Material.LIGHT_BLUE_CARPET,
            Material.LIGHT_GRAY_BANNER,
            Material.LIGHT_GRAY_CARPET,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.LILAC,
            Material.LILY_PAD,
            Material.LIME_BANNER,
            Material.LIME_CARPET,
            Material.MAGENTA_BANNER,
            Material.MAGENTA_CARPET,
            Material.MELON_STEM,
            Material.OAK_DOOR,
            Material.OAK_PRESSURE_PLATE,
            Material.OAK_SAPLING,
            Material.ORANGE_BANNER,
            Material.ORANGE_CARPET,
            Material.ORANGE_TULIP,
            Material.OXEYE_DAISY,
            Material.PEONY,
            Material.PINK_BANNER,
            Material.PINK_CARPET,
            Material.PINK_TULIP,
            Material.PLAYER_HEAD,
            Material.POPPY,
            Material.POTATOES,
            Material.POWERED_RAIL,
            Material.PURPLE_BANNER,
            Material.PURPLE_CARPET,
            Material.RAIL,
            Material.RED_BANNER,
            Material.RED_CARPET,
            Material.RED_MUSHROOM,
            Material.RED_TULIP,
            Material.REDSTONE,
            Material.REDSTONE_TORCH,
            Material.REDSTONE_WIRE,
            Material.REPEATER,
            Material.ROSE_BUSH,
            Material.SEA_PICKLE,
            Material.SEAGRASS,
            Material.SKELETON_SKULL,
            Material.SPRUCE_DOOR,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.SPRUCE_SAPLING,
            Material.STONE_PRESSURE_PLATE,
            Material.SUGAR_CANE,
            Material.SUNFLOWER,
            Material.TALL_GRASS,
            Material.TALL_SEAGRASS,
            Material.TORCH,
            Material.TUBE_CORAL,
            Material.TUBE_CORAL_FAN,
            Material.TURTLE_EGG,
            Material.WHEAT,
            Material.WHITE_BANNER,
            Material.WHITE_CARPET,
            Material.WHITE_TULIP,
            Material.WITHER_SKELETON_SKULL,
            Material.YELLOW_BANNER,
            Material.YELLOW_CARPET,
            Material.ZOMBIE_HEAD
    ))
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.BLACK_WALL_BANNER to Behind,
            Material.BLUE_WALL_BANNER to Behind,
            Material.BRAIN_CORAL_WALL_FAN to Behind,
            Material.BROWN_WALL_BANNER to Behind,
            Material.BUBBLE_CORAL_WALL_FAN to Behind,
            Material.COCOA to InFrontOf,
            Material.CREEPER_WALL_HEAD to Behind,
            Material.CYAN_WALL_BANNER to Behind,
            Material.DEAD_BRAIN_CORAL_WALL_FAN to Behind,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN to Behind,
            Material.DEAD_FIRE_CORAL_WALL_FAN to Behind,
            Material.DEAD_HORN_CORAL_WALL_FAN to Behind,
            Material.DEAD_TUBE_CORAL_WALL_FAN to Behind,
            Material.DRAGON_WALL_HEAD to Behind,
            Material.FIRE_CORAL_WALL_FAN to Behind,
            Material.GRAY_WALL_BANNER to Behind,
            Material.GREEN_WALL_BANNER to Behind,
            Material.HORN_CORAL_WALL_FAN to Behind,
            Material.LADDER to Behind,
            Material.LIGHT_BLUE_WALL_BANNER to Behind,
            Material.LIGHT_GRAY_WALL_BANNER to Behind,
            Material.LIME_WALL_BANNER to Behind,
            Material.MAGENTA_WALL_BANNER to Behind,
            Material.MOVING_PISTON to Behind,
            Material.ORANGE_WALL_BANNER to Behind,
            Material.PAINTING to Behind,
            Material.PINK_WALL_BANNER to Behind,
            Material.PISTON_HEAD to Behind,
            Material.PLAYER_WALL_HEAD to Behind,
            Material.PURPLE_WALL_BANNER to Behind,
            Material.RED_WALL_BANNER to Behind,
            Material.REDSTONE_WALL_TORCH to Behind,
            Material.SKELETON_WALL_SKULL to Behind,
            Material.TRIPWIRE_HOOK to Behind,
            Material.TUBE_CORAL_WALL_FAN to Behind,
            Material.VINE to Vine,
            Material.WALL_TORCH to Behind,
            Material.WITHER_SKELETON_WALL_SKULL to Behind,
            Material.WHITE_WALL_BANNER to Behind,
            Material.YELLOW_WALL_BANNER to Behind,
            Material.ZOMBIE_WALL_HEAD to Behind
    ))

    override fun versionTopBlocks(): ArrayList<Material> {
        return ArrayList<Material>(listOf(Material.valueOf("SIGN")))
    }

    override fun versionSideBlocks(): EnumMap<Material, FindDependentBlock> {
        return EnumMap<Material, FindDependentBlock>(mapOf(
                Material.valueOf("WALL_SIGN") to Behind,
                Material.ACACIA_BUTTON to Behind,
                Material.BIRCH_BUTTON to Behind,
                Material.DARK_OAK_BUTTON to Behind,
                Material.JUNGLE_BUTTON to Behind,
                Material.LEVER to Behind,
                Material.OAK_BUTTON to Behind,
                Material.SPRUCE_BUTTON to Behind,
                Material.STONE_BUTTON to Behind
        ))
    }
}

class Blocks14: VersionBlocks() {
    override val topBlocks = ArrayList<Material>(listOf(
            Material.ACACIA_SIGN,
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.BIRCH_SIGN,
            Material.CORNFLOWER,
            Material.DARK_OAK_SIGN,
            Material.JUNGLE_SIGN,
            Material.LILY_OF_THE_VALLEY,
            Material.OAK_SIGN,
            Material.SPRUCE_SIGN,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE
    ))
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.ACACIA_WALL_SIGN to Behind,
            Material.BIRCH_WALL_SIGN to Behind,
            Material.DARK_OAK_WALL_SIGN to Behind,
            Material.JUNGLE_WALL_SIGN to Behind,
            Material.OAK_WALL_SIGN to Behind,
            Material.SCAFFOLDING to Behind,
            Material.SPRUCE_WALL_SIGN to Behind
    ))

    override fun versionSideBlocks(): EnumMap<Material, FindDependentBlock>? {
        return EnumMap<Material, FindDependentBlock>(mapOf(
                Material.ACACIA_BUTTON to Behind,
                Material.BIRCH_BUTTON to Behind,
                Material.DARK_OAK_BUTTON to Behind,
                Material.JUNGLE_BUTTON to Behind,
                Material.LEVER to Behind,
                Material.OAK_BUTTON to Behind,
                Material.SPRUCE_BUTTON to Behind,
                Material.STONE_BUTTON to Behind
        ))
    }
}

class Blocks15: VersionBlocks() {
    override val topBlocks = ArrayList<Material>(listOf())
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.ACACIA_BUTTON to FaceAttachable,
            Material.BIRCH_BUTTON to FaceAttachable,
            Material.DARK_OAK_BUTTON to FaceAttachable,
            Material.JUNGLE_BUTTON to FaceAttachable,
            Material.LEVER to FaceAttachable,
            Material.OAK_BUTTON to FaceAttachable,
            Material.SPRUCE_BUTTON to FaceAttachable,
            Material.STONE_BUTTON to FaceAttachable
    ))
}
