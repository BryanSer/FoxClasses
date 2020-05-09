package com.github.bryanser.foxclasses.impl.archer.skill

import com.github.bryanser.brapi.Utils
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.Arrow
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.Motion
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector
import java.util.*

object ToxicBlast : Skill("ToxicBlast",
        "劇毒爆破",
        listOf(
                "§6向前方射出数支可以穿透和击退的箭",
                "§6(扇形范围90°，距离7，击退的距离为1)",
                "§6并附带毒伤(受到的所有伤害提升，效果随等级提升)"
        )
), Listener {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    const val distance = 7.0

    val poisoning = ConfigEntry.expressionConfig("poisoning", "%level% * 1.5 + %sx_damage%")

    val casting = hashMapOf<UUID, Int>()

    val enemy = mutableMapOf<UUID, MutableList<UUID>>()

    val time = ConfigEntry.expressionConfig("time", "%level% * 2")

    override fun init() {
    }

    override fun disable() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHit(evt: EntityDamageByEntityEvent) {
        enemy.forEach { (t, u) ->
            if (evt.entity.uniqueId in u) {
                val p = Bukkit.getPlayer(t) ?: return@forEach
                val poisoning = casting[t]?.let { poisoning()(p, it) }
                if (poisoning != null) {
                    evt.damage *= poisoning.toDouble()
                }
            }
        }
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv

        val distance = distance
        val dmg = damage()(p, lv).toDouble()

        val time = time()(p, lv).toLong()

        val loc = p.location
        val damaged = hashSetOf<Int>()

        val vecList = mutableListOf<Vector>()

        val vec = p.location.direction.normalize()
        val leftVec = vec.clone().add(Utils.getLeft(vec).multiply(0.35))
        val leftVec2 = vec.clone().add(Utils.getLeft(vec).multiply(0.5))
        val rightVec = vec.clone().add(Utils.getRight(vec).multiply(0.35))
        val rightVec2 = vec.clone().add(Utils.getRight(vec).multiply(0.5))

        vecList.add(vec)
        vecList.add(leftVec)
        vecList.add(leftVec2)
        vecList.add(rightVec)
        vecList.add(rightVec2)

        vecList.forEach {
            emission(p, loc, distance, it, dmg, damaged)
        }
        Bukkit.getScheduler().runTaskLater(Main.Plugin, fun() {
            enemy.remove(p.uniqueId)
        }, 20 * time)
    }

    private fun emission(p: Player,
                         loc: Location,
                         distance: Double,
                         vector: Vector,
                         dmg: Double,
                         damaged: HashSet<Int>) {
        Arrow.cast(p, loc, distance, vector, true) {
            if (it != p && it.entityId !in damaged) {
                damaged.add(it.entityId)
                it.damage(dmg, p)
                Motion.knock(p, it, 1.0)
                enemy[p.uniqueId]?.add(it.uniqueId)
            }
        }
    }


}

