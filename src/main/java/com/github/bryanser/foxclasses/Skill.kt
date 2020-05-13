package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.impl.wizard.passive.OlympusAffinity
import com.github.bryanser.foxclasses.mana.ManaAttribute
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

abstract class Skill(
        name: String,
        displayName: String,
        description: List<String>
) : Talent(name, description) {

    init {
        skills[name] = this
    }

    val displayName = ConfigEntry.colorConfig("displayName", displayName)


    abstract fun init()
    abstract fun disable()

    private val cooldown = ConfigEntry<(Int) -> Int>("cooldown", provider = fun(cs, ce): (Int) -> Int {
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

    fun getCooldown(pd: PlayerData): Int {
        val lv = pd.talentData.getLevel(this) ?: return Int.MAX_VALUE
        var cd = cooldown()(lv)
        pd.talentData.getLevel(OlympusAffinity)?.run {
            val red = OlympusAffinity.cdReduce()(this)
            cd = (cd * (1 - red)).toInt()
        }
        return cd
    }

    val manaCost = ConfigEntry.expressionConfig("manaCost", "%level% * 10 + 50")

    val lastCast = hashMapOf<UUID, Long>()


    open fun loadConfig() {
        val f = File(skillFolder, "$name.yml")
        if (!f.exists()) {
            f.createNewFile()
        }
        val cs = YamlConfiguration.loadConfiguration(f)
        var modify = false
        for (ce in ConfigEntry.getEntry(this)) {
            modify = modify or ce.load(cs)
        }
        if (modify) {
            cs.save(f)
        }
    }

    fun tryCast(p: Player) {
        val last = lastCast[p.uniqueId] ?: 0L
        val pass = System.currentTimeMillis() - last
        val pd = PlayerData.getData(p)
        val lv = pd.talentData.getLevel(this) ?: return
        val cd = getCooldown(pd)
        if (pass < cd) {
            p.sendMessage("§c技能${displayName()}还在冷却中. 还需要${String.format("%.2f", (cd - pass) / 1000.0)}秒")
            return
        }
        if (!ManaAttribute.costMana(p, manaCost()(p, lv).toDouble(), true)) {
            p.sendMessage("§c技能${displayName()}无法释放: 蓝量不足")
            return
        }
        this.cast(p)
        lastCast[p.uniqueId] = System.currentTimeMillis()
    }

    abstract fun cast(p: Player)


    companion object {
        val skills = hashMapOf<String, Skill>()

        val skillFolder: File by lazy {
            File(Main.Plugin.dataFolder, "skills").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }
}

