package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.MchDivisionReceiverGroup;

/**
 * @author paul 2024/2/22
 */

public interface MchDivisionReceiverGroupService extends IService<MchDivisionReceiverGroup> {
    /** 根据ID和商户号查询 分账组**/
    public MchDivisionReceiverGroup findByIdAndMchNo(Long groupId, String mchNo);
}
