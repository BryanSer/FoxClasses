package com.github.bryanser.foxclasses.impl.wizard.skill

import com.github.bryanser.brapi.Utils
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.tools.ParticleEffect
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object FinalChapterOfTheCode : Skill("FinalChapterOfTheCode",
        "法典终章",
        listOf(
                "§6向面前扇形90°距离8格",
                "§6倾泻3波魔法造成三次伤害"
        )
), Listener {
    //(横向紫色月牙形发射出去)
    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))

    val casting = hashMapOf<UUID, Int>()

    val explotion = hashSetOf<UUID>()

    override fun init() {
    }

    override fun disable() {
    }


    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        explotion.add(p.uniqueId)
        val speed = 0.4

        val dmg = damage()(p, lv).toDouble()
        val distance = distance()(lv).toDouble()

        emission(p, distance, speed, dmg,1)
        emission(p, distance, speed, dmg,10)
        emission(p, distance, speed, dmg,20)
    }


    fun emission(player: Player, distance: Double, speed: Double, dmg: Double, delay: Long) {
        val vec = player.location.direction.normalize()

        val centerLoc = player.eyeLocation.add(0.0, -0.5, 0.0).add(vec.clone().multiply(0.5))

        val locList = mutableListOf<Location>()
        locList.add(centerLoc)
        for (i in 0 until 10) {
            locList.add(player.eyeLocation.add(Utils.getLeft(vec).multiply(0.2 * i)).add(vec.clone().multiply(0.5 - 0.05 * i)).add(0.0, -0.5, 0.0))
            locList.add(player.eyeLocation.add(Utils.getRight(vec).multiply(0.2 * i)).add(vec.clone().multiply(0.5 - 0.05 * i)).add(0.0, -0.5, 0.0))
        }

        object : BukkitRunnable() {
            var p = distance
            val damaged = hashSetOf<Int>()
            override fun run() {
                if (p <= 0) {
                    this.cancel()
                    return
                }
                val t = vec.clone().multiply(distance - p)

                locList.forEach { loc ->
                    val curr = loc.clone().add(t)
                    ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.BLACK), curr.clone().add(vec.clone().multiply(5)), 50.0)
                    for (e in curr.world.getNearbyEntities(curr, 0.1, 0.1, 0.1)) {
                        if (e is LivingEntity && e != player && e.entityId !in damaged) {
                            damaged += e.entityId
                            e.damage(dmg, player)
                        }
                    }
                }
                p -= speed
            }
        }.runTaskTimerAsynchronously(Main.Plugin, delay, 1)
    }
}