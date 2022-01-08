package io.github.patrick.crosssync.bukkit

import io.lettuce.core.api.sync.RedisCommands
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class BukkitListener(
    private val sync: RedisCommands<String, String>,
    private val blocks: MutableMap<String, Object>,
    private val completes: MutableMap<String, Boolean>,
    private val inventories: MutableMap<String, Array<out ItemStack>>
) : Listener {
    @EventHandler
    fun on(event: AsyncPlayerPreLoginEvent) {
        if (sync.get("cross-sync:ready:${event.name}") == "false") {
            sync.set("cross-sync:ready:${event.name}", "true")
        } else {
            if (completes[event.name] != true) {
                val obj = Object()
                blocks[event.name] = obj
                synchronized(obj) {
                    obj.wait()
                }
            }
            completes[event.name] = false
        }
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        inventories.remove(event.player.name)?.let { inventory ->
            event.player.inventory.setContents(inventory)
        }
    }
}