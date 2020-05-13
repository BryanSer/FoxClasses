package com.github.bryanser.foxclasses.impl.wizard.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.warrior.Warrior
import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

object OlympusAffinity : Passive("OlympusAffinity", "奥数亲和", listOf(
        "每级增加冷却缩减",
        "每级增加真实伤害"
)) ,Listener{
    val cdReduce = ConfigEntry.mapConfig("cdReduce", mapOf(
            1 to 0.2, 2 to 0.3, 3 to 0.4
    ))

    val realDamage = ConfigEntry.mapConfig("realDamage", mapOf(
            1 to 100.0,
            2 to 200.0,
            3 to 300.0
    ))

    @EventHandler
    fun onAttribute(evt: SXLoadItemDataEvent) {
        val p = evt.entity as? Player ?: return
        val pd = PlayerData.getData(p)
        if (pd.getClassType() != Warrior) {
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val data = addAttribute(p)
        val hdata = data.getSubAttribute("Real")
        hdata.attributes[0] += realDamage()(lv)
    }

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }
}