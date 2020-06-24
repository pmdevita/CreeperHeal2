package net.pdevita.creeperheal2.utils

import net.pdevita.creeperheal2.CreeperHeal2
import org.bstats.bukkit.Metrics
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger

class Stats(private val plugin: CreeperHeal2) {
    private val metrics = Metrics(plugin, 7940)
    val totalExplosions = AtomicInteger(0)
    val totalBlocks = AtomicInteger(0)

    init {
        metrics.addCustomChart(Metrics.SingleLineChart("explosions_restored_over_time", Callable {
            return@Callable totalExplosions.getAndSet(0)
        }))
        metrics.addCustomChart(Metrics.SingleLineChart("blocks_replaced", Callable {
            return@Callable totalBlocks.getAndSet(0)
        }))
    }
}