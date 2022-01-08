package io.github.patrick.crosssync.bungee

import io.github.patrick.crosssync.Utils.REDIS
import net.md_5.bungee.api.plugin.Plugin

class BungeePlugin : Plugin() {
    private val redisConnection = REDIS.connect()
    private val redis = redisConnection.sync()

    override fun onEnable() {
        proxy.pluginManager.registerListener(this, BungeeListener(redis))
    }

    override fun onDisable() {
        redis.shutdown(false)
        redisConnection.close()
    }
}