package com.github.bryanser.foxclasses

import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onLoad() {
        Plugin = this
        ConfigurationSerialization.registerClass(TalentData::class.java)
        ConfigurationSerialization.registerClass(PlayerData::class.java)
    }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(ExpManager, this)
    }

    override fun onDisable() {
    }

    companion object {
        lateinit var Plugin: Main
    }
}