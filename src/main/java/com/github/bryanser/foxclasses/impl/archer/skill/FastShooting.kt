package com.github.bryanser.foxclasses.impl.archer.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.Arrow
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object FastShooting : Skill("FastShooting",
        "快速射击",
        listOf(
                "§6向鼠标所指位置快速射出10支无重力箭对第一个目标造成伤害"
        )
) {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    const val distance = 50.0

    val casting = hashMapOf<UUID, Int>()

    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val target = p.getTargetBlock(mutableSetOf(Material.AIR), 50).location.direction ?: return

        val distance = distance
        val dmg = damage()(p, lv).toDouble()

        val loc = p.location
        object : BukkitRunnable() {
            val damaged = hashSetOf<Int>()
            var amount = 0
            override fun run() {
                if (amount++ >= 10) {
                    cancel()
                    return
                }
                Arrow.cast(p, loc, distance, target, false) {
                    if (it != p && it.entityId !in damaged) {
                        damaged.add(it.entityId)
                        it.damage(dmg, p)
                    }
                }
            }

        }.runTaskTimer(Main.Plugin, 1, 1)

    }


}