package com.github.bryanser.foxclasses.impl.warrior

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.warrior.passive.Perseverance
import com.github.bryanser.foxclasses.impl.warrior.passive.Strong
import com.github.bryanser.foxclasses.impl.warrior.skill.*
import org.bukkit.Material

object Warrior : ClassType("Warrior", "战士", Material.DIAMOND_SWORD) {

    override val skills: List<Skill> by lazy {
        listOf(XSword,Guardian,Spike,TheProtectionOfGod,Windbreak)
    }

    override val passives: List<Passive> by lazy {
        listOf<Passive>(Strong, Perseverance)
    }
}