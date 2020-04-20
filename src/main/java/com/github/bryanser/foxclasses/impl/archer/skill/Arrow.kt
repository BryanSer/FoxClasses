package com.github.bryanser.foxclasses.impl.archer.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.util.ArmorStandManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.PI

object Arrow {
    fun cast(p: Player, loc: Location, distance: Double, vector: Vector,penetrate: Boolean, effect: (LivingEntity) -> Unit) {
        val isType = ItemStack(Material.ARROW)

        val arrowAS = ArmorStandManager.createArmorStand(
                loc) {
            it.setGravity(false)
            it.isVisible = false
            it.isMarker = true
            it.itemInHand = isType
            it.rightArmPose = EulerAngle(0.0, -PI / 4, 0.0)
        }
        archery(p, loc, vector, arrowAS, distance, penetrate) {
            effect(it)
        }
    }

    private fun archery(p: Player,
                        loc: Location,
                        vector: Vector,
                        armorStand: ArmorStand,
                        distance: Double,
                        penetrate: Boolean,
                        effect: (LivingEntity) -> Unit) {
        object : BukkitRunnable() {
            val dis2 = distance * distance

            init {
                run()
            }

            override fun run() {
                val dis = loc.distanceSquared2(armorStand.location)
                if (dis >= dis2) {
                    this.cancel()
                    armorStand.remove()
                    return
                }
                armorStand.velocity = vector
                for (e in armorStand.getNearbyEntities(0.1, 0.2, 0.1)) {
                    if (e == p) {
                        continue
                    } else if (e is LivingEntity) {
                        effect(e)
                        if (!penetrate) {
                            armorStand.remove()
                        }
                        break
                    }
                }
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }
}

fun Location.distanceSquared2(loc: Location): Double {
    if (this.world != loc.world) {
        return Double.MAX_VALUE
    }
    return this.distanceSquared(loc)
}
