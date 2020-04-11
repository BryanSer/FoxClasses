package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.entity.Player
import java.util.*

abstract class Skill(
        val name: String
) {

    val maxLevel = ConfigEntry<Int>("maxLevel", 3)

    val cooldown = ConfigEntry<(Int) -> Int>("cooldown", provider = fun(cs, ce): (Int) -> Int {
        val e = cs.getConfigurationSection("cooldown")
        val cd = hashMapOf<Int, Int>()
        for (key in e.getKeys(false)) {
            cd[key.toInt()] = e.getInt(key)
        }
        return fun(t): Int {
            return cd[t] ?: -1
        }
    }) { cs, ce ->
        val e = cs.createSection("cooldown")
        e["1"] = 5000
        e["2"] = 4000
        e["3"] = 3000
    }

    val lastCast = hashMapOf<UUID,Long>()


    fun tryCast(p:Player){
        val last = lastCast[p.uniqueId] ?: 0L
        val pass = System.currentTimeMillis() - last

    }

    abstract fun cast(p: Player)


}
