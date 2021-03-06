package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.impl.archer.Archer
import com.github.bryanser.foxclasses.impl.warrior.Warrior
import com.github.bryanser.foxclasses.impl.wizard.Wizard
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


    private var inited = false

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
            val t = config.createSection("Setting")
            config["Setting"] = t
            t
        }
        for(ce in ConfigEntry.getEntry(this)){
            modify = modify or ce.load(cs)
        }
        if (modify) {
            config.save(f)
        }
        skills.forEach(Skill::loadConfig)
        passives.forEach(Passive::loadConfig)
        if (inited) {
            skills.forEach(Skill::disable)
            passives.forEach(Passive::disable)
        }
        inited = true
        skills.forEach(Skill::init)
        passives.forEach(Passive::init)
    }


    abstract val skills: List<Skill>

    //abstract fun getSkills(): List<Skill>
    abstract val passives: List<Passive>

    //abstract fun getPassives(): List<Passive>

    val talent: List<Talent> by lazy {
        val list = mutableListOf<Talent>()
        list.addAll(skills)
        list.addAll(passives)
        list
    }

    companion object {
        val classes = hashMapOf<String, ClassType>()

        operator fun get(name: String?): ClassType? {
            return classes[name ?: return null]
        }

        fun registerClass(ct: ClassType) {
            classes[ct.name] = ct
            ct.loadConfig()
        }

        fun reload() {
            for (ct in classes.values) {
                ct.loadConfig()
            }
        }

        fun init() {
            registerClass(Warrior)
            registerClass(Archer)
            registerClass(Wizard)
        }

        val classFolder: File by lazy {
            File(Main.Plugin.dataFolder, "classes").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }
}