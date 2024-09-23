package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Forbidplayer : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val hours = form["hour"]!!.toInt()
            if (hours <= 0) return gson.toJson(mapOf("error" to "参数错误"))
            if (Statistics.forbidPlayer(name, hours))
                gson.toJson(mapOf("result" to "已将${name}封禁${hours}小时"))
            else gson.toJson(mapOf("result" to "找不到玩家"))
        } catch (e: NumberFormatException) {
            gson.toJson(mapOf("error" to "参数错误"))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
