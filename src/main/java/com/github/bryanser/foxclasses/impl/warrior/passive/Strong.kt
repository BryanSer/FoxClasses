package com.github.bryanser.foxclasses.impl.warrior.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.warrior.Warrior
import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.data.condition.SXConditionType
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

object Strong : Passive(
        "Strong",
        "强壮",
        listOf("§6每级增加生命加成与普通攻击伤害")
), Listener {

    val health = ConfigEntry.mapConfig("health", mapOf(1 to 50.0, 2 to 75.0, 3 to 100.0))
    val damage = ConfigEntry.mapConfig("damage", mapOf(1 to 10.0, 2 to 20.0, 3 to 25.0))

    @EventHandler
    fun onAttribute(evt:SXLoadItemDataEvent){
        if(evt.type != SXConditionType.EQUIPMENT){
            return
        }
        val pd = PlayerData.getData(evt.entity as? Player ?: return)
        if(pd.getClassType() != Warrior){
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val hdata = evt.attributeData.getSubAttribute("Health")
        hdata.attributes[0] += health()(lv)
        val ddata = evt.attributeData.getSubAttribute("Damage")
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