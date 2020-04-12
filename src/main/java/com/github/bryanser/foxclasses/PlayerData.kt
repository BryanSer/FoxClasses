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
    var level: Int = 1

    @BrConfigurationSerializableV2.Config
    lateinit var talentData: TalentData

    fun getReamingPoint():Int{
        return level * 3 - talentData.data.values.sum()
    }

    fun getClassType():ClassType? {
        TODO()
    }

    constructor(p: Player) : this() {
        name = p.name
        talentData = TalentData()
    }


    constructor(map: Map<String, Any?>) : this() {
        BrConfigurationSerializableV2.deserialize(map, this)
    }

    companion object {
        fun getData(p: Player): PlayerData {
            TODO()
        }
    }
}

class TalentData : ConfigurationSerializable {
    val data = hashMapOf<String, Int>()

    fun getLevel(t: Talent): Int? = data[t.name]

    fun levelUp(t:Talent){
        data[t.name] = (data[t.name] ?: 0) + 1
    }

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