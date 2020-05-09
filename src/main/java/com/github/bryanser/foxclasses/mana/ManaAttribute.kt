package com.github.bryanser.foxclasses.mana

import com.github.bryanser.foxclasses.Main
import github.saukiya.sxattribute.data.attribute.SXAttributeType
import github.saukiya.sxattribute.data.attribute.SubAttribute
import github.saukiya.sxattribute.data.eventdata.EventData
import github.saukiya.sxattribute.data.eventdata.sub.UpdateEventData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*

class ManaAttribute : SubAttribute("Stamina", 5, SXAttributeType.UPDATE) {
//max maxu rec recu cost

    data class ManaData(
            var maxMana: Double,
            var manaRecover: Double,
            var cost: Double,
            var mana: Double = maxMana
    )

    override fun eventMethod(p0: EventData) {
        val evt = p0 as? UpdateEventData ?: return
        val p = evt.entity as? Player ?: return
        val data = cache.getOrPut(p.uniqueId){
            ManaData(
                    attributes[0] * (1 + attributes[1]),
                    attributes[2] * (1 + attributes[3]),
                    attributes[4]
            )
        }
        data.maxMana = attributes[0] * (1 + attributes[1])
        data.manaRecover = attributes[2] * (1 + attributes[3])
        data.cost = attributes[4]
    }

    override fun getPlaceholders(): MutableList<String> = place

    override fun getValue(): Double {
        return attributes[0]
    }

    override fun loadAttribute(t: String): Boolean {
        val array = attributes
        val s = ChatColor.stripColor(t)
        var v = s.replace("[^0-9.%+-]".toRegex(), "")
        var i = 0
        if (v.contains("%")) {
            i = 1
            v = v.replace("%", "")
        }
        if (s.contains(maxManaName)) {
            array[i] += v.toDouble().let {
                if (i == 1) {
                    it / 100
                } else {
                    it
                }
            }
            return true
        }
        if(s.contains(manaRecoverName)){
            array[2 + i] += v.toDouble().let {
                if (i == 1) {
                    it / 100
                } else {
                    it
                }
            }
            return true
        }
        if(s.contains(manaCost)){
            array[4] += v.toDouble()
        }
        return false
    }

    override fun getPlaceholder(p0: Player, p1: String): String? {
        return when (p1.toLowerCase()) {
            "maxmana" -> {
                String.format("§6%.2f ↑ %.1f%% = %.2f", attributes[0], attributes[1] * 100, attributes[0] * (1 + attributes[1]))
            }
            "manarecover" -> {
                String.format("§6%.2f ↑ %.1f%% = %.2f", attributes[2], attributes[3] * 100, attributes[2] * (1 + attributes[3]))
            }
            "mana" -> {
                val data = cache[p0.uniqueId]
                String.format("%.2f", data?.mana ?: 0.0)
            }
            "manacost" -> {
                String.format("%.2f%%", attributes[4])
            }
            else -> null
        }
    }

    companion object : Listener, BukkitRunnable() {
        lateinit var maxManaName: String
        lateinit var manaRecoverName: String
        lateinit var manaCost: String
        fun loadConfig() {
            val f = File(Main.Plugin.dataFolder, "mana.yml")
            if (!f.exists()) {
                Main.Plugin.saveResource("mana.yml", false)
            }
            val config = YamlConfiguration.loadConfiguration(f)
            maxManaName = config.getString("Lore.Max")
            manaRecoverName = config.getString("Lore.Recover")
            manaCost = config.getString("Lore.Cost")
        }

        val place by lazy {
            mutableListOf(
                    "MaxMana",
                    "ManaRecover",
                    "Mana",
                    "ManaCost"
            )
        }

        fun init() {
            this.loadConfig()
            this.runTaskTimer(Main.Plugin, 10, 10)
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }


        val cache = hashMapOf<UUID, ManaData>()

        fun recover(p: Player) {
            val data = cache[p.uniqueId] ?: return
            data.mana = data.maxMana
        }

        fun recover(p: Player, value: Double) {
            val data = cache[p.uniqueId] ?: return
            data.mana += value
            if (data.mana > data.maxMana) {
                data.mana = data.maxMana
            }
        }

        fun recoverP(p: Player, value: Double) {
            val data = cache[p.uniqueId] ?: return
            data.mana += value * data.maxMana
            if (data.mana > data.maxMana) {
                data.mana = data.maxMana
            }

        }

        fun costMana(p: Player, value: Double, subCost: Boolean = false): Boolean {
            val data = cache[p.uniqueId] ?: return false
            var value = value
            if (subCost) {
                value += data.cost
                if (value < 0) {
                    value = 0.0
                }
            }
            if (data.mana >= value) {
                data.mana -= value
                return true
            }
            return false
        }


        override fun run() {
            for (p in Bukkit.getOnlinePlayers()) {
                val data = cache[p.uniqueId] ?: continue
                var v = data.mana + data.manaRecover / 2
                if (v > data.maxMana) {
                    v = data.maxMana
                }
                data.mana = v
            }
        }
    }
}