package io.github.patrick.crosssync.bungee

import io.lettuce.core.api.sync.RedisCommands
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeListener(
    private val redis: RedisCommands<String, String>
) : Listener {
    @EventHandler
    fun on(event: PostLoginEvent) {
        redis.set("cross-sync:ready:${event.player.name}", "false")
    }

    @EventHandler
    fun on(event: PlayerDisconnectEvent) {
        redis.set("cross-sync:ready:${event.player.name}", "false")
    }

    @EventHandler
    fun on(event: ServerConnectEvent) {
        println("${event.player.name} to ${event.target.name}")
        if (redis.get("cross-sync:ready:${event.player.name}") == "true") {
            redis.publish("cross-sync:save", event.player.name)
        }
    }
}