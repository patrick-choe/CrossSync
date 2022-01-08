package io.github.patrick.crosssync

import io.lettuce.core.RedisClient
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object Utils {
    val REDIS = RedisClient.create("redis://localhost")

    fun serialize(inventory: Array<out ItemStack?>?): ByteArray {
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
    fun deserialize(data: ByteArray): Array<out ItemStack> {
        return ByteArrayInputStream(data).use { stream ->
            GZIPInputStream(stream).use { gzipStream ->
                BukkitObjectInputStream(gzipStream).use { bukkitStream ->
                    bukkitStream.readObject() as Array<out ItemStack>
                }
            }
        }
    }
}