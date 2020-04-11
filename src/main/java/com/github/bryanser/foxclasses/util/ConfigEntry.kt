package com.github.bryanser.foxclasses.util

import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection

open class ConfigEntry<T>(
        val key: String,
        defaultValue: T? = null,
        val provider: (ConfigurationSection, ConfigEntry<T>) -> T = { cs, ce -> cs.get(ce.key, defaultValue) as T },
        val writer: (ConfigurationSection, ConfigEntry<T>) -> Unit = { cs, ce ->
            if (defaultValue != null) {
                cs[ce.key] = defaultValue
            }
        }
) {
    var data: T? = null

    fun load(cs: ConfigurationSection): Boolean {
        var modify = false
        if (!cs.contains(key)) {
            writer(cs, this)
            modify = true
        }
        data = provider(cs, this)
        return modify
    }

    operator fun invoke(): T = data as T

    inline fun <reified R> get(): R {
        if (data is R) {
            return data as R
        } else {
            throw IllegalStateException()
        }
    }

    companion object {
        fun colorConfig(key: String, default: String): ConfigEntry<String> = ConfigEntry(key, provider = { cs, ce ->
            ChatColor.translateAlternateColorCodes('&', cs.getString(ce.key))
        }) { cs, ce ->
            cs[ce.key] = default.replace("§", "&")
        }

        fun <V> mapConfig(key: String, map: Map<Int, V>): ConfigEntry<(Int) -> V?> {
            return ConfigEntry<(Int) -> V?>(key, provider = fun(cs, ce): (Int) -> V? {
                val e = cs.getConfigurationSection(key)
                val lv = hashMapOf<Int, V>()
                for (key in e.getKeys(false)) {
                    lv[key.toInt()] = e.get(key) as V
                }
                return fun(t): V? {
                    return lv[t]
                }
            }) { cs, ce ->
                val e = cs.createSection(key)
                for ((k, v) in map) {
                    e[k.toString()] = v
                }
            }
        }
    }
}
