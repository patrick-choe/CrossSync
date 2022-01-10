package io.github.patrick.crosssync.bukkit

import io.lettuce.core.api.sync.RedisCommands
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class BukkitListener(
    private val redis: RedisCommands<String, String>,
    private val blocks: MutableMap<String, Object>,
    private val completes: MutableSet<String>,
    private val inventories: MutableMap<String, Array<out ItemStack>>
) : Listener {
    @EventHandler
    fun on(event: AsyncPlayerPreLoginEvent) {
        if (redis.get("cross-sync:ready:${event.name}") == "false") {
            redis.set("cross-sync:ready:${event.name}", "true")
        } else {
            if (!completes.contains(event.name)) {
                val obj = Object()
                blocks[event.name] = obj
                synchronized(obj) {
                    obj.wait()
                }
            }
            completes.remove(event.name)
        }
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        redis.publish("cross-sync:invalidate", event.player.name)
        inventories.remove(event.player.name)?.let { inventory ->
            event.player.inventory.setContents(inventory)
        }
    }
}