package com.github.bryanser.foxclasses

import Br.API.Data.BrConfigurationSerializableV2
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File

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

    fun getReamingPoint(): Int {
        return level * 3 - talentData.data.values.sum()
    }

    fun getClassType(): ClassType? = ClassType[classType]

    constructor(p: Player) : this() {
        name = p.name
        talentData = TalentData()
    }

    constructor(map: Map<String, Any?>) : this() {
        BrConfigurationSerializableV2.deserialize(map, this)
    }

    companion object : Listener {
        val cache = hashMapOf<String, PlayerData>()

        private fun save(pd: PlayerData) {
            val f = File(playerFolder, "${pd.name}.yml")
            val config = YamlConfiguration()
            config["Data"] = pd
            config.save(f)
        }

        fun saveAll(){
           for(pd in cache.values){
               save(pd)
           }
        }

        private fun load(name:String):PlayerData?{
            val f = File(playerFolder, "${name}.yml")
            if(!f.exists()){
                return null
            }
            val config = YamlConfiguration.loadConfiguration(f)
            return config["Data"] as?  PlayerData
        }


        val playerFolder: File by lazy {
            File(Main.Plugin.dataFolder, "Players").also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }

        @EventHandler
        fun onQuit(evt: PlayerQuitEvent) {
            val pd = cache.remove(evt.player.name) ?: return
            save(pd)
        }

        fun getData(p: Player): PlayerData {
            var pd = cache[p.name]
            if (pd != null) {
                return pd
            }
            pd = load(p.name) ?: PlayerData(p)
            cache[p.name] = pd
            return pd
        }
    }
}

class TalentData : ConfigurationSerializable {
    val data = hashMapOf<String, Int>()

    fun getLevel(t: Talent): Int? = data[t.name]

    fun levelUp(t: Talent) {
        data[t.name] = (data[t.name] ?: 0) + 1
    }

    constructor()

    constructor(map: Map<String, Any?>) {
        for ((k, v) in map) {
            if(v is String) continue
            data[k] = (v as? String)?.toInt() ?: v as Int
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