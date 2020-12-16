package net.pdevita.creeperheal2.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.CoroutineContext

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

class MinecraftCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            plugin.server.scheduler.runTask(plugin, block)
        }
    }
}

class AsyncCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            plugin.server.scheduler.runTaskAsynchronously(plugin, block)
        } else {
            block.run()
        }
    }
}

object DispatcherContainer {
    /**
     * Gets the async coroutine context.
     */
    val async: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(JavaPlugin.getPlugin(CreeperHeal2::class.java))
    }

    /**
     * Gets the sync coroutine context.
     */
    val sync: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(JavaPlugin.getPlugin(CreeperHeal2::class.java))
    }
}

/**
 * Minecraft async dispatcher.
 */
val Dispatchers.async: CoroutineContext
    get() =  DispatcherContainer.async

/**
 * Minecraft sync dispatcher.
 */
val Dispatchers.minecraft: CoroutineContext
    get() =  DispatcherContainer.sync

