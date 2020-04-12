package com.github.bryanser.foxclasses

import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

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
        val need = pd.getClassType()?.exp()?.invoke(pd.level) ?: return
        if (exp > need) {
            exp -= need
            pd.level++
            evt.player.playSound(evt.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        }
        pd.exp = exp
    }

}