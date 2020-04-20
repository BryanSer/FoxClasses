package com.github.bryanser.foxclasses.impl.archer

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.archer.passive.Ming
import com.github.bryanser.foxclasses.impl.archer.skill.ExplosiveArrow
import org.bukkit.Material

object Archer :ClassType("Archer", "弓箭手", Material.DIAMOND_AXE) {
    override val skills: List<Skill> by lazy {
        listOf(ExplosiveArrow)
    }

    override val passives: List<Passive> by lazy {
        listOf(Ming)
    }
}