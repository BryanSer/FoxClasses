package com.github.bryanser.foxclasses.impl.warrior.skill

import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.Motion
import org.bukkit.entity.Player
import java.util.*

object Spike : Skill("Spike",
        "突刺",
        listOf(
                "§6施法者突进4格",
                "§6被撞到的怪物受到伤害并击退"
        )
) {

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

        val damaged = mutableSetOf<Int>()
        val knock = 3
        Motion.charge(p, 5.0) { e ->
            if (!damaged.contains(e.entityId)) {
                e.damage(dmg, p)
                val tvec = e.location.subtract(p.location).toVector()
                tvec.y = 1.0
                tvec.normalize().multiply(knock)
                e.velocity = tvec
                damaged += e.entityId
            }
        }

    }


}