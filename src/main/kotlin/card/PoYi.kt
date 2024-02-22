package com.fengsheng.card

import com.fengsheng.*
import com.fengsheng.phase.OnFinishResolveCard
import com.fengsheng.phase.ResolveCard
import com.fengsheng.phase.SendPhaseIdle
import com.fengsheng.protos.Common.card_type.Po_Yi
import com.fengsheng.protos.Common.color
import com.fengsheng.protos.Common.direction
import com.fengsheng.protos.Fengsheng.*
import com.fengsheng.skill.cannotPlayCard
import com.google.protobuf.GeneratedMessageV3
import org.apache.logging.log4j.kotlin.logger
import java.util.concurrent.TimeUnit

class PoYi : Card {
    constructor(id: Int, colors: List<color>, direction: direction, lockable: Boolean) :
            super(id, colors, direction, lockable)

    constructor(id: Int, card: Card) : super(id, card)

    /**
     * 仅用于“作为破译使用”
     */
    internal constructor(originCard: Card) : super(originCard)

    override val type = Po_Yi

    override fun canUse(g: Game, r: Player, vararg args: Any): Boolean {
        if (r.cannotPlayCard(type)) {
            logger.error("你被禁止使用破译")
            (r as? HumanPlayer)?.sendErrorMessage("你被禁止使用破译")
            return false
        }
        val fsm = g.fsm as? SendPhaseIdle
        if (r !== fsm?.inFrontOfWhom) {
            logger.error("破译的使用时机不对")
            (r as? HumanPlayer)?.sendErrorMessage("破译的使用时机不对")
            return false
        }
        return true
    }

    override fun execute(g: Game, r: Player, vararg args: Any) {
        val fsm = g.fsm as SendPhaseIdle
        logger.info("${r}使用了$this")
        r.deleteCard(id)
        val resolveFunc = { _: Boolean ->
            executePoYi(this@PoYi, fsm)
        }
        g.resolve(ResolveCard(fsm.whoseTurn, r, null, getOriginCard(), Po_Yi, resolveFunc, fsm))
    }

    private data class executePoYi(val card: PoYi, val sendPhase: SendPhaseIdle) : WaitingFsm {
        override fun resolve(): ResolveResult? {
            val r = sendPhase.inFrontOfWhom
            for (player in r.game!!.players) {
                if (player is HumanPlayer) {
                    val builder = use_po_yi_toc.newBuilder()
                    builder.card = card.toPbCard()
                    builder.playerId = player.getAlternativeLocation(r.location)
                    if (!sendPhase.isMessageCardFaceUp) builder.waitingSecond = Config.WaitSecond
                    if (player === r) {
                        val seq2 = player.seq
                        builder.messageCard = sendPhase.messageCard.toPbCard()
                        builder.seq = seq2
                        val waitingSecond =
                            if (builder.waitingSecond == 0) 0
                            else player.getWaitSeconds(builder.waitingSecond + 2)
                        player.timeout = GameExecutor.post(r.game!!, {
                            if (player.checkSeq(seq2))
                                r.game!!.tryContinueResolveProtocol(r, po_yi_show_tos.getDefaultInstance())
                        }, waitingSecond.toLong(), TimeUnit.SECONDS)
                    }
                    player.send(builder.build())
                }
            }
            if (r is RobotPlayer) {
                GameExecutor.post(r.game!!, {
                    val builder2 = po_yi_show_tos.newBuilder()
                    builder2.show = sendPhase.messageCard.isBlack()
                    r.game!!.tryContinueResolveProtocol(r, builder2.build())
                }, 1, TimeUnit.SECONDS)
            }
            return null
        }

        override fun resolveProtocol(player: Player, message: GeneratedMessageV3): ResolveResult? {
            if (message !is po_yi_show_tos) {
                logger.error("现在正在结算破译")
                (player as? HumanPlayer)?.sendErrorMessage("现在正在结算破译")
                return null
            }
            if (player !== sendPhase.inFrontOfWhom) {
                logger.error("你不是破译的使用者")
                (player as? HumanPlayer)?.sendErrorMessage("你不是破译的使用者")
                return null
            }
            if (message.show && !sendPhase.messageCard.isBlack()) {
                logger.error("非黑牌不能翻开")
                (player as? HumanPlayer)?.sendErrorMessage("非黑牌不能翻开")
                return null
            }
            player.incrSeq()
            showAndDrawCard(message.show)
            val newFsm = if (message.show) sendPhase.copy(isMessageCardFaceUp = true) else sendPhase
            return ResolveResult(
                OnFinishResolveCard(sendPhase.whoseTurn, player, null, card.getOriginCard(), Po_Yi, newFsm),
                true
            )
        }

        private fun showAndDrawCard(show: Boolean) {
            val r = sendPhase.inFrontOfWhom
            if (show) {
                logger.info("${sendPhase.messageCard}被翻开了")
                r.draw(1)
            }
            for (player in r.game!!.players) {
                if (player is HumanPlayer) {
                    val builder = po_yi_show_toc.newBuilder()
                    builder.playerId = player.getAlternativeLocation(r.location)
                    builder.show = show
                    if (show) builder.messageCard = sendPhase.messageCard.toPbCard()
                    player.send(builder.build())
                }
            }
        }
    }

    override fun toString(): String {
        return "${cardColorToString(colors)}破译"
    }

    companion object {
        fun ai(e: SendPhaseIdle, card: Card): Boolean {
            val player = e.inFrontOfWhom
            !player.cannotPlayCard(Po_Yi) || return false
            !e.isMessageCardFaceUp && e.messageCard.isBlack() || return false
            GameExecutor.post(player.game!!, { card.asCard(Po_Yi).execute(player.game!!, player) }, 1, TimeUnit.SECONDS)
            return true
        }
    }
}