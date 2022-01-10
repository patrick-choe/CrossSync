package io.github.patrick.crosssync.bukkit

import io.github.patrick.crosssync.FriendlyByteBuf
import io.github.patrick.crosssync.Utils.REDIS
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.netty.buffer.Unpooled
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class BukkitPlugin : JavaPlugin() {
    override fun onEnable() {
        val decoder = Base64.getDecoder()
        val encoder = Base64.getEncoder()

        server.messenger.registerIncomingPluginChannel(this, "cross-sync:save") { _, _, message ->
            val player = server.getPlayerExact(message.decodeToString()) ?: return@registerIncomingPluginChannel
            val inventory = serialize(player.inventory.contents)

            val byteBufOut = FriendlyByteBuf(Unpooled.buffer())
            byteBufOut.writeUtf(player.name)
            byteBufOut.writeByteArray(inventory)
            server.scheduler.runTaskLater(this, Runnable {
                redis.publish("cross-sync:inv", encoder.encodeToString(byteBufOut.array()))
            }, 100L)
        }

        pubSub.statefulConnection.addListener(object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                when (channel) {
                    "cross-sync:inv" -> {
                        val byteBuf = FriendlyByteBuf(Unpooled.wrappedBuffer(decoder.decode(message)))
                        val playerName = byteBuf.readUtf()
                        inventories[playerName] = deserialize(byteBuf.readByteArray())
                        completes.add(playerName)

                        blocks.remove(playerName)?.let { obj ->
                            synchronized(obj) {
                                obj.notifyAll()
                            }
                        }
                    }
                    "cross-sync:invalidate" -> {
                        inventories.remove(message)
                        completes.remove(message)
                        blocks.remove(message)?.let { obj ->
                            synchronized(obj) {
                                obj.notifyAll()
                            }
                        }
                    }
                }
            }
        })

        pubSub.subscribe("cross-sync:inv", "cross-sync:invalidate")

        val listener = BukkitListener(redis, blocks, completes, inventories)
        server.pluginManager.registerEvents(listener, this)
    }

    override fun onDisable() {
        pubSubConnection.close()
        redisConnection.close()
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    companion object {
        private val pubSubConnection = REDIS.connectPubSub()
        private val redisConnection = REDIS.connect()
        private val pubSub = pubSubConnection.async()
        private val redis = redisConnection.sync()
        private val blocks = mutableMapOf<String, Object>()
        private val completes = mutableSetOf<String>()
        private val inventories = mutableMapOf<String, Array<out ItemStack>>()

        private fun serialize(inventory: Array<out ItemStack?>?): ByteArray {
            return ByteArrayOutputStream().use { stream ->
                GZIPOutputStream(stream).use { gzipStream ->
                    BukkitObjectOutputStream(gzipStream).use { bukkitStream ->
                        bukkitStream.writeObject(inventory)
                    }

                    stream.toByteArray()
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun deserialize(data: ByteArray): Array<out ItemStack> {
            return ByteArrayInputStream(data).use { stream ->
                GZIPInputStream(stream).use { gzipStream ->
                    BukkitObjectInputStream(gzipStream).use { bukkitStream ->
                        bukkitStream.readObject() as Array<out ItemStack>
                    }
                }
            }
        }
    }
}