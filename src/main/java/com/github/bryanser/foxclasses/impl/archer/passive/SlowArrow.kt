package com.github.bryanser.foxclasses.impl.archer.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.archer.Archer
import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object SlowArrow : Passive(
        "SlowArrow",
        "减速箭",
        listOf(
                "§6所有攻击和技能附加减速效果",
                "§6每级增加暴击几率"
        )
), Listener {

    val crit = ConfigEntry.expressionConfig("crit", "%level% * 10.0")

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
//        val ddata = data.getSubAttribute("Damage")
//        val dmg = damage()(p, lv).toDouble()
//        ddata.attributes[0] += dmg
//        ddata.attributes[1] += dmg
//        val avg = (ddata.attributes[1] + ddata.attributes[0]) / 2
//        val spd = speed()(p, lv).toDouble() * avg

    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    fun onDamage(evt: EntityDamageByEntityEvent) {
        val p = evt.damager as? Player ?: return
        val pd = PlayerData.getData(p)
        if (pd.getClassType() != Archer) {
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val t = evt.entity as? LivingEntity ?: return
        t.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20, 0, false, false))
    }

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }

}