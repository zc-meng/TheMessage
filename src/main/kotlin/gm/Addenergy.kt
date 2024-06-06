package com.fengsheng.gm

import com.fengsheng.Statistics
import java.util.function.Function

class Addenergy : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val energy = form["energy"]!!.toInt()
            val result = Statistics.addEnergy(name, energy)
            "{\"result\": $result}"
        } catch (e: NumberFormatException) {
            "{\"error\": \"参数错误\"}"
        } catch (e: NullPointerException) {
            "{\"error\": \"参数错误\"}"
        }
    }
}
