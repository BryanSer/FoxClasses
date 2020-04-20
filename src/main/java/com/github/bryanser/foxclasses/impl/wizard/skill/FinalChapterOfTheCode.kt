package com.github.bryanser.foxclasses.impl.wizard.skill

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.archer.skill.ToxicBlast
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*

object FinalChapterOfTheCode : Skill("FinalChapterOfTheCode",
        "法典终章",
        listOf(
                "§6向面前扇形90°距离8格",
                "§6倾泻3波魔法造成三次伤害"
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


    override fun cast(p: Player) {
        val lv = PlayerData.getData(p).talentData.getLevel(this) ?: return
        casting[p.uniqueId] = lv
        explotion.add(p.uniqueId)

    }


}