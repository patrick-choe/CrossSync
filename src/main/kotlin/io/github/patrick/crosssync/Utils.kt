package io.github.patrick.crosssync

import io.lettuce.core.RedisClient

object Utils {
    val REDIS: RedisClient = RedisClient.create("redis://localhost")
}