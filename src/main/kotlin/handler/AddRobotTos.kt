package com.fengsheng.handler

import com.fengsheng.HumanPlayer
import com.fengsheng.Player
import com.fengsheng.RobotPlayer
import com.fengsheng.Statistics
import com.fengsheng.protos.Fengsheng

class AddRobotTos : AbstractProtoHandler<Fengsheng.add_robot_tos>() {
    override fun handle0(r: HumanPlayer, pb: Fengsheng.add_robot_tos) {
        if (r.game!!.isStarted) {
            return
        }
        val emptyPosition = r.game!!.players.count { it == null }
        if (emptyPosition == 0) {
            return
        }
//        if (!Config.IsGmEnable) {
//            val score = Statistics.getScore(r.playerName) ?: 0
//            if (score <= 0) {
//                val now = System.currentTimeMillis()
//                val startTrialTime = Statistics.getTrialStartTime(r.playerName)
//                if (startTrialTime == 0L) {
//                    Statistics.setTrialStartTime(r.playerName, now)
//                } else if (now - 3 * 24 * 3600 * 1000 >= startTrialTime) {
//                    r.sendErrorMessage("您已被禁止添加机器人，多参与群内活动即可解锁")
//                    return
//                }
//            }
//            val humanCount = r.game!!.players.count { it is HumanPlayer }
//            if (humanCount <= 1 && emptyPosition == 1 && (Statistics.getScore(r.playerName) ?: 0) >= 60) {
//                r.sendErrorMessage("至少要2人才能开始游戏")
//                return
//            }
//        }
        val robotPlayer = RobotPlayer()
        robotPlayer.playerName = Player.randPlayerName(r.game!!)
        robotPlayer.game = r.game
        robotPlayer.game!!.onPlayerJoinRoom(robotPlayer, Statistics.totalPlayerGameCount.random())
    }
}
