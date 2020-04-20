package com.github.bryanser.foxclasses.impl.wizard.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ArmorStandManager
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object FlameNova : Skill("FlameNova",
        "火焰新星",
        listOf(
                "§6向面前发射两个蓝色的火球",
                "§6命中的第一个目标会受到伤害",
                "§6随后目标发生一次环形火焰新星对周围半径4格的目标造成伤害"
        )
) {
//(火焰新星为深红色)

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))

    val casting = hashMapOf<UUID, Int>()

    override fun init() {
    }

    override fun disable() {
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv

        val dmg = damage()(p, lv).toDouble()
        val distance = distance()(lv).toDouble()

        val fire1 = getArmorStand(p.location, Material.FIREBALL, false)
        val fire2 = getArmorStand(p.location, Material.FIREBALL, false)
        fireball(p,dmg,distance,fire1)
        fireball(p,dmg,distance,fire2)

    }

    fun fireball(p: Player, dmg: Double, distance: Double, fire: ArmorStand) {
        object : BukkitRunnable() {
            val vec = p.location.direction.normalize()

            val loc = p.location

            val ll = distance * distance

            override fun run() {

                val d = loc.distanceSquared(fire.location)

                if (d >= ll) {
                    fire.remove()
                    this.cancel()
                    return
                }

                fire.velocity = vec
                for (e in fire.getNearbyEntities(0.25, 1.0, 0.25)) {
                    if (e == p) {
                        continue
                    } else if (e is LivingEntity) {
                        e.damage(dmg, p)
                        fire.remove()
                        val t = e.location
                        t.world.createExplosion(t.x, t.y, t.z, 0.0F, false, false)

                        this.cancel()
                        break
                    }
                }
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    fun getArmorStand(location: Location, material: Material, isVisible: Boolean): ArmorStand {
        return ArmorStandManager.createArmorStand(location) {
            it.isVisible = isVisible
            it.itemInHand = ItemStack(material)
        }
    }
}