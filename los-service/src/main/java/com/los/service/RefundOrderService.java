package com.los.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.RefundOrder;

/*
 * <p>
 * 退款订单表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface RefundOrderService extends IService<RefundOrder> {
    /* 查询商户订单 **/
    public RefundOrder queryMchOrder(String mchNo, String mchRefundNo, String refundOrderId);
    /* 更新退款单状态  【退款单生成】 --》 【退款中】 **/
    public boolean updateInit2Ing(String refundOrderId, String channelOrderNo);
    /* 更新退款单状态  【退款中】 --》 【退款成功】 **/
    public boolean updateIng2Success(String refundOrderId, String channelOrderNo);
    /* 更新退款单状态  【退款中】 --》 【退款失败】 **/
    public boolean updateIng2Fail(String refundOrderId, String channelOrderNo, String channelErrCode, String channelErrMsg);
    /* 更新退款单状态  【退款中】 --》 【退款成功/退款失败】,传递条件参数 **/
    public boolean updateIng2SuccessOrFail(String refundOrderId, Byte updateState, String channelOrderNo, String channelErrCode, String channelErrMsg);
    /* 更新退款单为 关闭状态 **/
    public Integer updateOrderExpired();
    public IPage<RefundOrder> pageList(IPage iPage, LambdaQueryWrapper<RefundOrder> wrapper, RefundOrder refundOrder, JSONObject paramJSON);

    public long sumSuccessRefundAmount(String payOrderId);
}
