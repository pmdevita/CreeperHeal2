package net.pdevita.creeperheal2.constants

import org.bukkit.Bukkit
import org.bukkit.Material


class ConstantsManager {
    val gravityBlocks: HashSet<Material>
    val dependentBlocks: DependentBlocks

    init {
        // Version extracting test, just here for convenience/testing
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        version = splitVersion[0] + splitVersion[1]
        print(version)
        gravityBlocks = GravityBlocks().getBlocks(version)
        dependentBlocks = DependentBlocks(version)
    }
}
