package com.github.bryanser.foxclasses.impl.warrior.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object TheProtectionOfGod : Skill("TheProtectionOfGod",
        "神之庇护",
        listOf(
                "§6自身获得100%韧性与急迫2，持续2秒",
                "§6随后对周围方形区域7x7的范围造成一次伤害",
                "§6普通攻击范围增加2格持续15秒"
        )
), Listener {


    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")

    //    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))
//    val radius = ConfigEntry.mapConfig("radius", mapOf(1 to 7, 2 to 8, 3 to 9))
    const val radius = 7.0

    val casting = hashSetOf<UUID>()

    var ignore = false

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onDamage(evt: EntityDamageByEntityEvent) {
        if (ignore) {
            return
        }
        if (casting.contains(evt.damager.uniqueId)) {
            ignore = true
            for (e in evt.entity.getNearbyEntities(2.0, 2.0, 2.0)) {
                if (e == evt.entity || e == evt.damager || e !is LivingEntity) {
                    continue
                }
                e.damage(evt.damage, evt.damager)
            }
            ignore = false
        }
    }

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting.add(p.uniqueId)
        val dmg = damage()(p, lv).toDouble()
//        val radius = radius//()(lv).toDouble()

        /**
         * 粒子效果:人物头顶生成形似大宝剑的黄色粒子插向人物
         */
        val loc = p.location
        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (t >= 20) {
                    this.cancel()
                    return
                }

                ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.YELLOW), loc.add(0.0, -0.2, 0.0), 50.0)

                t++
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 1)

        /**
         * 自身获得100%韧性与急迫2，持续2秒
         */
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 1))
        p.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 40, 1))
        p.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 3))

        /**
         * （效果：7x7的地面出现黄色橙色交加的粒子）
         */
        ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.YELLOW), p.location, 50.0)

        for (e in loc.world.getNearbyEntities(loc, radius, radius, radius)) {
            if (e is LivingEntity) {
                e.damage(dmg)
            }
        }

        /**
         * 普通攻击范围增加2格持续15秒
         */


        Bukkit.getScheduler().runTaskLater(Main.Plugin, {
            casting.remove(p.uniqueId)
        }, 300)
    }
}