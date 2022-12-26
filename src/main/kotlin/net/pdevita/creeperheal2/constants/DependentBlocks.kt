package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.*
import org.bukkit.Material
import org.bukkit.Tag
import java.util.*

// List of blocks that need to be attached to a block in this version of MC


class DependentBlocks(version: Pair<Int, Int>) {
    val sideBlocks: EnumMap<Material, FindDependentBlock> = EnumMap(Material::class.java)

    init {
        for (versionBlocks in VERSION_TO_BLOCKS) {
            this.getVersionBlocks(version.second, versionBlocks)
        }
    }

    private fun getVersionBlocks(spigotVersion: Int, versionBlocks: VersionBlocks) {
        if (spigotVersion >= versionBlocks.version) {
            sideBlocks.putAll(versionBlocks.blocks)
            addTagMap(versionBlocks.tags)
        }
        if (spigotVersion == versionBlocks.version) {
            versionBlocks.versionBlocks()?.let { sideBlocks.putAll(it) }
            versionBlocks.versionTags()?.let { addTagMap(it) }
        }
    }

    private fun addTagMap(tagMap: Array<Pair<Tag<Material>, FindDependentBlock>>) {
        for (tag in tagMap) {
            for (block in tag.first.values) {
                sideBlocks[block] = tag.second
            }
        }
    }
}


private val VERSION_TO_BLOCKS: Array<VersionBlocks> = arrayOf(
    Blocks13,
    Blocks14,
    Blocks15,
    Blocks16,
    Blocks17,
    Blocks19
)


private open class VersionBlocks {
    open val version: Int = -1;
    // Any block that is placed on the side of a block
    // These blocks are dependent on the block opposite of where they are facing
    // Buttons, wall signs,
    open val blocks: EnumMap<Material, FindDependentBlock> = EnumMap(org.bukkit.Material::class.java)
    open val tags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf()
    // Version specific side blocks
    open fun versionBlocks(): EnumMap<Material, FindDependentBlock>? { return null }
    open fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>>? { return null}
}

private object Blocks13: VersionBlocks() {
    override val version = 13
    override val blocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
        Material.ALLIUM to OnTopOf,
        Material.ATTACHED_MELON_STEM to OnTopOf,
        Material.ATTACHED_PUMPKIN_STEM to OnTopOf,
        Material.AZURE_BLUET to OnTopOf,
        Material.BEETROOTS to OnTopOf,
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
        Material.DRAGON_HEAD to OnTopOf,
        Material.FERN to OnTopOf,
        Material.FIRE to OnTopOf,
        Material.FIRE_CORAL to OnTopOf,
        Material.FIRE_CORAL_FAN to OnTopOf,
        Material.GRAY_BANNER to OnTopOf,
        Material.GRASS to OnTopOf,
        Material.GREEN_BANNER to OnTopOf,
        Material.HORN_CORAL to OnTopOf,
        Material.HORN_CORAL_FAN to OnTopOf,
        Material.KELP to OnTopOf,
        Material.LARGE_FERN to OnTopOf,
        Material.LIGHT_BLUE_BANNER to OnTopOf,
        Material.LIGHT_GRAY_BANNER to OnTopOf,
        Material.LILAC to OnTopOf,
        Material.LILY_PAD to OnTopOf,
        Material.LIME_BANNER to OnTopOf,
        Material.MAGENTA_BANNER to OnTopOf,
        Material.MELON_STEM to OnTopOf,
        Material.ORANGE_BANNER to OnTopOf,
        Material.ORANGE_TULIP to OnTopOf,
        Material.OXEYE_DAISY to OnTopOf,
        Material.PEONY to OnTopOf,
        Material.PINK_BANNER to OnTopOf,
        Material.PINK_TULIP to OnTopOf,
        Material.PLAYER_HEAD to OnTopOf,
        Material.POPPY to OnTopOf,
        Material.POTATOES to OnTopOf,
        Material.PURPLE_BANNER to OnTopOf,
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
        Material.PISTON to Piston,
        Material.STICKY_PISTON to Piston,
        Material.PLAYER_WALL_HEAD to Behind,
        Material.PURPLE_WALL_BANNER to Behind,
        Material.RED_WALL_BANNER to Behind,
        Material.REDSTONE_WALL_TORCH to Behind,
        Material.SKELETON_WALL_SKULL to Behind,
        Material.TRIPWIRE_HOOK to Behind,
        Material.TUBE_CORAL_WALL_FAN to Behind,
        Material.WALL_TORCH to Behind,
        Material.WITHER_SKELETON_WALL_SKULL to Behind,
        Material.WHITE_WALL_BANNER to Behind,
        Material.YELLOW_WALL_BANNER to Behind,
        Material.ZOMBIE_WALL_HEAD to Behind
    ))

    override val tags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
        Pair(Tag.DOORS, OnTopOf),
        Pair(Tag.RAILS, OnTopOf),
        Pair(Tag.SAPLINGS, OnTopOf),
        Pair(Tag.BUTTONS, Behind)
    )

    override fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>>? {
        return arrayOf(
            Pair(Tag.CARPETS, OnTopOf),
        )
    }

    override fun versionBlocks(): EnumMap<Material, FindDependentBlock> {
        return EnumMap<Material, FindDependentBlock>(mapOf(
            Material.valueOf("SIGN") to OnTopOf, // For values that have been removed from the current build target, reference them by string
            Material.valueOf("WALL_SIGN") to Behind,
            Material.ACACIA_PRESSURE_PLATE to OnTopOf,
            Material.BIRCH_PRESSURE_PLATE to OnTopOf,
            Material.DARK_OAK_PRESSURE_PLATE to OnTopOf,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.JUNGLE_PRESSURE_PLATE to OnTopOf,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.OAK_PRESSURE_PLATE to OnTopOf,
            Material.SPRUCE_PRESSURE_PLATE to OnTopOf,
            Material.STONE_PRESSURE_PLATE to OnTopOf,

        ))
    }
}

