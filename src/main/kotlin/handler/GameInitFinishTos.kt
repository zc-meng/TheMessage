package com.fengsheng.handler

import com.fengsheng.GameExecutor
import com.fengsheng.HumanPlayer
import com.fengsheng.phase.WaitForSelectRole
import com.google.protobuf.GeneratedMessage
import org.apache.logging.log4j.kotlin.logger

class GameInitFinishTos : ProtoHandler {
    override fun handle(player: HumanPlayer, message: GeneratedMessage) {
        if (player.isLoadingRecord) {
            player.displayRecord()
        } else {
            val game = player.game
            if (game == null) {
                logger.error("can not find game")
                player.sendErrorMessage("找不到房间")
                return
            }
            GameExecutor.post(game) {
                if (player.isReconnecting)
                    player.reconnect()
                val fsm = game.fsm as? WaitForSelectRole ?: return@post
                fsm.notifySelectRole(player)
            }
        }
    }
}
