package com.fengsheng.gm

import com.fengsheng.Game
import com.fengsheng.GameExecutor
import java.util.function.Function

class Forceend : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            Game.gameCache.values.toList().forEach {
                GameExecutor.post(it) {
                    if (it.isStarted && !it.isEnd)
                        it.end(null, null, true)
                }
            }
            gson.toJson(mapOf("result" to true))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
