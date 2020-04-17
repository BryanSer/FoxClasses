package com.github.bryanser.foxclasses.impl.warrior.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.brapi.Utils
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object XSword : Skill("XSword", "X剑气", listOf("§6快速向前方划出两道交叉的X形状剑气")) {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        val loc = p.location
        val vec = loc.direction.also {
            it.y = 0.0
            it.normalize()
        }
        val left = loc.add(Utils.getLeft(vec).multiply(1.5))
        val right = loc.add(Utils.getRight(vec).multiply(1.5))
        val front = loc.add(vec.clone().multiply(3.0))
        val damaged = hashSetOf<Int>()
        val dmg = damage()(p, lv).toDouble()
        draw(left, front, Color.BLUE) {
            if (it != p && it.entityId !in damaged) {
                damaged.add(it.entityId)
                it.damage(dmg, p)
            }
        }
        draw(right, front, Color.RED) {
            if (it != p && it.entityId !in damaged) {
                damaged.add(it.entityId)
                it.damage(dmg, p)
            }
        }
    }

    private inline fun draw(start: Location, target: Location, color: Color, crossinline func: (LivingEntity) -> Unit) {
        val vec = target.toVector().subtract(start.toVector()).normalize().multiply(0.05)
        object : BukkitRunnable() {
            var curr: Location = start.clone()
            override fun run() {
                if (curr.distanceSquared(start) >= 36.0) {
                    this.cancel()
                }
                ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(color), curr, 50.0)
                for (e in curr.world.getNearbyEntities(curr, 0.3, 0.3, 0.3)) {
                    if (e is LivingEntity) {
                        func(e)
                    }
                }
                curr = curr.add(vec)
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }
}