package com.github.bryanser.foxclasses.impl.warrior

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.warrior.passive.Strong
import org.bukkit.Material

object Warrior : ClassType("Warrior", "战士", Material.DIAMOND_SWORD) {
    override fun getSkills(): List<Skill> {
        TODO("not implemented")
    }

    override fun getPassives(): List<Passive> {
        return listOf(Strong)
    }
}