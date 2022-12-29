package net.pdevita.creeperheal2.constants

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.Material


class ConstantsManager(plugin: CreeperHeal2) {
    val version: Pair<Int, Int> = getServerVersion()
    val gravityBlocks: HashSet<Material> = GravityBlocks().getBlocks(version)
    val dependentBlocks: DependentBlocks = DependentBlocks(version, plugin)
    val multiBlocks: MultiBlocks = MultiBlocks(version)

    private fun getServerVersion(): Pair<Int, Int> {
        var version = Bukkit.getBukkitVersion()
        version = version.substringBefore("-")
        val splitVersion = version.split(".")
        return Pair(splitVersion[0].toInt(), splitVersion[1].toInt())
    }
}
