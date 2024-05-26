package com.fengsheng

import com.fengsheng.skill.RoleCache
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.logging.log4j.kotlin.logger
import java.time.Duration

object QQPusher {
    private val mu = Mutex()
    private val notifyQueueOnStart = HashSet<Long>()
    private val notifyQueueOnEnd = HashSet<Long>()

    fun addIntoNotifyQueue(qq: Long, onStart: Boolean) = runBlocking {
        mu.withLock {
            val map = if (onStart) notifyQueueOnStart else notifyQueueOnEnd
            map.size < 5 || return@withLock false
            map.add(qq)
            true
        }
    }

    fun notifyStart() {
        val at = runBlocking {
            mu.withLock {
                notifyQueueOnStart.toLongArray().apply { notifyQueueOnStart.clear() }
            }
        }
        if (at.isNotEmpty()) {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                try {
                    Config.PushQQGroups.forEach { sendGroupMessage(it, "开了", *at) }
                } catch (e: Throwable) {
                    logger.error("catch throwable", e)
                }
            }
        }
    }

    fun push(
        game: Game,
        declareWinners: List<Player>,
        winners: List<Player>,
        addScoreMap: HashMap<String, Int>,
        newScoreMap: HashMap<String, Int>
    ) {
        if (!Config.EnablePush) return
        val lines = ArrayList<String>()
        lines.add("对局结果")
        for (player in game.players.sortedBy { it!!.identity.number }) {
            val name = player!!.playerName
            var roleName = player.roleName
            if (player.role != player.originRole)
                RoleCache.getRoleName(player.originRole)?.let { roleName += "(原$it)" }
            var identity = Player.identityColorToString(player.identity, player.secretTask)
            if (player.identity != player.originIdentity || player.secretTask != player.originSecretTask)
                identity += "(原${Player.identityColorToString(player.originIdentity, player.originSecretTask)})"
            val result =
                if (declareWinners.any { it === player }) "宣胜"
                else if (winners.any { it === player }) "胜利"
                else if (player.lose) "输掉游戏"
                else "失败"
            val addScore = addScoreMap[name] ?: 0
            val newScore = newScoreMap[name] ?: 0
            val addScoreStr =
                if (addScore > 0) "+$addScore"
                else if (addScore < 0) addScore.toString()
                else if (result == "失败" || result == "输掉游戏") "-0"
                else "+0"
            val rank = ScoreFactory.getRankNameByScore(newScore)
            lines.add("$name,$roleName,$identity,$result,$rank,$newScore($addScoreStr)")
        }
        val text = lines.joinToString(separator = "\\n")
        val at = runBlocking {
            mu.withLock {
                notifyQueueOnEnd.toLongArray().apply { notifyQueueOnEnd.clear() }
            }
        }
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                Config.PushQQGroups.forEach { sendGroupMessage(it, text, *at) }
            } catch (e: Throwable) {
                logger.error("catch throwable", e)
            }
        }
    }

    private fun sendGroupMessage(groupId: Long, message: String, vararg at: Long) {
        val atStr = at.joinToString(separator = "") { "{\"type\":\"at\",\"data\":{\"qq\":$it}}," }
        val postData = """{
            "group_id":$groupId,
            "message":[$atStr{"type":"text","data":{"text":"$message"}}]
        }""".trimMargin().toRequestBody(contentType)
        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${Config.MiraiVerifyKey}")
            .url("${Config.MiraiHttpUrl}/send_group_msg").post(postData).build()
        val resp = client.newCall(request).execute()
        if (resp.code != 200) {
            resp.close()
            throw Exception("sendGroupMessage failed, status code: ${resp.code}")
        }
        val json = gson.fromJson(resp.body!!.string(), JsonElement::class.java)
        val code = json.asJsonObject["retcode"].asInt
        if (code != 0) throw Exception("sendGroupMessage failed, retcode: $code")
    }

    private val client = OkHttpClient().newBuilder().connectTimeout(Duration.ofMillis(20000)).build()
    private val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val gson = Gson()
}
