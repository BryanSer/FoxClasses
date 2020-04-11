package com.github.bryanser.foxclasses

import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onLoad() {
        Plugin = this
        ConfigurationSerialization.registerClass(SkillData::class.java)
        ConfigurationSerialization.registerClass(PlayerData::class.java)
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    companion object {
        lateinit var Plugin: Main
    }
}