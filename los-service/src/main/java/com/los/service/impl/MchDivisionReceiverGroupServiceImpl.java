package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.MchDivisionReceiverGroup;
import com.los.service.mapper.MchDivisionReceiverGroupMapper;
import com.los.service.MchDivisionReceiverGroupService;
import com.los.service.MchDivisionReceiverService;
import org.springframework.stereotype.Service;

/*
 * @author paul 2024/2/22
 */
@Service
public class MchDivisionReceiverGroupServiceImpl  extends ServiceImpl<MchDivisionReceiverGroupMapper, MchDivisionReceiverGroup> implements MchDivisionReceiverGroupService {

    @Override
    public MchDivisionReceiverGroup findByIdAndMchNo(Long groupId, String mchNo) {
        return this.getOne(MchDivisionReceiverGroup.gw()
                .eq(MchDivisionReceiverGroup::getReceiverGroupId,groupId)
                .eq(MchDivisionReceiverGroup::getMchNo,mchNo));
    }
}
