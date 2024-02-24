package net.pdevita.creeperheal2.events

import net.pdevita.creeperheal2.core.Boundary
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CHExplosionEvent(val blockList: MutableList<Block>, val world: World, val boundary: Boundary): Event() {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}