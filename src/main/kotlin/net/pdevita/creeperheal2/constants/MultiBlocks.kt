package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.BedMultiBlock
import net.pdevita.creeperheal2.utils.DoorMultiBlock
import net.pdevita.creeperheal2.utils.FindDependentBlocks
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
            Material.BLACK_BED to BedMultiBlock,
            Material.BLUE_BED to BedMultiBlock,
            Material.BROWN_BED to BedMultiBlock,
            Material.CYAN_BED to BedMultiBlock,
            Material.GRAY_BED to BedMultiBlock,
            Material.GREEN_BED to BedMultiBlock,
            Material.LIGHT_BLUE_BED to BedMultiBlock,
            Material.LIGHT_GRAY_BED to BedMultiBlock,
            Material.LIME_BED to BedMultiBlock,
            Material.MAGENTA_BED to BedMultiBlock,
            Material.ORANGE_BED to BedMultiBlock,
            Material.PINK_BED to BedMultiBlock,
            Material.PURPLE_BED to BedMultiBlock,
            Material.RED_BED to BedMultiBlock,
            Material.WHITE_BED to BedMultiBlock,
            Material.YELLOW_BED to BedMultiBlock
    ))
}


private class MBlocks16: MVersionBlocks() {
    override val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(mapOf(
            Material.CRIMSON_DOOR to DoorMultiBlock,
            Material.WARPED_DOOR to DoorMultiBlock
    ))
}
