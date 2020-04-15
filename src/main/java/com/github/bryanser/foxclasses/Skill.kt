package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

abstract class Skill(
        name: String,
        displayName: String,
        description: List<String>
) : Talent(name,description) {

    init{
        skills[name] = this
    }

    val displayName = ConfigEntry.colorConfig("displayName", displayName)


    abstract fun init()
    abstract fun disable()

    val cooldown = ConfigEntry<(Int) -> Int>("cooldown", provider = fun(cs, ce): (Int) -> Int {
        val e = cs.getConfigurationSection("cooldown")
        val cd = hashMapOf<Int, Int>()
        for (key in e.getKeys(false)) {
            cd[key.toInt()] = e.getInt(key)
        }
        return fun(t): Int {
            return cd[t] ?: -1
        }
    }) { cs, ce ->
        val e = cs.createSection("cooldown")
        e["1"] = 5000
        e["2"] = 4000
        e["3"] = 3000
    }

    val lastCast = hashMapOf<UUID, Long>()


    open fun loadConfig() {
        val f = File(skillFolder, "$name.yml")
        if (!f.exists()) {
            f.createNewFile()
        }
        val cs = YamlConfiguration.loadConfiguration(f)
        var modify = false
        for(ce in ConfigEntry.getEntry(this)){
            modify = modify or ce.load(cs)
        }
        if (modify) {
            cs.save(f)
        }
    }

    fun tryCast(p: Player) {
        val last = lastCast[p.uniqueId] ?: 0L
        val pass = System.currentTimeMillis() - last
        TODO()
    }

    abstract fun cast(p: Player)


    companion object {
        val skills = hashMapOf<String,Skill>()

        val skillFolder: File by lazy {
            File(Main.Plugin.dataFolder, "skills").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }
}

