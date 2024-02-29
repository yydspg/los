package com.los.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.PayOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * <p>
 * 支付订单表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface PayOrderService extends IService<PayOrder> {
    /* 更新订单状态  【订单生成】 --》 【订单关闭】 **/
    boolean updateInit2Close(String payOrderId);
    /* 更新订单状态  【订单生成】 --> 【支付中】 **/
    boolean updateInit2Ing(String payOrderId,PayOrder payOrder);
    /* 更新订单状态  【支付中】 --》 【支付成功】 **/
    boolean updateIng2Success(String payOrderId,String channelOrderNo,String channelUserId);
    /* 更新订单状态  【支付中】 --》 【订单关闭】 **/
    boolean updateIng2Close(String payOrderId);
    /* 更新订单状态  【支付中】 --》 【支付失败】 **/
    boolean updateIng2Fail(String payOrderId, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg);
    /* 更新订单状态  【支付中】 --》 【支付成功/支付失败】 **/
    public boolean updateIng2SuccessOrFail(String payOrderId, Byte updateState, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg);
    /* 查询商户订单 **/
    public PayOrder queryMchOrder(String mchNo, String payOrderId, String mchOrderNo);
    /* 支付统计 **/
    public Map<String,Object> payCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd);
    public List<Map<String,Object>> payTypeCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd);
    /* 更新订单为 超时状态 **/
    public Integer updateOrderExpired();
    /* 更新订单 通知状态 --> 已发送 **/
    public int updateNotifySent(String payOrderId);
    public JSONObject mainPageWeekCount(String mchNo);
    /* 首页统计总数量 **/
    public JSONObject mainPageNumCount(String mchNo);
    /* 首页支付统计 **/
    public List<Map<Object,Object>> mainPagePayCount(String mchNo, String createdStart, String createdEnd);
    /* 首页支付类型统计 **/
    public ArrayList<Map<String,Object>> mainPagePayTypeCount(String mchNo, String createdStart, String createdEnd);
    /* 生成首页交易统计数据类型 **/
    public List<Map<Object,Object>> getReturnList(int daySpace, String createdStart, List<Map<Object,Object>> payOrderList, List<Map<Object,Object>> refundOrderList);
    /*  计算支付订单商家入账金额 商家订单入账金额 （支付金额 - 手续费 - 退款金额 - 总分账金额）**/
    public Long calMchIncomeAmount(PayOrder dbPayOrder);
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper);

}
