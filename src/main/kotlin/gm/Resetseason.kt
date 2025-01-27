package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Resetseason : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        Statistics.resetSeason()
        return gson.toJson(mapOf("result" to "重置成功"))
    }
}
