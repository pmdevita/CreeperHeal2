package net.pdevita.creeperheal2.commands

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Commands(private val plugin: CreeperHeal2): CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        var player: Player? = null
        if (sender is Player) {
            player = sender
        }

        if (args.isNotEmpty()) {
            if (player != null) {
                when (args[0]) {
                    "warp" -> if (player.hasPermission("creeperheal.warp")) { plugin.warpExplosions() }
                }
            } else {
                when (args[0]) {
                    "warp" -> plugin.warpExplosions()
                }
            }
        }
        return false
    }
}