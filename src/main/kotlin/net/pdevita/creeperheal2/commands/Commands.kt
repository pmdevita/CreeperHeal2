package net.pdevita.creeperheal2.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.pdevita.creeperheal2.CreeperHeal2
import net.pdevita.creeperheal2.utils.async
import net.pdevita.creeperheal2.utils.minecraft
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

class Commands(private val plugin: CreeperHeal2): CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            return when (args[0]) {
                "warp" -> this.warpExplosions(sender, args)
                "stats" -> this.stats(sender, args)
                "cancel" -> this.cancelExplosions(sender, args)
                else -> false
            }
        }
        return false
    }

    private fun stats(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (!sender.hasPermission("creeperheal2.stats")) {
                return false
            }
        }

        val runtime = Runtime.getRuntime()
        val memory = "Server Memory Usage: ${floor(((runtime.totalMemory() - runtime.freeMemory())/1000000).toDouble()).toInt()}MB/${floor((runtime.totalMemory()/1000000).toDouble()).toInt()}MB (${floor((runtime.maxMemory()/1000000).toDouble()).toInt()}MB)"
        sender.sendMessage(memory)
        GlobalScope.launch(Dispatchers.async) {
            val explosions = plugin.manager.getExplosions()
            GlobalScope.launch(Dispatchers.minecraft) {
                sender.sendMessage("Current Explosions: ${explosions.size}")
            }
            explosions.forEachIndexed { i, explosion ->
                val finishedPost: String = when (explosion.postProcessComplete.get()) {
                    true -> "Waiting..."
                    false -> "Post-processing..."
                }
                val status: String = if (explosion.replaceCounter > 0) {
                    "Replacing ${explosion.replaceCounter}/${explosion.totalBlockList.size}"
                } else {
                    finishedPost
                }
                GlobalScope.launch(Dispatchers.minecraft) {
                    sender.sendMessage("Explosion $i (${explosion.boundary}): $status")
                }
            }
        }
        return true
    }

    private fun warpExplosions(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (!sender.hasPermission("creeperheal2.warp")) {
                return false
            }
        }
        plugin.warpExplosions()
        sender.sendMessage("Warping explosions...")
        return true
    }

    private fun cancelExplosions(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (!sender.hasPermission("creeperheal2.cancel")) {
                return false
            }
        }
        plugin.cancelExplosions()
        sender.sendMessage("Cancelling explosions...")
        return true
    }

}