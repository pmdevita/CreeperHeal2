package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.*
import org.bukkit.Material
import org.bukkit.Tag
import java.util.*

// List of blocks that need to be attached to a block in this version of MC

class DependentBlocks(version: Pair<Int, Int>) {
    val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap(Material::class.java)

    init {
        if (version.second >= 13) {
            this.getVersionBlocks(version.second, 13, Blocks13())
        }
        if (version.second >= 14) {
            this.getVersionBlocks(version.second, 14, Blocks14())
        }
        if (version.second >= 15) {
            this.getVersionBlocks(version.second, 15, Blocks15())
        }
        if (version.second >= 16) {
            this.getVersionBlocks(version.second, 16, Blocks16())
        }
        if (version.second >= 17) {
            this.getVersionBlocks(version.second, 17, Blocks17())
        }
    }

    private fun getVersionBlocks(spigotVersion: Int, thisVersion: Int, blocks: VersionBlocks) {
        sideBlocks.putAll(blocks.sideBlocks)
        for (tag in blocks.sideTags) {
            for (block in tag.first.values) {
                sideBlocks[block] = tag.second
            }
        }
        if (spigotVersion == thisVersion) {
            blocks.versionSideBlocks()?.let { sideBlocks.putAll(it) }
        }
    }

}

private open class VersionBlocks {
    // Any block that is placed on the side of a block
    // These blocks are dependent on the block opposite of where they are facing
    // Buttons, wall signs,
    open val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap(org.bukkit.Material::class.java)
    open val sideTags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf()
    // Version specific side blocks
    open fun versionSideBlocks(): EnumMap<Material, FindDependentBlock>? { return null }
}

