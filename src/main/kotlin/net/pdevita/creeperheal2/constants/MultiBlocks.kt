package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.utils.Bed
import net.pdevita.creeperheal2.utils.Door
import net.pdevita.creeperheal2.utils.FindDependentBlock
import org.bukkit.Material
import java.util.*

class MultiBlocks(private val version: ArrayList<Int>) {
    val blocks: EnumMap<Material, FindDependentBlock> = EnumMap(Material::class.java)

    init {
        if (version[1] >= 13) {
            this.getVersionBlocks(version[1], 13, MBlocks13())
        }
        if (version[1] >= 16) {
            this.getVersionBlocks(version[1], 16, MBlocks16())
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
    open val blocks: EnumMap<Material, FindDependentBlock> = EnumMap(org.bukkit.Material::class.java)
    open fun versionBlocks(): EnumMap<Material, FindDependentBlock>? { return null }
}


private class MBlocks13: MVersionBlocks() {
    override val blocks = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.ACACIA_DOOR to Door,
            Material.BIRCH_DOOR to Door,
            Material.DARK_OAK_DOOR to Door,
            Material.IRON_DOOR to Door,
            Material.JUNGLE_DOOR to Door,
            Material.OAK_DOOR to Door,
            Material.SPRUCE_DOOR to Door,
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
    override val blocks = EnumMap<Material, FindDependentBlock>(mapOf(
            Material.CRIMSON_DOOR to Door,
            Material.WARPED_DOOR to Door
    ))
}
