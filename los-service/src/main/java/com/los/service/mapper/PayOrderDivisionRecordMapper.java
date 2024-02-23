package com.los.service.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.entity.PayOrderDivisionRecord;
import org.apache.ibatis.annotations.Param;

/**
 * @author paul 2024/2/21
 */

public interface PayOrderDivisionRecordMapper extends BaseMapper<PayOrderDivisionRecord> {
    Long sumSuccessDivisionAmount(String payOrderId);

    IPage<PayOrderDivisionRecord> distinctBatchOrderIdList(IPage<?> page, @Param("ew") Wrapper<PayOrderDivisionRecord> wrapper);
}
