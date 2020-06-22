package net.pdevita.creeperheal2.utils

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

// Thanks to Shynixn for https://github.com/Shynixn/MCCoroutine

/**
 * Executes the given [f] via the [plugin] asynchronous.
 */
fun Any.async(plugin : Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit): BukkitTask {
    return if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, f, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLaterAsynchronously(plugin, f, delayTicks)
    }
}

/**
 * Executes the given [f] via the [plugin] synchronized with the server tick.
 */
fun Any.sync(plugin : Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit): BukkitTask {
    return if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimer(plugin, f, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLater(plugin, f, delayTicks)
    }
}

