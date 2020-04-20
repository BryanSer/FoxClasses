package com.github.bryanser.foxclasses.impl.wizard.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.archer.skill.ExplosiveArrow
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector
import java.util.*

object BurstAttachment : Skill("BurstAttachment",
        "爆裂附加",
        listOf(
                "§63秒内造成的所有伤害",
                "§6都会附加一次小型爆炸伤害(半径1格)"
        )
), Listener {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val distance = ConfigEntry.mapConfig("distance", mapOf(1 to 25, 2 to 25, 3 to 25))

    val casting = hashMapOf<UUID, Int>()

    val explotion = hashSetOf<UUID>()

    override fun init() {
    }

    override fun disable() {
    }

    @EventHandler
    fun onHit(evt: EntityDamageByEntityEvent) {
        val p = evt.damager as Player
        if (p.uniqueId in explotion) {
            val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
            val dmg = damage()(p, lv).toDouble()
            if (evt.entity is Player) {

                val loc = evt.entity.location
                ParticleEffect.EXPLOSION_HUGE.display(Vector.getRandom(), 1f, loc, 50.0)

                val enemy = mutableListOf<UUID>()

                for (e in loc.world.getNearbyEntities(loc, 1.0, 1.0, 1.0)) {
                    if (e !is LivingEntity) {
                        continue
                    }
                    if (e === p) {
                        continue
                    }
                    enemy.add(e.uniqueId)
                    e.damage(dmg, p)
                }
            }
        }
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        explotion.add(p.uniqueId)


        Bukkit.getScheduler().runTaskLater(Main.Plugin, fun() {
            explotion.remove(p.uniqueId)
        }, 20 * 1)
    }


}