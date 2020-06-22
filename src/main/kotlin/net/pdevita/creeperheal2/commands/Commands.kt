package net.pdevita.creeperheal2.commands

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Commands(private val plugin: CreeperHeal2): CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "warp" -> plugin.warpExplosions()
            }
        }
        return false
    }
}