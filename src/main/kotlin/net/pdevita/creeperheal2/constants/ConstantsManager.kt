package net.pdevita.creeperheal2.constants

import org.bukkit.Bukkit
import org.bukkit.Material


class ConstantsManager {
    val gravityBlocks: HashSet<Material>
    val dependentBlocks: DependentBlocks
    val multiBlocks: MultiBlocks
    private val versionList = ArrayList<Int>()

    init {
        // Version extracting test, just here for convenience/testing
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        versionList.addAll(listOf(splitVersion[0].toInt(), splitVersion[1].toInt()))
        gravityBlocks = GravityBlocks().getBlocks(versionList)
        dependentBlocks = DependentBlocks(versionList)
        multiBlocks = MultiBlocks(versionList)
    }
}
