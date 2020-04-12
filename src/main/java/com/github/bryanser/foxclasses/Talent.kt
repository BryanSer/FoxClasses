package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry

abstract class Talent(val name: String, description: List<String>) {
    val maxLevel = ConfigEntry<Int>("maxLevel", 3)

    val vv_icon = ConfigEntry("vv_icon", "skill_icon.png")
    val vv_x = ConfigEntry("vv_x",10)
    val vv_y = ConfigEntry("vv_y",10)
    val vv_w = ConfigEntry("vv_w",10)
    val vv_h = ConfigEntry("vv_w",10)

    val description = ConfigEntry.colorConfig("description", description)
}