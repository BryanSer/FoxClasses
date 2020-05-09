package com.github.bryanser.foxclasses

import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

object ExpManager : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExpChange(evt: PlayerExpChangeEvent) {
        if (evt.amount <= 0) {
            return
        }
        val pd = PlayerData.getData(evt.player)
        var exp = pd.exp + evt.amount
        val ct = pd.getClassType() ?: return
        if (pd.level >= ct.maxLevel) {
            pd.exp = exp
            return
        }
        val need = pd.getClassType()?.exp?.invoke()?.invoke(pd.level) ?: return
        if (exp > need) {
            exp -= need
            pd.level++
            evt.player.playSound(evt.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        }
        pd.exp = exp
    }

    val currSkill = hashMapOf<String, String>()

    @EventHandler
    fun onCast(evt: PlayerInteractEvent) {
        if (!evt.hasItem()) {
            return
        }
        val pd = PlayerData.getData(evt.player)
        val ct = pd.getClassType() ?: return
        val type = evt.player.inventory.itemInHand?.type ?: return
        if (type == ct.castItem()) {
            var curr = currSkill[evt.player.name] ?: run {
                evt.player.sendMessage("§6你还没有选择技能 请按下shift切换")
                return
            }
            val ski = Skill.skills[curr] ?: run {
                evt.player.sendMessage("§c错误 找不到技能$curr")
                return
            }
            ski.tryCast(evt.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChangeSkill(evt: PlayerToggleSneakEvent) {
        if(!evt.isSneaking){
            return
        }
        val pd = PlayerData.getData(evt.player)
        val ct = pd.getClassType() ?: return
        val type = evt.player.inventory.itemInHand?.type ?: return
        if (type == ct.castItem()) {
            var curr = currSkill[evt.player.name]
            if (curr == null) {
                curr = ct.skills.first().name
            } else {
                var find = false
                for (ski in ct.skills) {
                    if (find) {
                        curr = ski.name
                        find = false
                        break
                    }
                    if (ski.name == curr) {
                        find = true
                    }
                }
                if (find) {
                    curr = ct.skills.first().name
                }
            }
            val skill = Skill.skills[curr] ?: return
            evt.player.sendMessage("§6当前已切换到技能${skill.displayName()}")
            currSkill[evt.player.name] = curr!!
        }
    }

}