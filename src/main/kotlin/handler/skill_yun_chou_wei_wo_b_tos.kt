package com.fengsheng.handler

import com.fengsheng.HumanPlayer
import com.fengsheng.protos.Role
import com.fengsheng.skill.SkillId
import org.apache.logging.log4j.kotlin.logger

class skill_yun_chou_wei_wo_b_tos : AbstractProtoHandler<Role.skill_yun_chou_wei_wo_b_tos>() {
    override fun handle0(r: HumanPlayer, pb: Role.skill_yun_chou_wei_wo_b_tos) {
        val skill = r.findSkill(SkillId.YUN_CHOU_WEI_WO)
        if (skill == null) {
            logger.error("你没有这个技能")
            r.sendErrorMessage("你没有这个技能")
            return
        }
        r.game!!.tryContinueResolveProtocol(r, pb)
    }
}
