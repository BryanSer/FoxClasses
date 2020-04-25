package com.github.bryanser.foxclasses.impl.archer.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

object CloudArrow : Skill("CloudArrow",
        "穿云箭",
        listOf(
                "§6延迟1秒后向鼠标所指位置发射一根速度极快不会下落的箭造成伤害",
                "§6可穿透(距离25)"
        )
) {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))

    val casting = hashMapOf<UUID, Int>()

    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val target = p.getTargetBlock(mutableSetOf(Material.AIR), 50).location.direction ?: return

        val distance = distance()(lv).toDouble()
        val dmg = damage()(p, lv).toDouble()

        Bukkit.getScheduler().runTaskLater(Main.Plugin, fun() {
            val loc = p.location
            val damaged = hashSetOf<Int>()

            Arrow.cast(p, loc, distance, target,true) {
                if (it != p && it.entityId !in damaged) {
                    damaged.add(it.entityId)
                    it.damage(dmg, p)
                }
            }
        }, 20)
    }


}

