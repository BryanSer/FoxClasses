package com.github.bryanser.foxclasses.impl.wizard.skill

import com.github.bryanser.brapi.Utils
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.ImmobilizeManager
import com.github.bryanser.foxclasses.util.tools.ParticleEffect
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

object BlackMist : Skill("BlackMist",
        "黑雾",
        listOf(
                "§6向面前释放一排黑雾向前进(宽3格，距离7)造成伤害",
                "§6并禁锢1秒",
                "§6该技能的一定百分比伤害会治疗施法者(黑雾为黑色)"
        )
) {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 7, 2 to 7, 3 to 7))

    val casting = hashMapOf<UUID, Int>()

    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        val speed = 0.4

        val dmg = damage()(p, lv).toDouble()

        val vec: Vector = p.location.direction.normalize()

        val centerLoc = p.eyeLocation.add(0.0, -0.5, 0.0)

        val leftLoc = p.eyeLocation.add(Utils.getLeft(vec).multiply(1.0)).add(0.0, -0.5, 0.0)

        val rightLoc = p.eyeLocation.add(Utils.getRight(vec).multiply(1.0)).add(0.0, -0.5, 0.0)

        val locList = mutableListOf<Location>()

        locList.add(centerLoc)
        locList.add(leftLoc)
        locList.add(rightLoc)

        object : BukkitRunnable() {
            var ddistance = distance()(lv).toDouble()
            val damaged = hashSetOf<Int>()
            override fun run() {
                if (ddistance <= 0) {

                    this.cancel()
                    return
                }

                val t = vec.clone().multiply(distance()(lv) - ddistance)

                locList.forEach { loc ->
                    val curr = loc.clone().add(t)
                    ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(Color.BLACK), curr.clone().add(vec.clone().multiply(5)), 50.0)

                    for (e in curr.world.getNearbyEntities(curr, 0.1, 0.1, 0.1)) {

                        if (e is LivingEntity && e != p && e.entityId !in damaged) {
                            damaged += e.entityId
                            e.damage(dmg, p)
                            //禁锢
                            ImmobilizeManager.newData().also {
                                it.timeLength = 1.0
                                ImmobilizeManager.addEffect(p, e, it)
                            }
                        }
                    }
                }
                ddistance -= speed
            }
        }.runTaskTimerAsynchronously(Main.Plugin, 1, 1)

    }


}