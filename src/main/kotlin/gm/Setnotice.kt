package com.fengsheng.gm

import com.fengsheng.Config
import java.util.function.Function

class Setnotice : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val notice = form["notice"]!!
            Config.Notice.set(notice)
            Config.save()
            gson.toJson(mapOf("result" to true))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
