package com.github.bryanser.foxclasses.impl.warrior.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.util.ConfigEntry
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitTask

object Perseverance : Passive("Perseverance", "坚毅",
        listOf(
                "每级增加击退抗性",
                "并且受到伤害时回敬给对方一定伤害",
                "效果随等级提升"
        )
), Runnable, Listener {
    val knock = ConfigEntry.expressionConfig("knock", "%level% * 0.01")
    val damageRate = ConfigEntry.expressionConfig("damageRate", "%level% * 0.01")
    const val ATTRIBUTE_KEY = "fc_w_perseverance"

    lateinit var task: BukkitTask
    override fun init() {
        task = Bukkit.getScheduler().runTaskTimer(Main.Plugin, this, 100, 100)
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(evt: EntityDamageByEntityEvent) {
        val p = evt.entity as? Player ?: return
        val pd = PlayerData.getData(p)
        val lv = pd.talentData.getLevel(this) ?: return
        val dr = damageRate()(p,lv).toDouble()
        val dmg = dr * evt.damage
        evt.damager.let { it as? LivingEntity }?.also {
            it.damage(dmg)
        }
    }

    override fun disable() {
        task.cancel()
        HandlerList.unregisterAll(this)
    }

    override fun run() {
        for (p in Bukkit.getOnlinePlayers()) {
            val pd = PlayerData.getData(p)
            val lv = pd.talentData.getLevel(this) ?: continue
            val v = knock()(p, lv).toDouble()
            val attribute = p.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)
            for (ins in attribute.modifiers) {
                if (ins.name == ATTRIBUTE_KEY) {
                    attribute.removeModifier(ins)
                }
            }
            attribute.addModifier(AttributeModifier(ATTRIBUTE_KEY, v, AttributeModifier.Operation.ADD_NUMBER))
        }
    }
}