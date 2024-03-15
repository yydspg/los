package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.RefundOrder;

/*
* <p>
    * 退款订单表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {
    long sumFinishRefundAmount(String payOrderId);
}
