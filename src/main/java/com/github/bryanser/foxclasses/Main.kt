package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.mana.ManaAttribute
import com.github.bryanser.foxclasses.util.ImmobilizeManager
import com.github.bryanser.foxclasses.util.SpeedManager
import com.github.bryanser.foxclasses.view.ClassViewContext
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onLoad() {
        Plugin = this
        ConfigurationSerialization.registerClass(TalentData::class.java)
        ConfigurationSerialization.registerClass(PlayerData::class.java)
        ManaAttribute().registerAttribute(this)
    }

    override fun onEnable() {
        ClassType.init()
        Bukkit.getPluginManager().registerEvents(ExpManager, this)
        Bukkit.getPluginManager().registerEvents(PlayerData.Companion, this)
        ManaAttribute.init()
        SpeedManager.init()
        ImmobilizeManager.init()
    }

    override fun onDisable() {
        PlayerData.saveAll()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (args.isEmpty()) {
                try {
                    ClassViewContext.view.open(sender)
                } catch (e: IllegalArgumentException) {
                    sender.sendMessage(e.message)
                }
                return true
            }
            if (args[0].equals("class", true)) {
                if (args.size < 2) {
                    sender.sendMessage("§c目前可用的职业如下:")
                    for (c in ClassType.classes.values) {
                        sender.sendMessage("§e    ${c.name}: ${c.displayName()}")
                    }
                    return true
                }
                val name = args[1]
                val ct = ClassType.classes[name] ?: run {
                    sender.sendMessage("§c未找到职业${name}")
                    sender.sendMessage("§c目前可用的职业如下:")
                    for (c in ClassType.classes.values) {
                        sender.sendMessage("§e    ${c.name}: ${c.displayName()}")
                    }
                    return true
                }
                val pd = PlayerData.getData(sender)
                pd.classType = ct.name
                pd.talentData.data.clear()
                sender.sendMessage("§6职业切换成功")
                return true
            }
        }
        if (sender.isOp) {
            if (args[0].equals("reload", true)) {
                ClassType.reload()
                ManaAttribute.loadConfig()
                sender.sendMessage("§6重载成功")
                return true
            }
            if (args[0].equals("mana", true) && args.size >= 2) {
                val t = Bukkit.getPlayerExact(args[1]) ?: run {
                    sender.sendMessage("§c找不到玩家${args[1]}")
                    return true
                }
                if (args.size > 2) {
                    val v = args[2]
                    if (v.contains("%")) {
                        ManaAttribute.recoverP(t, v.replace("%", "").toDouble() / 100)
                    } else {
                        ManaAttribute.recover(t, v.toDouble())
                    }
                } else {
                    ManaAttribute.recover(t)
                }
                sender.sendMessage("§6处理完成")
                return true
            }
        }

        return false
    }

    companion object {
        lateinit var Plugin: Main
    }
}
