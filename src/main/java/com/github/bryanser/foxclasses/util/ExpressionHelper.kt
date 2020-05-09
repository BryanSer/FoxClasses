package com.github.bryanser.foxclasses.util

import com.github.bryanser.brapi.ScriptManager
import com.github.bryanser.foxclasses.Main
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import java.util.regex.Pattern

typealias Expression = (Player, Int) -> ExpressionResult
typealias VarReader = (Player, String) -> String?

object ExpressionHelper {
    val compile = Pattern.compile("(?<pattern>%(?<name>[^%]*)%)")
    val levelHolder = mutableMapOf<Int, Int>()
    val varReader = mutableListOf<VarReader>(
            { p, s ->
                val lv = levelHolder[p.entityId] ?: return@mutableListOf null
                if (s.equals("%level%", true)) {
                    lv.toString()
                } else {
                    null
                }
            },
            { p, s ->
                val r = PlaceholderAPI.setPlaceholders(p, s)
                return@mutableListOf if (r == s) null else r
            }
    )

    fun readVar(p: Player, s: String): Double {
        var r: String? = null
        for (vr in varReader) {
            if (r != null) {
                break
            }
            r = vr(p, s)
        }
        return r?.toDouble() ?: 0.0
    }

    fun compileExpression(src: String, isBoolean: Boolean = false): Expression {
        if (src.startsWith("[Script]")) {
            /*如果是完全独立脚本写法应该如下
             * [Script]
             * function calc(p, lv){
             *    你的代码;
             *    return 一个值;
             * }
             * 将会传入玩家对象作为变量
            */
            val src = src.replaceFirst("[Script]", "")
            val t = ScriptManager.createScriptEngine(Main.Plugin)
            t.eval(src)
            if (isBoolean) {
                return { it, lv ->
                    levelHolder[it.entityId] = lv
                    val d = t.invokeFunction("calc", it, lv) as? Boolean ?: true
                    ExpressionResult(d)
                }

            } else {
                return { it, lv ->
                    levelHolder[it.entityId] = lv
                    val d = t.invokeFunction("calc", it, lv) as? Double ?: 0.0
                    ExpressionResult(d)
                }
            }
        }
        val matcher = compile.matcher(src)
        var script = src
        val vars = mutableMapOf<String, String>()
        var index = 0
        while (matcher.find()) {
            val pattern = matcher.group("pattern")
            val name = matcher.group("name")
            if (!vars.containsKey(pattern)) {
                vars[pattern] = "var$index"
                index++
            }
        }
        var args = ""
        for ((p, v) in vars) {
            script = script.replace(p, v)
            if (!args.isEmpty()) {
                args += ","
            }
            args += v
        }
        val scrr = """
            function calc($args){
                return $script;
            }
            """.trimIndent()
        val t = ScriptManager.createScriptEngine(Main.Plugin)
        println("编译脚本: $scrr")
        t.eval(scrr)
        if (isBoolean) {
            return { it, lv ->
                levelHolder[it.entityId] = lv
                val vari = mutableListOf<Double>()
                for ((par, _) in vars) {
                    vari += readVar(it, par)
                }
                val b = t.invokeFunction("calc", *vari.toTypedArray()) as? Boolean ?: true
                ExpressionResult(b)
            }
        } else {
            return { it, lv ->
                levelHolder[it.entityId] = lv
                val vari = mutableListOf<Double>()
                for ((par, _) in vars) {
                    vari += readVar(it, par)
                }
                val d = t.invokeFunction("calc", *vari.toTypedArray()) as? Number ?: 0.0
                ExpressionResult(d.toDouble())
            }
        }
    }
}