package com.github.bryanser.foxclasses.impl.wizard

import com.github.bryanser.foxclasses.ClassType
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.Skill
import com.github.bryanser.foxclasses.impl.wizard.passive.FlameShock
import com.github.bryanser.foxclasses.impl.wizard.passive.OlympusAffinity
import com.github.bryanser.foxclasses.impl.wizard.skill.*
import org.bukkit.Material

object Wizard : ClassType("Wizard", "巫师", Material.DIAMOND_HOE) {
    override val skills: List<Skill> by lazy {
        listOf(BlackMist, BurstAttachment, FinalChapterOfTheCode, FlameNova, Hexagram)
    }

    override val passives: List<Passive> by lazy {
        listOf(FlameShock, OlympusAffinity)
    }
}