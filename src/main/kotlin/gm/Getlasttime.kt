package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Getlasttime : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val playerInfo = Statistics.getPlayerInfo(name)
            if (playerInfo == null || playerInfo.score <= 0) "{\"result\": 0}"
            else "{\"result\": \"${System.currentTimeMillis() - playerInfo.lastTime}\"}"
        } catch (e: NullPointerException) {
            "{\"error\": \"参数错误\"}"
        }
    }
}
