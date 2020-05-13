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

    override fun toString(): String {
        return this().toString()
    }

    inline operator fun invoke(): T = data as T

    inline fun <reified R> get(): R {
        if (data is R) {
            return data as R
        } else {
            throw IllegalStateException()
        }
    }

    companion object {

        fun getEntry(obj: Any): List<ConfigEntry<*>> {
            val list = mutableListOf<ConfigEntry<*>>()
            var curr: Class<*> = obj::class.java
            while (curr != Object::class.java) {
                for (df in curr.declaredFields) {
                    if (ConfigEntry::class.java.isAssignableFrom(df.type)) {
                        df.isAccessible = true
                        list.add(df.get(obj) as ConfigEntry<*>)
                    }
                }
                curr = curr.superclass as Class<*>
            }
            return list
        }

        fun colorConfig(key: String, default: String): ConfigEntry<String> = ConfigEntry(key, provider = { cs, ce ->
            ChatColor.translateAlternateColorCodes('&', cs.getString(ce.key))
        }) { cs, ce ->
            cs[ce.key] = default.replace("§", "&")
        }

        fun colorConfig(key: String, default: List<String>): ConfigEntry<List<String>> = ConfigEntry(key, provider = { cs, ce ->
            cs.getStringList(ce.key).map {
                ChatColor.translateAlternateColorCodes('&', it)
            }
        }) { cs, ce ->
            cs[ce.key] = default.map { it.replace("§", "&") }
        }

        fun expressionConfig(key: String, default: String): ConfigEntry<Expression> {
            return ConfigEntry<Expression>(key, null, fun(cs, ce): Expression {
                val s = cs.getString(ce.key, default)
                return ExpressionHelper.compileExpression(s)
            }) { cs, ce ->
                cs[ce.key] = default
            }
        }


        fun booleanExpressionConfig(key: String, default: String): ConfigEntry<Expression> {
            return ConfigEntry<Expression>(key, null, fun(cs, ce): Expression {
                val s = cs.getString(ce.key, default)
                return ExpressionHelper.compileExpression(s, true)
            }) { cs, ce ->
                cs[ce.key] = default
            }
        }

        fun <V> mapConfig(key: String, map: Map<Int, V>): ConfigEntry<(Int) -> V> {
            return ConfigEntry<(Int) -> V>(key, provider = fun(cs, ce): (Int) -> V {
                val e = cs.getConfigurationSection(key)
                val lv = hashMapOf<Int, V>()
                var max = 1
                for (key in e.getKeys(false)) {
                    lv[key.toInt()] = e.get(key) as V
                    if (key.toInt() > max) {
                        max = key.toInt()
                    }
                }
                return fun(t): V {
                    return lv[t] ?: lv[max]!!
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
