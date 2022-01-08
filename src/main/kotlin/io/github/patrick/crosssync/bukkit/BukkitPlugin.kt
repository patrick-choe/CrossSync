package io.github.patrick.crosssync.bukkit

import io.github.patrick.crosssync.Utils.REDIS
import io.github.patrick.crosssync.Utils.deserialize
import io.github.patrick.crosssync.Utils.serialize
import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.Base64

class BukkitPlugin : JavaPlugin() {
    override fun onEnable() {
        val decoder = Base64.getDecoder()
        val encoder = Base64.getEncoder()

        pubSub.statefulConnection.addListener(object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                val player = server.getPlayerExact(message) ?: return
                val inventory = encoder.encodeToString(serialize(player.inventory.contents))
                redis.publish("cross-sync:inv:${player.name}", inventory)
            }

            override fun message(pattern: String, channel: String, message: String) {
                val playerName = channel.removePrefix("cross-sync:inv:")
                inventories[playerName] = deserialize(decoder.decode(message))

                completes[playerName] = true
                blocks.remove(playerName)?.let { obj ->
                    synchronized(obj) {
                        obj.notifyAll()
                    }
                }
            }
        })
        pubSub.subscribe("cross-sync:save")
        pubSub.psubscribe("cross-sync:inv:*")

        val listener = BukkitListener(redis, blocks, completes, inventories)
        server.pluginManager.registerEvents(listener, this)
    }

    override fun onDisable() {
        pubSub.shutdown(false)
        pubSubConnection.close()
        redis.shutdown(false)
        redisConnection.close()
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    companion object {
        private val pubSubConnection = REDIS.connectPubSub()
        private val redisConnection = REDIS.connect()
        private val pubSub = pubSubConnection.async()
        private val redis = redisConnection.sync()
        private val blocks = mutableMapOf<String, Object>()
        private val completes = mutableMapOf<String, Boolean>()
        private val inventories = mutableMapOf<String, Array<out ItemStack>>()
    }
}