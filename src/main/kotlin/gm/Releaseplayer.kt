package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Releaseplayer : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            if (Statistics.releasePlayer(name))
                gson.toJson(mapOf("result" to "${name}已解封"))
            else gson.toJson(mapOf("result" to "找不到玩家"))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
