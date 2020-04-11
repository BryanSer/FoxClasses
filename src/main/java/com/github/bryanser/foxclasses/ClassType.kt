package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class ClassType(
        val name: String,
        displayName: String
) {
    val displayName = ConfigEntry.colorConfig("displayName", displayName)

    open fun loadConfig() {
        val f = File(classFolder, "$name.yml")
        if (!f.exists()) {
            f.createNewFile()
        }
        val config = YamlConfiguration.loadConfiguration(f)
        var modify = false
        val cs = if (config.contains("Setting")) {
            config.getConfigurationSection("Setting")!!
        } else {
            modify = true
            config.createSection("Setting")
        }
        for (field in this::class.java.fields) {
            if (ConfigEntry::class.java.isAssignableFrom(field.type)) {
                val ce = field.get(this) as ConfigEntry<*>
                modify = modify or ce.load(cs)
            }
        }
        if (modify) {
            config.save(f)
        }
    }

    companion object {
        val classFolder: File by lazy {
            File(Main.Plugin.dataFolder, "classes").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }
}