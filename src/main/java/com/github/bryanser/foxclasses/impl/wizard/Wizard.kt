package com.github.bryanser.foxclasses.impl.wizard

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.archer.passive.Ming
import com.github.bryanser.foxclasses.impl.archer.skill.ExplosiveArrow
import com.github.bryanser.foxclasses.impl.wizard.skill.BlackMist
import org.bukkit.Material

object Wizard : ClassType("Wizard", "巫师", Material.DIAMOND_AXE) {
    override val skills: List<Skill> by lazy {
        listOf(BlackMist)
    }

    override val passives: List<Passive> by lazy {
        listOf(Ming)
    }
}