private class Blocks13: VersionBlocks() {
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
//            Material.ACACIA_DOOR to OnTopOf,
            Material.ACACIA_PRESSURE_PLATE to OnTopOf,
            Material.ACACIA_SAPLING to OnTopOf,
            Material.ACTIVATOR_RAIL to OnTopOf,
            Material.ALLIUM to OnTopOf,
            Material.ATTACHED_MELON_STEM to OnTopOf,
            Material.ATTACHED_PUMPKIN_STEM to OnTopOf,
            Material.AZURE_BLUET to OnTopOf,
            Material.BEETROOTS to OnTopOf,
//            Material.BIRCH_DOOR to OnTopOf,
            Material.BIRCH_PRESSURE_PLATE to OnTopOf,
            Material.BIRCH_SAPLING to OnTopOf,
            Material.BLACK_BANNER to OnTopOf,
            Material.BRAIN_CORAL to OnTopOf,
            Material.BRAIN_CORAL_FAN to OnTopOf,
            Material.BLUE_BANNER to OnTopOf,
            Material.BLUE_ORCHID to OnTopOf,
            Material.BROWN_BANNER to OnTopOf,
            Material.BROWN_MUSHROOM to OnTopOf,
            Material.BUBBLE_CORAL to OnTopOf,
            Material.BUBBLE_CORAL_FAN to OnTopOf,
            Material.CACTUS to OnTopOf,
            Material.CHORUS_FLOWER to OnTopOf,
            Material.CHORUS_PLANT to OnTopOf,
            Material.CREEPER_HEAD to OnTopOf,
            Material.CYAN_BANNER to OnTopOf,
            Material.COMPARATOR to OnTopOf,
            Material.DANDELION to OnTopOf,
//            Material.DARK_OAK_DOOR to OnTopOf,
            Material.DARK_OAK_PRESSURE_PLATE to OnTopOf,
            Material.DARK_OAK_SAPLING to OnTopOf,
            Material.DEAD_BRAIN_CORAL to OnTopOf,
            Material.DEAD_BRAIN_CORAL_FAN to OnTopOf,
            Material.DEAD_BUBBLE_CORAL to OnTopOf,
            Material.DEAD_BUBBLE_CORAL_FAN to OnTopOf,
            Material.DEAD_BUSH to OnTopOf,
            Material.DEAD_FIRE_CORAL to OnTopOf,
            Material.DEAD_FIRE_CORAL_FAN to OnTopOf,
            Material.DEAD_HORN_CORAL to OnTopOf,
            Material.DEAD_HORN_CORAL_FAN to OnTopOf,
            Material.DEAD_TUBE_CORAL to OnTopOf,
            Material.DEAD_TUBE_CORAL_FAN to OnTopOf,
            Material.DETECTOR_RAIL to OnTopOf,
            Material.DRAGON_HEAD to OnTopOf,
            Material.FERN to OnTopOf,
            Material.FIRE to OnTopOf,
            Material.FIRE_CORAL to OnTopOf,
            Material.FIRE_CORAL_FAN to OnTopOf,
            Material.GRAY_BANNER to OnTopOf,
            Material.GRASS to OnTopOf,
            Material.GREEN_BANNER to OnTopOf,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.HORN_CORAL to OnTopOf,
            Material.HORN_CORAL_FAN to OnTopOf,
//            Material.JUNGLE_DOOR to OnTopOf,
            Material.JUNGLE_PRESSURE_PLATE to OnTopOf,
            Material.JUNGLE_SAPLING to OnTopOf,
            Material.KELP to OnTopOf,
            Material.LARGE_FERN to OnTopOf,
            Material.LIGHT_BLUE_BANNER to OnTopOf,
            Material.LIGHT_GRAY_BANNER to OnTopOf,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.LILAC to OnTopOf,
            Material.LILY_PAD to OnTopOf,
            Material.LIME_BANNER to OnTopOf,
            Material.MAGENTA_BANNER to OnTopOf,
            Material.MELON_STEM to OnTopOf,
//            Material.OAK_DOOR to OnTopOf,
            Material.OAK_PRESSURE_PLATE to OnTopOf,
            Material.OAK_SAPLING to OnTopOf,
            Material.ORANGE_BANNER to OnTopOf,
            Material.ORANGE_TULIP to OnTopOf,
            Material.OXEYE_DAISY to OnTopOf,
            Material.PEONY to OnTopOf,
            Material.PINK_BANNER to OnTopOf,
            Material.PINK_TULIP to OnTopOf,
            Material.PLAYER_HEAD to OnTopOf,
            Material.POPPY to OnTopOf,
            Material.POTATOES to OnTopOf,
            Material.POWERED_RAIL to OnTopOf,
            Material.PURPLE_BANNER to OnTopOf,
            Material.RAIL to OnTopOf,
            Material.RED_BANNER to OnTopOf,
            Material.RED_MUSHROOM to OnTopOf,
            Material.RED_TULIP to OnTopOf,
            Material.REDSTONE to OnTopOf,
            Material.REDSTONE_TORCH to OnTopOf,
            Material.REDSTONE_WIRE to OnTopOf,
            Material.REPEATER to OnTopOf,
            Material.ROSE_BUSH to OnTopOf,
            Material.SEA_PICKLE to OnTopOf,
            Material.SEAGRASS to OnTopOf,
            Material.SKELETON_SKULL to OnTopOf,
//            Material.SPRUCE_DOOR to OnTopOf,
            Material.SPRUCE_PRESSURE_PLATE to OnTopOf,
            Material.SPRUCE_SAPLING to OnTopOf,
            Material.STONE_PRESSURE_PLATE to OnTopOf,
            Material.SUGAR_CANE to OnTopOf,
            Material.SUNFLOWER to OnTopOf,
            Material.TALL_GRASS to OnTopOf,
            Material.TALL_SEAGRASS to OnTopOf,
            Material.TORCH to OnTopOf,
            Material.TUBE_CORAL to OnTopOf,
            Material.TUBE_CORAL_FAN to OnTopOf,
            Material.TURTLE_EGG to OnTopOf,
            Material.WHEAT to OnTopOf,
            Material.WHITE_BANNER to OnTopOf,
            Material.WHITE_TULIP to OnTopOf,
            Material.WITHER_SKELETON_SKULL to OnTopOf,
            Material.YELLOW_BANNER to OnTopOf,
            Material.ZOMBIE_HEAD to OnTopOf,
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
            Material.ORANGE_WALL_BANNER to Behind,
            Material.PAINTING to Behind,
            Material.PINK_WALL_BANNER to Behind,
//            Material.PISTON_HEAD to Behind,
//            Material.MOVING_PISTON to Behind,
            Material.PISTON to Piston,
            Material.STICKY_PISTON to Piston,
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

