package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Getlasttime : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val playerInfo = Statistics.getPlayerInfo(name)
            if (playerInfo == null || playerInfo.energy <= 0) gson.toJson(mapOf("result" to 0))
            else gson.toJson(mapOf("result" to System.currentTimeMillis() - playerInfo.lastTime))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
