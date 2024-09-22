package com.fengsheng.gm

import com.fengsheng.QQPusher
import java.util.function.Function

class Addnotify : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val qq = form["qq"]!!.toLong()
            val onStart = (form["when"]?.toInt() ?: 0) == 0
            val result = QQPusher.addIntoNotifyQueue(qq, onStart)
            gson.toJson(mapOf("result" to result))
        } catch (e: NumberFormatException) {
            gson.toJson(mapOf("error" to "参数错误"))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
