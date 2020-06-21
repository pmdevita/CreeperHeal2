package net.pdevita.creeperheal2.constants

import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

// List of blocks that are affected by gravity in this version of MC

class GravityBlocks {
    var blocks13 = ArrayList<Material>(listOf(
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.DRAGON_EGG,
            Material.GRAVEL,
            Material.RED_SAND,
            Material.SAND
    ))
    var blocks14 = ArrayList<Material>(listOf(
            Material.SCAFFOLDING
    ))
    var blocks15 = ArrayList<Material>()
    var concrete = ArrayList<Material>(listOf(
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
    constructor() {
    }

    fun getBlocks(): HashSet<Material> {

        var materials = HashSet<Material>()
        materials.addAll(blocks13)
        materials.addAll(blocks14)
        materials.addAll(concrete)
        return materials
    }
}