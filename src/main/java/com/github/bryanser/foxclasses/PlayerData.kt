package com.github.bryanser.foxclasses

import Br.API.Data.BrConfigurationSerializableV2
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player

class PlayerData() : BrConfigurationSerializableV2 {

    @BrConfigurationSerializableV2.Config
    lateinit var name: String

    @BrConfigurationSerializableV2.Config
    var classType: String? = null

    @BrConfigurationSerializableV2.Config
    var exp: Int = 0
    @BrConfigurationSerializableV2.Config
    val level: Int = 1
    @BrConfigurationSerializableV2.Config
    lateinit var skillData: SkillData

    constructor(p: Player) : this() {
        name = p.name
        skillData = SkillData()
    }


    constructor(map: Map<String, Any?>) : this() {
        BrConfigurationSerializableV2.deserialize(map, this)
    }
}

class SkillData : ConfigurationSerializable {
    val data = hashMapOf<String, Int>()

    constructor()

    constructor(map: Map<String, Any?>) {
        for ((k, v) in map) {
            data[k] = v as Int
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        for ((k, v) in data) {
            map[k] = v
        }
        return map
    }
}