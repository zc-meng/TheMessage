package com.fengsheng.skill

import com.fengsheng.*
import com.fengsheng.card.PlayerAndCard
import com.fengsheng.protos.Role.*
import com.google.protobuf.GeneratedMessageV3
import org.apache.logging.log4j.kotlin.logger
import java.util.concurrent.TimeUnit

/**
 * 李宁玉技能【遗信】：你死亡前，可以将一张手牌置入另一名角色的情报区。
 */
class YiXin : TriggeredSkill, BeforeDieSkill {
    override val skillId = SkillId.YI_XIN

    override val isInitialSkill = true

    override fun execute(g: Game, askWhom: Player): ResolveResult? {
        val event = g.findEvent<PlayerDieEvent>(this) { event ->
            askWhom === event.whoDie || return@findEvent false
            askWhom.roleFaceUp || return@findEvent false
            askWhom.cards.isNotEmpty() || return@findEvent false
            g.players.any { it!!.alive }
        } ?: return null
        return ResolveResult(executeYiXin(g.fsm!!, event, askWhom), true)
    }

    private data class executeYiXin(val fsm: Fsm, val event: PlayerDieEvent, val r: Player) : WaitingFsm {
        override fun resolve(): ResolveResult? {
            for (player in r.game!!.players) {
                if (player is HumanPlayer) {
                    val builder = skill_wait_for_yi_xin_toc.newBuilder()
                    builder.playerId = player.getAlternativeLocation(r.location)
                    builder.waitingSecond = Config.WaitSecond
                    if (player == r) {
                        val seq2 = player.seq
                        builder.seq = seq2
                        player.timeout = GameExecutor.post(
                            r.game!!,
                            {
                                val builder2 = skill_yi_xin_tos.newBuilder()
                                builder2.enable = false
                                builder2.seq = seq2
                                r.game!!.tryContinueResolveProtocol(r, builder2.build())
                            },
                            player.getWaitSeconds(builder.waitingSecond + 2).toLong(),
                            TimeUnit.SECONDS
                        )
                    }
                    player.send(builder.build())
                }
            }
            if (r is RobotPlayer) {
                var value = -1
                var playerAndCard: PlayerAndCard? = null
                for (c in r.cards) {
                    for (p in r.game!!.players.shuffled()) {
                        if (p!!.alive && r !== p) {
                            val v = r.calculateMessageCardValue(event.whoseTurn, p, c)
                            if (v > value) {
                                value = v
                                playerAndCard = PlayerAndCard(p, c)
                            }
                        }
                    }
                }
                GameExecutor.post(r.game!!, {
                    val builder = skill_yi_xin_tos.newBuilder()
                    if (playerAndCard != null) {
                        builder.enable = true
                        builder.cardId = playerAndCard.card.id
                        builder.targetPlayerId = r.getAlternativeLocation(playerAndCard.player.location)
                    }
                    r.game!!.tryContinueResolveProtocol(r, builder.build())
                }, 3, TimeUnit.SECONDS)
            }
            return null
        }

        override fun resolveProtocol(player: Player, message: GeneratedMessageV3): ResolveResult? {
            if (player !== r) {
                logger.error("不是你发技能的时机")
                (player as? HumanPlayer)?.sendErrorMessage("不是你发技能的时机")
                return null
            }
            if (message !is skill_yi_xin_tos) {
                logger.error("错误的协议")
                (player as? HumanPlayer)?.sendErrorMessage("错误的协议")
                return null
            }
            val g = r.game!!
            if (r is HumanPlayer && !r.checkSeq(message.seq)) {
                logger.error("操作太晚了, required Seq: ${r.seq}, actual Seq: ${message.seq}")
                r.sendErrorMessage("操作太晚了")
                return null
            }
            if (!message.enable) {
                r.incrSeq()
                for (p in g.players) {
                    (p as? HumanPlayer)?.send(skill_yi_xin_toc.newBuilder().setEnable(false).build())
                }
                return ResolveResult(fsm, true)
            }
            val card = r.findCard(message.cardId)
            if (card == null) {
                logger.error("没有这张卡")
                (player as? HumanPlayer)?.sendErrorMessage("没有这张卡")
                return null
            }
            if (message.targetPlayerId < 0 || message.targetPlayerId >= g.players.size) {
                logger.error("目标错误")
                (player as? HumanPlayer)?.sendErrorMessage("目标错误")
                return null
            }
            if (message.targetPlayerId == 0) {
                logger.error("不能以自己为目标")
                (player as? HumanPlayer)?.sendErrorMessage("不能以自己为目标")
                return null
            }
            val target = g.players[r.getAbstractLocation(message.targetPlayerId)]!!
            if (!target.alive) {
                logger.error("目标已死亡")
                (player as? HumanPlayer)?.sendErrorMessage("目标已死亡")
                return null
            }
            r.incrSeq()
            logger.info("${r}发动了[遗信]")
            r.deleteCard(card.id)
            target.messageCards.add(card)
            logger.info("${r}将${card}放置在${target}面前")
            for (p in g.players) {
                if (p is HumanPlayer) {
                    val builder = skill_yi_xin_toc.newBuilder()
                    builder.enable = true
                    builder.card = card.toPbCard()
                    builder.playerId = p.getAlternativeLocation(r.location)
                    builder.targetPlayerId = p.getAlternativeLocation(target.location)
                    p.send(builder.build())
                }
            }
            g.addEvent(AddMessageCardEvent(event.whoseTurn))
            return ResolveResult(fsm, true)
        }
    }
}