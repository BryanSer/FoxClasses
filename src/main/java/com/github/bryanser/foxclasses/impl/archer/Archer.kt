package com.github.bryanser.foxclasses.impl.archer

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.archer.passive.Ming
import com.github.bryanser.foxclasses.impl.archer.passive.SlowArrow
import com.github.bryanser.foxclasses.impl.archer.skill.CloudArrow
import com.github.bryanser.foxclasses.impl.archer.skill.ExplosiveArrow
import com.github.bryanser.foxclasses.impl.archer.skill.FastShooting
import com.github.bryanser.foxclasses.impl.archer.skill.ToxicBlast
import org.bukkit.Material

object Archer : ClassType("Archer", "弓箭手", Material.DIAMOND_AXE) {
    override val skills: List<Skill> by lazy {
        listOf(ExplosiveArrow, CloudArrow, FastShooting, ToxicBlast)
    }

    override val passives: List<Passive> by lazy {
        listOf<Passive>(Ming, SlowArrow)
    }
}