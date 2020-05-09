package com.github.bryanser.foxclasses

import com.github.bryanser.foxclasses.util.ConfigEntry
import github.saukiya.sxattribute.SXAttribute
import github.saukiya.sxattribute.data.attribute.SXAttributeData
import org.bukkit.entity.Player
import java.lang.invoke.MethodHandles

abstract class Talent(val name: String, description: List<String>) {
    val maxLevel = ConfigEntry<Int>("maxLevel", 3)

    val vv_icon = ConfigEntry("vv_icon", "skill_icon.png")
    val vv_x = ConfigEntry("vv_x", 10)
    val vv_y = ConfigEntry("vv_y", 10)
    val vv_w = ConfigEntry("vv_w", 10)
    val vv_h = ConfigEntry("vv_h", 10)

    val description = ConfigEntry.colorConfig("description", description)


    fun addAttribute(p: Player): SXAttributeData {
        val data = SXAttributeData()
        setValid(data)
        SXAttribute.getApi().setEntityAPIData(this::class.java, p.uniqueId, data)
        return data
    }

    companion object {
        val setValid: (SXAttributeData) -> Unit by lazy {
            val method = SXAttributeData::class.java.getDeclaredMethod("valid")
            method.isAccessible = true
            val mh = MethodHandles.lookup().unreflect(method)
            val f = fun(data: SXAttributeData) {
                if (!data.isValid) {
                    mh.invokeWithArguments(data)
                }
            }
            f
        }
    }
}