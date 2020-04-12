package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class Passive(name: String, displayName: String, description: List<String>) : Talent(name, description) {
    val displayName = ConfigEntry.colorConfig("displayName", displayName)

    abstract fun init()
    abstract fun disable()

    open fun loadConfig() {
        val f = File(passiveFolder, "$name.yml")
        if (!f.exists()) {
            f.createNewFile()
        }
        val cs = YamlConfiguration.loadConfiguration(f)
        var modify = false
        for (field in this::class.java.fields) {
            if (ConfigEntry::class.java.isAssignableFrom(field.type)) {
                val ce = field.get(this) as ConfigEntry<*>
                modify = modify or ce.load(cs)
            }
        }
        if (modify) {
            cs.save(f)
        }
    }

    companion object {
        val passiveFolder: File by lazy {
            File(Main.Plugin.dataFolder, "passives").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }
}