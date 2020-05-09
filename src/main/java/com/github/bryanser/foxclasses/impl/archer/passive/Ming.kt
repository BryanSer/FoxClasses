package com.github.bryanser.foxclasses.impl.archer.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.archer.Archer
import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.SXAttribute
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask

object Ming : Passive(
        "Ming",
        "心眼",
        listOf(
                "§6使用弓时不消耗箭",
                "§6每级增加暴击倍率与攻击力",
                "§6根据攻击力增加移动速度"
        )
), Listener, Runnable {

    val crit = ConfigEntry.expressionConfig("crit", "%level% * 10.0")
    val damage = ConfigEntry.expressionConfig("damage", "%level% * 50.0 + 100")
    val speed = ConfigEntry.expressionConfig("speed", "0.0005")

    @EventHandler
    fun onAttribute(evt: SXLoadItemDataEvent) {
        val p = evt.entity as? Player ?: return
        val pd = PlayerData.getData(p)
        if (pd.getClassType() != Archer) {
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val data = addAttribute(p)
        val cdata = data.getSubAttribute("Crit")
        cdata.attributes[0] += crit()(p, lv).toDouble()
        val ddata = data.getSubAttribute("Damage")
        val dmg = damage()(p, lv).toDouble()
        ddata.attributes[0] += dmg
        ddata.attributes[1] += dmg
//        val avg = (ddata.attributes[1] + ddata.attributes[0]) / 2
//        val spd = speed()(p, lv).toDouble() * avg

    }

    lateinit var task: BukkitTask

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        task = Bukkit.getScheduler().runTaskTimer(Main.Plugin, this, 100, 100)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
        task.cancel()
    }

    const val ATTRIBUTE_KEY = "fc_a_ming"

    override fun run() {
        for (p in Bukkit.getOnlinePlayers()) {
            val pd = PlayerData.getData(p)
            val lv = pd.talentData.getLevel(this) ?: continue
            val data = SXAttribute.getApi().getEntityAllData(p)
            val ddata = data.getSubAttribute("Damage")
            val attribute = p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
            for (ins in attribute.modifiers) {
                if (ins.name == ATTRIBUTE_KEY) {
                    attribute.removeModifier(ins)
                }
            }
            val avg = (ddata.attributes[1] + ddata.attributes[0]) / 2
            val spd = speed()(p, lv).toDouble() * avg
            attribute.addModifier(AttributeModifier(ATTRIBUTE_KEY, spd, AttributeModifier.Operation.ADD_NUMBER))
        }
    }

}