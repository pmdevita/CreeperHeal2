package net.pdevita.creeperheal2.constants

import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

// List of blocks that are affected by gravity in this version of MC

class GravityBlocks() {
    private var blocks13 = ArrayList<Material>(listOf(
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.DRAGON_EGG,
            Material.GRAVEL,
            Material.RED_SAND,
            Material.SAND
    ))
    private var blocks14 = ArrayList<Material>(listOf(
            Material.SCAFFOLDING
    ))
    private var blocks15 = ArrayList<Material>()
    private var concrete = ArrayList<Material>(listOf(
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

    fun getBlocks(version: ArrayList<Int>): HashSet<Material> {
        val materials = HashSet<Material>()
        if (version[1] >= 13) {
            materials.addAll(blocks13)
        }
        if (version[1] >= 14) {
            materials.addAll(blocks14)
        }
        if (version[1] >= 15) {
            materials.addAll(concrete)
        }
        return materials
    }
}