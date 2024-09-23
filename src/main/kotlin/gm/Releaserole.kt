package com.fengsheng.gm

import com.fengsheng.skill.RoleCache
import java.util.function.Function

class Releaserole : Function<Map<String, String>, Any> {
    override fun apply(form: Map<String, String>): Any {
        return try {
            val name = form["name"]!!
            val result = RoleCache.releaseRole(name)
            gson.toJson(mapOf("result" to result))
        } catch (e: NullPointerException) {
            gson.toJson(mapOf("error" to "参数错误"))
        }
    }
}
