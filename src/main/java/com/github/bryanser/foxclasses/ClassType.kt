package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class ClassType(
        val name: String,
        displayName: String,
        defaultCastItem: Material
) {
    val displayName = ConfigEntry.colorConfig("displayName", displayName)
    var maxLevel: Int = 1
    val exp = ConfigEntry<(Int) -> Int>("exp", provider = fun(cs, ce): (Int) -> Int {
        val e = cs.getConfigurationSection("exp")
        var max = 1
        val lv = hashMapOf<Int, Int>()
        for (key in e.getKeys(false)) {
            lv[key.toInt()] = e.getInt(key)
            if (key.toInt() > max) {
                max = key.toInt()
            }
        }
        maxLevel = max
        return fun(t): Int {
            return lv[t] ?: -1
        }
    }) { cs, ce ->
        val e = cs.createSection("exp")
        e["1"] = 100
        e["2"] = 200
        e["3"] = 300
    }

    val mana = ConfigEntry<(Int) -> Double>("mana", provider = fun(cs, ce): (Int) -> Double {
        val e = cs.getConfigurationSection("mana")
        var max = 1
        val lv = hashMapOf<Int, Double>()
        for (key in e.getKeys(false)) {
            lv[key.toInt()] = e.getDouble(key)
            if (key.toInt() > max) {
                max = key.toInt()
            }
        }
        return fun(t): Double {
            return lv[t] ?: lv[max]!!
        }
    }) { cs, ce ->
        val e = cs.createSection("mana")
        e["1"] = 50.0
        e["2"] = 60.0
        e["3"] = 70.0
    }

    val manaRecover = ConfigEntry<(Int) -> Double>("manaRecover", provider = fun(cs, ce): (Int) -> Double {
        val e = cs.getConfigurationSection("manaRecover")
        var max = 1
        val lv = hashMapOf<Int, Double>()
        for (key in e.getKeys(false)) {
            lv[key.toInt()] = e.getDouble(key)
            if (key.toInt() > max) {
                max = key.toInt()
            }
        }
        return fun(t): Double {
            return lv[t] ?: lv[max]!!
        }
    }) { cs, ce ->
        val e = cs.createSection("manaRecover")
        e["1"] = 1.0
        e["2"] = 1.5
        e["3"] = 2.0
    }

    val castItem = ConfigEntry<Material>("castItem", provider = { cs, ce ->
        Material.getMaterial(cs.getInt(ce.key))
    }) { cs, ce ->
        cs[ce.key] = defaultCastItem.id
    }

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

    abstract fun getSkills(): List<Skill>

    abstract fun getPassives(): List<Passive>

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