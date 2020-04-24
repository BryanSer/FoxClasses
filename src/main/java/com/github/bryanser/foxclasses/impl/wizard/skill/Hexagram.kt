package com.github.bryanser.foxclasses.impl.wizard.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.SpeedManager
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Hexagram : Skill("Hexagram",
        "六芒星阵",
        listOf(
                "§6在鼠标方向区域形成一个半径为2的绿色圆形六芒星阵(选取范围为15)持续3秒",
                "§6区域内的怪物会减速3秒",
                "§6并且每0.5秒持续掉血持续3秒"
        )
), Listener {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))

    val time = ConfigEntry.mapConfig("distance", mapOf(1 to 3, 2 to 3, 3 to 3))

    val casting = hashMapOf<UUID, Int>()

    val enemy = mutableMapOf<LivingEntity, EnemyData>()

    data class EnemyData(
            var t: Int,
            var dmg: Double
    )

    override fun init() {
        object : BukkitRunnable() {
            override fun run() {
                enemy.forEach { (e, data) ->
                    e.damage(data.dmg)
                }
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 10)
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val target = p.getTargetBlock(mutableSetOf(Material.AIR), 50).location ?: return
        val dmg = damage()(p, lv).toDouble()
        val time = time()(lv).toDouble()
        val round = 2.0
        val damaged = mutableListOf<UUID>()

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (t >= time * 20) {
                    this.cancel()
                    return
                }

                for (e in target.world.getNearbyEntities(target, round, round, round)) {
                    if (e is LivingEntity && e != p && e.uniqueId !in damaged) {
                        damaged += e.uniqueId
                        e.damage(dmg, p)

                        SpeedManager.newData().also {
                            it.modifier = -0.2
                            it.timeLength = 3.0
                            SpeedManager.addEffect(p, e, it)
                        }

                        enemy[e] = EnemyData(5, dmg)
                    }
                }
                t++
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 1)
    }


}