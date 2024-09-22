package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Addenergy : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val energy = form["energy"]!!.toInt()
            val playerInfo = Statistics.getPlayerInfo(name) ?: return gson.toJson(mapOf("error" to "玩家不存在"))
            val forbidLeft = playerInfo.forbidUntil - System.currentTimeMillis()
            if (forbidLeft > 0) {
                gson.toJson(mapOf("result" to false))
            } else {
                val result = Statistics.addEnergy(name, energy, true)
                gson.toJson(mapOf("result" to result))
            }
        } catch (e: NumberFormatException) {
            gson.toJson(mapOf("error" to "参数错误"))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
