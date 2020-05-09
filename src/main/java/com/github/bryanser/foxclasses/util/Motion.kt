package com.github.bryanser.foxclasses.util

import com.github.bryanser.foxclasses.Main
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.pow

object Motion {
    fun knock(p: Player, target: LivingEntity, dis: Double) {

        val vec = target.location.toVector().subtract(p.location.toVector())
        vec.y = 1.0
        vec.normalize().multiply(dis)
        target.velocity = vec
    }

    fun isDamage(
            p: Player,
            ins: ArmorStand,
            penetrate: Boolean,
            effect: (LivingEntity) -> Unit
    ) {
        for (e in ins.getNearbyEntities(0.25, 1.0, 0.25)) {
            if (e == p) {
                continue
            } else if (e is LivingEntity) {
                effect(e)
                if (!penetrate) {
                    ins.remove()
                }
                break
            }
        }
    }

    fun charge(player: Player/*, dmg: Double*/,
               lengthSq1: Double,
               effect: (LivingEntity) -> Unit
    ): MutableList<LivingEntity> {
        val lengthSq = lengthSq1.pow(2)

        //val stop = stop(player).toBoolean()

        val start = player.location.clone()
        val vec = player.location.direction.clone()
        vec.setY(0)
        vec.normalize()
        val enemyList = mutableListOf<LivingEntity>()
        object : BukkitRunnable() {
            var time = 0
            override fun run() {
                if (player.world != start.world) {
                    this.cancel()
                    player.velocity = Vector()
                    return
                }
                if (time++ >= 100) {
                    this.cancel()
                    player.velocity = Vector()
                    return
                }
                if (player.location.add(vec).add(0.0, 1.0, 0.0).block.type != Material.AIR) {
                    player.velocity = Vector()
                    this.cancel()
                    return
                }
                if (start.distanceSquared2(player.location) >= lengthSq) {
                    player.velocity = Vector()
                    this.cancel()
                    return
                }
                player.velocity = vec
                for (e in player.getNearbyEntities(0.25, 0.25, 0.25)) {

                    if (e is LivingEntity && e !== player ) {
                        effect(e)
                    }
                }
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
        return enemyList
    }
}