package com.github.bryanser.foxclasses.impl.wizard.passive

import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.Passive
import com.github.bryanser.foxclasses.PlayerData
import com.github.bryanser.foxclasses.impl.warrior.Warrior
import com.github.bryanser.foxclasses.util.ConfigEntry
import com.github.bryanser.foxclasses.util.tools.ParticleEffect
import github.saukiya.sxattribute.event.SXLoadItemDataEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

object FlameShock : Passive("FlameShock", "火焰冲击", listOf(
        "每级增加法力值",
        "无法使用武器进行普通攻击",
        "但是平A会发射一枚可以穿透两个目标的火焰弹造成额外(武器)伤害"
)), Listener {

    val mana = ConfigEntry.mapConfig("mana", mapOf(1 to 50.0, 2 to 75.0, 3 to 100.0))
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

    @EventHandler
    fun onAttribute(evt: SXLoadItemDataEvent) {
        val p = evt.entity as? Player ?: return
        val pd = PlayerData.getData(p)
        if (pd.getClassType() != Warrior) {
            return
        }
        val lv = pd.talentData.getLevel(this) ?: return
        val data = addAttribute(p)
        val hdata = data.getSubAttribute("Mana")
        hdata.attributes[0] += mana()(lv)
    }

    val lastCast = hashMapOf<UUID, Long>()

    var ignore = false
    fun attack(p: Player, lv: Int, vec: Vector = p.eyeLocation.direction) {
        vec.multiply(0.25)
        val last = lastCast[p.uniqueId] ?: 0L
        val pass = System.currentTimeMillis() - last
        val cd = cooldown()(lv)
        if (pass < cd) {
            return
        }
        val dmg = p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).value
        object : BukkitRunnable() {
            val from = p.location
            var curr = p.eyeLocation.add(0.0, -0.25, 0.0)
            var display = true
            var hit: Int? = null

            init {
                run()
            }
            override fun run() {
                if(p.world!=curr.world){
                    cancel()
                    return
                }
                if (from.distanceSquared(curr) >= 400) {
                    cancel()
                    return
                }
                curr.block?.run {
                    if (type != Material.AIR) {
                        cancel()
                        return
                    }
                }
                for (e in curr.world.getNearbyEntities(curr, 0.1, 0.1, 0.1)) {
                    if (e is LivingEntity && e != p && e.entityId != hit) {
                        ignore = true
                        e.damage(dmg, p)
                        ignore = false
                        if (hit != null) {
                            cancel()
                            return
                        }
                        hit = e.entityId
                    }
                }
                if (display) {
                    val loc = curr.clone()
                    Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                        ParticleEffect.FLAME.display(0f, 0f, 0f, 0.01f, 1, loc, 50.0)
                    }
                }
                display = !display
                curr.add(vec)
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
        lastCast[p.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onAttack(evt: EntityDamageByEntityEvent) {
        if (ignore) {
            return
        }
        val p = evt.damager as? Player ?: return
        val pd = PlayerData.getData(p)
        val lv = pd.talentData.getLevel(this) ?: return
        attack(p, lv)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onAttack(evt: PlayerInteractEvent) {
        if (evt.hand != EquipmentSlot.HAND) {
            return
        }
        val p = evt.player
        val pd = PlayerData.getData(p)
        val lv = pd.talentData.getLevel(this) ?: return
        attack(p, lv)
    }


    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }
}