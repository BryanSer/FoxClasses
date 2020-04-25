package com.github.bryanser.foxclasses.impl.warrior.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.SpeedManager
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Guardian : Skill("Guardian",
        "守护",
        listOf(
                "§6嘲讽自身为半径3格的怪物并减速他们",
                "§6其仇恨转移到施法者身上",
                "§6施法者自身获得百分比伤害减免",
                "§6持续4秒(期间施法者脚底围绕少量红色)"
        )
), Listener {
    val round = ConfigEntry.mapConfig("round", mapOf(1 to 3, 2 to 4, 3 to 5))

    val damageReductionRate = ConfigEntry.mapConfig("damageReductionRate", mapOf(1 to 0.3, 2 to 0.4, 3 to 0.5))

    val casting = hashMapOf<UUID, Int>()

    private val guardianList = hashMapOf<UUID, Double>()

    override fun init() {
    }

    override fun disable() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHitGuardian(evt: EntityDamageByEntityEvent) {
        if (evt.entity.uniqueId in guardianList) {
            evt.damage *= guardianList[evt.entity.uniqueId]!!
        }
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val time = 4.0

        val loc = p.location
        val round = round()(lv).toDouble()

        for (e in loc.world.getNearbyEntities(loc, round, round, round)) {
            if (e is LivingEntity) {
                /**
                 * 嘲讽
                 */

                /**
                 * 减速度
                 */
                SpeedManager.newData().also {
                    it.modifier = -0.3
                    it.timeLength = time
                    SpeedManager.addEffect(p, e, it)
                }
            }
        }

        /**
         * 特效
         */
        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (t >= time * 20) {
                    this.cancel()
                    return
                }
                val loc = p.location
                ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.RED), loc, 50.0)
                t++
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 1)

        /**
         * 减伤
         */
        val damageReductionRate = damageReductionRate()(lv)

        guardianList[p.uniqueId] = damageReductionRate
        Bukkit.getScheduler().runTaskLater(Main.Plugin, fun() {

        }, time.toLong() * 20)
    }


}