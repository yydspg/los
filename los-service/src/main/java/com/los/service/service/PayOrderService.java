package com.los.service.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.PayOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付订单表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface PayOrderService extends IService<PayOrder> {
    /** 更新订单状态  【订单生成】 --》 【订单关闭】 **/
    boolean updateInit2Close(String payOrderId);
    /** 更新订单状态  【订单生成】 --> 【支付中】 **/
    boolean updateInit2Ing(String payOrderId,PayOrder payOrder);
    /** 更新订单状态  【支付中】 --》 【支付成功】 **/
    boolean updateIng2Success(String payOrderId,String channelOrderNo,String channelUserId);
    /** 更新订单状态  【支付中】 --》 【订单关闭】 **/
    boolean updateIng2Close(String payOrderId);
    /** 更新订单状态  【支付中】 --》 【支付失败】 **/
    boolean updateIng2Fail(String payOrderId, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg);
    /** 更新订单状态  【支付中】 --》 【支付成功/支付失败】 **/
    public boolean updateIng2SuccessOrFail(String payOrderId, Byte updateState, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg);
    /** 查询商户订单 **/
    public PayOrder queryMchOrder(String mchNo, String payOrderId, String mchOrderNo);
    public Map<String,Object> payCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd);
    public List<Map> payTypeCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd);
    public Integer updateOrderExpired();
    public int updateNotifySent(String payOrderId);
    public JSONObject mainPageWeekCount(String mchNo);
    public JSONObject mainPageNumCount(String mchNo);
    public List<Map> mainPagePayCount(String mchNo, String createdStart, String createdEnd);
    public ArrayList mainPagePayTypeCount(String mchNo, String createdStart, String createdEnd);
    public List<Map> getReturnList(int daySpace, String createdStart, List<Map> payOrderList, List<Map> refundOrderList);
    public Long calMchIncomeAmount(PayOrder dbPayOrder);
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper);

}
