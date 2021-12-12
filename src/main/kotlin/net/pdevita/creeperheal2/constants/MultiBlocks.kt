package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.*
import org.bukkit.Material
import java.util.*

class MultiBlocks(version: Pair<Int, Int>) {
    val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(Material::class.java)

    init {
        if (version.second >= 13) {
            this.getVersionBlocks(version.second, 13, MBlocks13())
        }
        if (version.second >= 16) {
            this.getVersionBlocks(version.second, 16, MBlocks16())
        }
        if (version.second >= 17) {
            this.getVersionBlocks(version.second, 17, MBlocks17())
        }
    }

    private fun getVersionBlocks(spigotVersion: Int, thisVersion: Int, blocks: MVersionBlocks) {
        this.blocks.putAll(blocks.blocks)
        if (spigotVersion == thisVersion) {
            blocks.versionBlocks()?.let { this.blocks.putAll(it) }
        }
    }
}

private open class MVersionBlocks {
    open val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(org.bukkit.Material::class.java)
    open fun versionBlocks(): EnumMap<Material, FindDependentBlocks>? { return null }
}


private class MBlocks13: MVersionBlocks() {
    override val blocks = EnumMap<Material, FindDependentBlocks>(mapOf(
            Material.ACACIA_DOOR to DoorMultiBlock,
            Material.BIRCH_DOOR to DoorMultiBlock,
            Material.DARK_OAK_DOOR to DoorMultiBlock,
            Material.IRON_DOOR to DoorMultiBlock,
            Material.JUNGLE_DOOR to DoorMultiBlock,
            Material.OAK_DOOR to DoorMultiBlock,
            Material.SPRUCE_DOOR to DoorMultiBlock,
            Material.BLACK_BED to Bed,
            Material.BLUE_BED to Bed,
            Material.BROWN_BED to Bed,
            Material.CYAN_BED to Bed,
            Material.GRAY_BED to Bed,
            Material.GREEN_BED to Bed,
            Material.LIGHT_BLUE_BED to Bed,
            Material.LIGHT_GRAY_BED to Bed,
            Material.LIME_BED to Bed,
            Material.MAGENTA_BED to Bed,
            Material.ORANGE_BED to Bed,
            Material.PINK_BED to Bed,
            Material.PURPLE_BED to Bed,
            Material.RED_BED to Bed,
            Material.WHITE_BED to Bed,
            Material.YELLOW_BED to Bed
    ))
}


private class MBlocks16: MVersionBlocks() {
    override val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(mapOf(
            Material.CRIMSON_DOOR to DoorMultiBlock,
            Material.WARPED_DOOR to DoorMultiBlock
    ))
}

private class MBlocks17: MVersionBlocks() {
    override val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(mapOf(
        Material.SMALL_DRIPLEAF to SmallDripLeaf,
        Material.BIG_DRIPLEAF_STEM to BigDripLeafStem,
        Material.GLOW_LICHEN to GlowLichen
    ))
}
