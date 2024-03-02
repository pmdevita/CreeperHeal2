package net.pdevita.creeperheal2.constants

import com.google.auto.service.AutoService
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.*
import org.bukkit.Material
import org.bukkit.Tag
import java.util.*

class MultiBlocks(version: Pair<Int, Int>, plugin: CreeperHeal2) {
    val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(Material::class.java)

    init {
        val blockLoader = ServiceLoader.load(MVersionBlocks::class.java, plugin.javaClass.classLoader)
        for (versionBlocks in blockLoader) {
            this.getVersionBlocks(version.second, versionBlocks)
        }
    }

    private fun getVersionBlocks(spigotVersion: Int, versionBlocks: MVersionBlocks) {
        if (spigotVersion >= versionBlocks.version) {
            versionBlocks.blocks?.let { this.blocks.putAll(it) }
            versionBlocks.tags?.let { addTagMap(it) }
        }
        if (spigotVersion == versionBlocks.version) {
            versionBlocks.versionBlocks()?.let { this.blocks.putAll(it) }
            versionBlocks.versionTags()?.let { addTagMap(it) }
        }
    }

    private fun addTagMap(tagMap: Array<Pair<Tag<Material>, FindDependentBlocks>>) {
        for (tag in tagMap) {
            for (block in tag.first.values) {
                blocks[block] = tag.second
            }
        }
    }
}

private interface MVersionBlocks {
    val version: Int
    val blocks: EnumMap<Material, FindDependentBlocks>?
        get() = null
    val tags: Array<Pair<Tag<Material>, FindDependentBlocks>>?
        get() = null
    fun versionBlocks(): EnumMap<Material, FindDependentBlocks>? {
        return null
    }
    fun versionTags(): Array<Pair<Tag<Material>, FindDependentBlocks>>? {
        return null
    }
}


private object MBlocks13: MVersionBlocks {
    override val version = 13
    override val blocks = EnumMap<Material, FindDependentBlocks>(mapOf(
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
            Material.YELLOW_BED to Bed,
            Material.VINE to Vine
    ))
    override val tags: Array<Pair<Tag<Material>, FindDependentBlocks>>
        get() {
            return arrayOf(
                Pair(Tag.DOORS, Door)
            )
        }
}
@AutoService(MVersionBlocks::class)
class MBlocks13Proxy : MVersionBlocks by MBlocks13

private object MBlocks17: MVersionBlocks {
    override val version = 17
    override val blocks: EnumMap<Material, FindDependentBlocks> = EnumMap(mapOf(
        Material.SMALL_DRIPLEAF to SmallDripLeaf,
        Material.BIG_DRIPLEAF_STEM to BigDripLeafStem,
        Material.GLOW_LICHEN to GlowLichen
    ))
}
@AutoService(MVersionBlocks::class)
class MBlocks17Proxy : MVersionBlocks by MBlocks17
