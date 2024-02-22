package net.pdevita.creeperheal2.constants

import org.bukkit.Material

// List of blocks that are affected by gravity in this version of MC

class GravityBlocks {
    fun getBlocks(version: Pair<Int, Int>): HashSet<Material> {
        val materials = HashSet<Material>()
        if (version.second >= 13) {
            materials.addAll(GBlocks13().blocks)
        }
        if (version.second >= 20) {
            materials.addAll(GBlocks20().blocks)
        }
        return materials
    }

}

interface GravityBlockList {
    val blocks: ArrayList<Material>
        get() = ArrayList()
}

private class GBlocks13: GravityBlockList {
    override val blocks = ArrayList<Material>(listOf(
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.DRAGON_EGG,
            Material.GRAVEL,
            Material.RED_SAND,
            Material.SAND,
            Material.BLACK_CONCRETE_POWDER,
            Material.BLUE_CONCRETE_POWDER,
            Material.BROWN_CONCRETE_POWDER,
            Material.CYAN_CONCRETE_POWDER,
            Material.GRAY_CONCRETE_POWDER,
            Material.GREEN_CONCRETE_POWDER,
            Material.LIGHT_BLUE_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.LIME_CONCRETE_POWDER,
            Material.MAGENTA_CONCRETE_POWDER,
            Material.ORANGE_CONCRETE_POWDER,
            Material.PINK_CONCRETE_POWDER,
            Material.PURPLE_CONCRETE_POWDER,
            Material.RED_CONCRETE_POWDER,
            Material.WHITE_CONCRETE_POWDER,
            Material.YELLOW_CONCRETE_POWDER
    ))
}

private class GBlocks20: GravityBlockList {
    override val blocks = ArrayList<Material>(listOf(
        Material.SUSPICIOUS_SAND,
        Material.SUSPICIOUS_GRAVEL
    ))
}

