package com.github.bryanser.foxclasses.impl.warrior.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Color
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Windbreak : Skill("Windbreak",
        "风之结界",
        listOf(
                "§6释放以自身为半径3格的圆形旋风",
                "§6每秒造成伤害(旋风跟随施法者)",
                "§6持续4秒(效果:少量白色粒子螺旋升天)"
        )
) {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val round = ConfigEntry.mapConfig("round", mapOf(1 to 3, 2 to 4, 3 to 5))

    val casting = hashMapOf<UUID, Int>()

    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val dmg = damage()(p, lv).toDouble()
        val time = 4.0

        val round = round()(lv).toDouble()
        val damaged = mutableSetOf<UUID>()

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (t % 20 == 0) {
                    damaged.clear()
                }
                if (t >= time * 20) {
                    this.cancel()
                    return
                }
                val loc = p.location

                /**
                 * 特效
                 */
                ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.RED), loc, 50.0)

                /**
                 * 每秒造成伤害
                 */
                for (e in loc.world.getNearbyEntities(loc, round, round, round)) {
                    if (e is LivingEntity && e.uniqueId !in damaged) {
                        damaged.add(e.uniqueId)
                        e.damage(dmg)
                    }
                }

                t++
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 1)
    }


}