private object Blocks14: VersionBlocks() {
    override val version = 14
    override val blocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.BAMBOO to OnTopOf,
            Material.BAMBOO_SAPLING to OnTopOf,
            Material.CORNFLOWER to OnTopOf,
            Material.LILY_OF_THE_VALLEY to OnTopOf,
            Material.SWEET_BERRY_BUSH to OnTopOf,
            Material.WITHER_ROSE to OnTopOf,
            Material.SCAFFOLDING to Scaffolding,
            Material.LANTERN to TopOrBottom
    ))

    override val tags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
            Pair(Tag.STANDING_SIGNS, OnTopOf),
            Pair(Tag.WALL_SIGNS, Behind)
        )

    override fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>> {
        return arrayOf(
            Pair(Tag.CARPETS, OnTopOf),
        )
    }

    override fun versionBlocks(): EnumMap<Material, FindDependentBlock> {
        return EnumMap<Material, FindDependentBlock>(mapOf(
                Material.ACACIA_PRESSURE_PLATE to OnTopOf,
                Material.BIRCH_PRESSURE_PLATE to OnTopOf,
                Material.DARK_OAK_PRESSURE_PLATE to OnTopOf,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE to OnTopOf,
                Material.JUNGLE_PRESSURE_PLATE to OnTopOf,
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE to OnTopOf,
                Material.OAK_PRESSURE_PLATE to OnTopOf,
                Material.SPRUCE_PRESSURE_PLATE to OnTopOf,
                Material.STONE_PRESSURE_PLATE to OnTopOf,
        ))
    }
}

private object Blocks15: VersionBlocks() {
    override val version = 15

    override fun versionBlocks(): EnumMap<Material, FindDependentBlock> {
        return EnumMap<Material, FindDependentBlock>(mapOf(
            Material.ACACIA_PRESSURE_PLATE to OnTopOf,
            Material.BIRCH_PRESSURE_PLATE to OnTopOf,
            Material.DARK_OAK_PRESSURE_PLATE to OnTopOf,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.JUNGLE_PRESSURE_PLATE to OnTopOf,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE to OnTopOf,
            Material.OAK_PRESSURE_PLATE to OnTopOf,
            Material.SPRUCE_PRESSURE_PLATE to OnTopOf,
            Material.STONE_PRESSURE_PLATE to OnTopOf,
        ))
    }

    override fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>>? {
        return arrayOf(
            Pair(Tag.CARPETS, OnTopOf),
        )
    }
}

private object Blocks16: VersionBlocks() {
    override val version = 16
    override val blocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.CRIMSON_FUNGUS to OnTopOf,
            Material.CRIMSON_ROOTS to OnTopOf,
            Material.SOUL_TORCH to OnTopOf,
            Material.TWISTING_VINES to OnTopOf,
            Material.TWISTING_VINES_PLANT to OnTopOf,
            Material.WARPED_FUNGUS to OnTopOf,
            Material.WARPED_ROOTS to OnTopOf,
            Material.SOUL_WALL_TORCH to Behind,
            Material.WEEPING_VINES to Below,
            Material.WEEPING_VINES_PLANT to Below,
            Material.SOUL_LANTERN to TopOrBottom
    ))
    override val tags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
        Pair(Tag.PRESSURE_PLATES, OnTopOf)
    )

    override fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>>? {
        return arrayOf(
            Pair(Tag.CARPETS, OnTopOf),
        )
    }
}

private object Blocks17: VersionBlocks() {
    override val version = 17
    override val blocks: EnumMap<Material, FindDependentBlock> = EnumMap<Material, FindDependentBlock>(mapOf(
        Material.AMETHYST_CLUSTER to Behind,
        Material.SMALL_AMETHYST_BUD to Behind,
        Material.MEDIUM_AMETHYST_BUD to Behind,
        Material.LARGE_AMETHYST_BUD to Behind,
        Material.POINTED_DRIPSTONE to Dripstone,
        Material.AZALEA to OnTopOf,
        Material.FLOWERING_AZALEA to OnTopOf,
        Material.HANGING_ROOTS to Below,
        Material.SPORE_BLOSSOM to Below,
        Material.BIG_DRIPLEAF to OnTopOf,
        Material.CAVE_VINES to Below,
        Material.CAVE_VINES_PLANT to Below
    ))
    override fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlock>>? {
        return arrayOf(
            Pair(Tag.CARPETS, OnTopOf),
        )
    }
}

private object Blocks19: VersionBlocks() {
    override val version = 19
    override val blocks = EnumMap<Material, FindDependentBlock>(mapOf(
        Material.FROGSPAWN to OnTopOf,
    ))
    override val tags: Array<Pair<Tag<Material>, FindDependentBlock>> = arrayOf(
        Pair(Tag.WOOL_CARPETS, OnTopOf)
    )
}
