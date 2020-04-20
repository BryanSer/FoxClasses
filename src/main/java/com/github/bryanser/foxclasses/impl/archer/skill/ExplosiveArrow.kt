package com.github.bryanser.foxclasses.impl.archer.skill

import Br.API.ParticleEffect.ParticleEffect
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.util.Vector
import java.util.*

object ExplosiveArrow : Skill("ExplosiveArrow",
        "爆炸箭",
        listOf(
                "§6使用后下一发普通攻击的弓箭附加一次爆炸伤害",
                "§6被炸到的怪物会受到禁锢效果"
        )
), Listener {

    val damage = ConfigEntry.expressionConfig("damage", "%level% * 20 + %sx_damage%")
    val range = ConfigEntry.mapConfig("range", mapOf(1 to 1.5, 2 to 2.0, 3 to 3.0))

    val casting = hashMapOf<UUID, Int>()
    val flying = hashMapOf<UUID, Pair<Int, UUID>>()

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
        casting.clear()
        flying.clear()
    }

    const val ATTRIBUTE_NAME = "FC_EA"

    @EventHandler
    fun onHit(evt: ProjectileHitEvent) {
        val (lv, uid) = flying.remove(evt.entity.uniqueId) ?: return
        val p = Bukkit.getPlayer(uid) ?: return
        val loc = evt.hitBlock?.location ?: (evt.hitEntity?.location ?: return)
        val dmg = damage()(p, lv).toDouble()
        val r = range()(lv)

        ParticleEffect.EXPLOSION_HUGE.display(Vector.getRandom(), 1f, loc, 50.0)

        val enemy = mutableListOf<UUID>()

        for (e in loc.world.getNearbyEntities(loc, r, r, r)) {
            if (e !is LivingEntity) {
                continue
            }
            if (e === p) {
                continue
            }
            enemy.add(e.uniqueId)
            e.damage(dmg, p)
            val speed = e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
            speed.addModifier(AttributeModifier(ATTRIBUTE_NAME, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
        }

        Bukkit.getScheduler().runTaskLater(Main.Plugin, fun() {
            for (uid in enemy) {
                val e = Bukkit.getEntity(uid) as? LivingEntity ?: continue
                val speed = e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                for (attr in speed.modifiers) {
                    if (attr.name == ATTRIBUTE_NAME) {
                        speed.removeModifier(attr)
                    }
                }
            }
        }, 40)
    }

    @EventHandler
    fun onLaunch(evt: ProjectileLaunchEvent) {
        if (evt.entity !is Arrow) {
            return
        }
        val lv = casting.remove((evt.entity.shooter as? Player ?: return).uniqueId) ?: return
        flying[evt.entity.uniqueId] = lv to (evt.entity.shooter as Player).uniqueId
    }

    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
    }


}