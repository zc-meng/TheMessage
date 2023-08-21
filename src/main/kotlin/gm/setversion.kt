package com.fengsheng.gm

import com.fengsheng.Config
import java.util.function.Function

class setversion : Function<Map<String, String?>, String> {
    override fun apply(form: Map<String, String?>): String {
        return try {
            val name = form["version"]!!
            Config.ClientVersion.set(name.toInt())
            Config.save()
            "{\"result\": true}"
        } catch (e: NumberFormatException) {
            "{\"error\": \"invalid arguments\"}"
        } catch (e: NullPointerException) {
            "{\"error\": \"invalid arguments\"}"
        }
    }
}