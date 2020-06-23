package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.Material


class ConstantsManager(private val plugin: CreeperHeal2) {
    val gravityBlocks: HashSet<Material>
    val dependentBlocks: DependentBlocks
    private val versionList = ArrayList<Int>()

    init {
        // Version extracting test, just here for convenience/testing
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        version = splitVersion[0] + splitVersion[1]
        versionList.addAll(listOf(splitVersion[0].toInt(), splitVersion[1].toInt()))
        gravityBlocks = GravityBlocks().getBlocks(versionList)
        dependentBlocks = DependentBlocks(versionList)
    }
}
