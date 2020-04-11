package com.github.bryanser.foxclasses

import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onLoad() {
        Plugin = this
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    companion object {
        lateinit var Plugin: Main
    }
}