        override val sideTags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
            Pair(Tag.CARPETS, OnTopOf)
        )

        override fun versionSideBlocks(): EnumMap<Material, FindDependentBlock> {
        return EnumMap<Material, FindDependentBlock>(mapOf(
                Material.valueOf("SIGN") to OnTopOf, // For values that have been removed from the current build target, reference them by string
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

private class Blocks14: VersionBlocks() {
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.ACACIA_SIGN to OnTopOf,
            Material.BAMBOO to OnTopOf,
            Material.BAMBOO_SAPLING to OnTopOf,
            Material.BIRCH_SIGN to OnTopOf,
            Material.CORNFLOWER to OnTopOf,
            Material.DARK_OAK_SIGN to OnTopOf,
            Material.JUNGLE_SIGN to OnTopOf,
            Material.LILY_OF_THE_VALLEY to OnTopOf,
            Material.OAK_SIGN to OnTopOf,
            Material.SPRUCE_SIGN to OnTopOf,
            Material.SWEET_BERRY_BUSH to OnTopOf,
            Material.WITHER_ROSE to OnTopOf,
            Material.ACACIA_WALL_SIGN to Behind,
            Material.BIRCH_WALL_SIGN to Behind,
            Material.DARK_OAK_WALL_SIGN to Behind,
            Material.JUNGLE_WALL_SIGN to Behind,
            Material.OAK_WALL_SIGN to Behind,
            Material.SCAFFOLDING to Scaffolding,
            Material.SPRUCE_WALL_SIGN to Behind,
            Material.LANTERN to TopOrBottom
    ))

    override fun versionSideBlocks(): EnumMap<Material, FindDependentBlock> {
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

private class Blocks15: VersionBlocks() {
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

private class Blocks16: VersionBlocks() {
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
//            Material.CRIMSON_DOOR to OnTopOf,
            Material.CRIMSON_FUNGUS to OnTopOf,
            Material.CRIMSON_PRESSURE_PLATE to OnTopOf,
            Material.CRIMSON_ROOTS to OnTopOf,
            Material.CRIMSON_SIGN to OnTopOf,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE to OnTopOf,
            Material.SOUL_TORCH to OnTopOf,
            Material.TWISTING_VINES to OnTopOf,
            Material.TWISTING_VINES_PLANT to OnTopOf,
//            Material.WARPED_DOOR to OnTopOf,
            Material.WARPED_FUNGUS to OnTopOf,
            Material.WARPED_PRESSURE_PLATE to OnTopOf,
            Material.WARPED_ROOTS to OnTopOf,
            Material.WARPED_SIGN to OnTopOf,
            Material.CRIMSON_BUTTON to FaceAttachable,
            Material.CRIMSON_WALL_SIGN to Behind,
            Material.POLISHED_BLACKSTONE_BUTTON to FaceAttachable,
            Material.SOUL_WALL_TORCH to Behind,
            Material.WARPED_BUTTON to FaceAttachable,
            Material.WARPED_WALL_SIGN to Behind,
            Material.WEEPING_VINES to Below,
            Material.WEEPING_VINES_PLANT to Below,
            Material.SOUL_LANTERN to TopOrBottom
    ))
}

private class Blocks17: VersionBlocks() {
    override val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
        Material.AMETHYST_CLUSTER to Behind,
        Material.SMALL_AMETHYST_BUD to Behind,
        Material.MEDIUM_AMETHYST_BUD to Behind,
        Material.LARGE_AMETHYST_BUD to Behind,
        Material.POINTED_DRIPSTONE to Dripstone,
        Material.AZALEA to OnTopOf,
        Material.FLOWERING_AZALEA to OnTopOf,
        Material.BIG_DRIPLEAF to OnTopOf,
        Material.SMALL_DRIPLEAF to OnTopOf,
        Material.BIG_DRIPLEAF_STEM to OnTopOf,
        Material.HANGING_ROOTS to Below,
        Material.GLOW_LICHEN to Vine,
        Material.SPORE_BLOSSOM to Below
    ))
    override val sideTags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
        
    )
}
