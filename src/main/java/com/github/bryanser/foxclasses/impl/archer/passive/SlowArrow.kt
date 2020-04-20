package com.github.bryanser.foxclasses.impl.archer.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.warrior.Warrior
import com.github.bryanser.foxclasses.impl.warrior.passive.Strong
import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

object SlowArrow  : Passive(
        "SlowArrow",
        "减速箭",
        listOf(
                "§6所有攻击和技能附加减速效果",
                "§6每级增加暴击几率与拉弓速率"
        )
), Listener {

    val criticalHitRate = ConfigEntry.mapConfig("health", mapOf(1 to 50.0, 2 to 75.0, 3 to 100.0))
    val damage = ConfigEntry.mapConfig("damage", mapOf(1 to 10.0, 2 to 20.0, 3 to 25.0))

    @EventHandler
    fun onAttribute(evt: SXLoadItemDataEvent) {
        val p = evt.entity as? Player ?: return
        val pd = PlayerData.getData(p)
        if (pd.getClassType() != Warrior) {
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val data = Strong.addAttribute(p)
        val cdata = data.getSubAttribute("CriticalHitRate")
        cdata.attributes[0] += criticalHitRate()(lv)
        val ddata = data.getSubAttribute("Damage")
        val dmg = damage()(lv)
        ddata.attributes[0] += dmg
        ddata.attributes[1] += dmg
    }

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }

}