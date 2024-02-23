package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* <p>
    * 支付订单表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface PayOrderMapper extends BaseMapper<PayOrder> {
    Map<String,Object> payCount(Map<String,Object> param);

    List<Map<String,Object>> payTypeCount(Map<String,Object> param);

    List<Map<Object,Object>> selectOrderCount(Map<String,Object> param);

    /** 更新订单退款金额和次数 **/
    int updateRefundAmountAndCount(@Param("payOrderId") String payOrderId, @Param("currentRefundAmount") Long currentRefundAmount);
}
