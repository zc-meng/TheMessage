package com.fengsheng.gm

import com.fengsheng.ScoreFactory
import com.fengsheng.Statistics
import java.net.URLDecoder
import java.util.function.Function

class getscore : Function<Map<String, String?>, String> {
    override fun apply(form: Map<String, String?>): String {
        return try {
            val name = URLDecoder.decode(form["name"]!!, Charsets.UTF_8)
            val playerInfo = Statistics.getPlayerInfo(name)
            if (playerInfo == null) {
                "{\"result\": \"${name}已身死道消\"}"
            } else {
                val score = playerInfo.score
                val rank = ScoreFactory.getRankNameByScore(score)
                val winRate =
                    if (playerInfo.gameCount == 0) "0.00%"
                    else "%.2f%%".format(playerInfo.winCount * 100.0 / playerInfo.gameCount)
                "{\"result\": \"$name·$rank·$score，总场次：${playerInfo.gameCount}，胜率：$winRate\"}"
            }
        } catch (e: NullPointerException) {
            "{\"error\": \"参数错误\"}"
        }
    }
}