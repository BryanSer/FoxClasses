package com.github.bryanser.foxclasses.view

import com.github.bryanser.brapi.data.NamingSpace
import com.github.bryanser.brapi.data.ValueProxy
import com.github.bryanser.brapi.vview.VViewContext
import com.github.bryanser.brapi.vview.VViewHandler
import com.github.bryanser.foxclasses.Main
import com.github.bryanser.foxclasses.PlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ClassViewContext(p: Player) : VViewContext(p) {
    val pd = PlayerData.getData(p)
    val classType = pd.getClassType() ?: throw IllegalArgumentException()
    val data = pd.talentData

    companion object {
        val space = NamingSpace(Main.Plugin, "classview")

        fun <T> NamingSpace.proxy(key: String, default: T): T = ValueProxy(this, key, default)()

        val view = VViewHandler.createView(
                space.proxy("view_img", "background.png"),
                space.proxy("view_x", -1),
                space.proxy("view_y", -1),
                space.proxy("view_w", 300),
                space.proxy("view_h", 600),
                ::ClassViewContext
        ) {
            for (i in 0..7) {
                button("btn_$i", "") {
                    visible {
                        classType.talent.getOrNull(i) != null
                    }
                    onBuild { ctx ->
                        val talent = ctx.classType.talent.getOrNull(i) ?: return@onBuild
                        img = talent.vv_icon()
                        x = talent.vv_x()
                        y = talent.vv_y()
                        h = talent.vv_h()
                        w = talent.vv_w()
                        clickImg = img
                        hover {
                            for (d in talent.description()) {
                                +d
                            }
                            +"§6技能等级: ${ctx.data.getLevel(talent) ?: 0}/${talent.maxLevel()}"
                            +"§e余剩技能点: ${ctx.pd.getReamingPoint()}"
                            +"§b左键点击加点"
                        }
                    }
                    onClick {
                        val talent = classType.talent.getOrNull(i) ?:return@onClick
                        if(pd.getReamingPoint() > 0){
                            data.levelUp(talent)
                            player.sendMessage("§6加点成功")
                            Bukkit.getScheduler().runTaskLater(Main.Plugin,{
                                player.closeInventory()
                                view.open(player)
                            },2)
                        }else{
                            player.sendMessage("§c你已经没有剩余的技能点了")
                        }
                    }
                }
                text(scale = 2.0) {
                    visible {
                        classType.talent.getOrNull(i) != null
                    }
                    onBuild { ctx ->
                        val talent = ctx.classType.talent.getOrNull(i) ?: return@onBuild
                        x = talent.vv_x() + talent.vv_w() - 2
                        y = talent.vv_y() + talent.vv_h() - 2
                        text {
                            +"${ctx.data.getLevel(talent) ?: 0}"
                        }
                    }
                }
            }
        }
    }
}