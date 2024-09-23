package com.fengsheng.gm

import com.fengsheng.Config
import java.util.function.Function

class Updatewaitsecond : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val second = form["second"]!!.toInt()
            if (second <= 0) {
                gson.toJson(mapOf("result" to false))
            } else {
                Config.WaitingSecond.set(second)
                Config.save()
                gson.toJson(mapOf("result" to true))
            }
        } catch (e: NumberFormatException) {
            gson.toJson(mapOf("error" to "参数错误"))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
