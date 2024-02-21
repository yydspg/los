package com.los.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.service.mapper.*;
import com.los.service.service.PayOrderService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* <p>
    * 支付订单表 服务实现类
    * </p>
*
 * TODO 订单表主键数据类型的选择
* @author paul
* @since 2024-02-05
*/
@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {
    @Resource private PayOrderMapper payOrderMapper;
    @Resource private MchInfoMapper mchInfoMapper;
    @Resource private IsvInfoMapper isvInfoMapper;
    @Resource private PayWayMapper payWayMapper;
    @Resource private PayOrderDivisionRecordMapper payOrderDivisionRecordMapper;

    @Override
    public boolean updateInit2Close(String payOrderId) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }

    @Override
    public boolean updateInit2Ing(String payOrderId, PayOrder payOrder) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_ING);
        //更新订单状态  [订单生成]-->[支付中],确定其他信息,ex: 支付接口确认,费率计算.etc
        //属性转移
        updateRecord.setIfCode(payOrder.getIfCode());
        updateRecord.setWayCode(payOrder.getWayCode());
        updateRecord.setMchFeeRate(payOrder.getMchFeeRate());
        updateRecord.setMchFeeAmount(payOrder.getMchFeeAmount());
        updateRecord.setChannelUser(payOrder.getChannelUser());
        updateRecord.setChannelOrderNo(payOrder.getChannelOrderNo());
        return this.update(updateRecord,new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId,payOrderId)
                .eq(PayOrder::getState,PayOrder.STATE_ING));
    }

    @Override
    public boolean updateIng2Success(String payOrderId, String channelOrderNo, String channelUserId) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_SUCCESS);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setChannelUser(channelUserId);
        updateRecord.setSuccessTime(new Date());
        return this.update(updateRecord,new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId,payOrderId)
                .eq(PayOrder::getState,PayOrder.STATE_ING));
    }

    @Override
    public boolean updateIng2Close(String payOrderId) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
    }

    @Override
    public boolean updateIng2Fail(String payOrderId, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_FAIL);
        updateRecord.setErrCode(channelErrCode);
        updateRecord.setErrMsg(channelErrMsg);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setChannelUser(channelUserId);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
    }
    /*
    在Java 14及以上版本中，如果你在switch表达式中尝试使用return关键字，而不是yield关键字来返回值，
    Java编译器会报错："Return outside of enclosing function"或"Return outside of enclosing switch expression"。
    这是因为switch表达式有自己的返回机制，需要使用yield关键字来返回结果。
    要解决这个问题，请将return替换为yield，或者如果你实际上希望从包含switch表达式的函数中返回，
    可以将switch表达式放在一个变量赋值语句中，然后在switch之外使用return。
     */
    @Override
    public boolean updateIng2SuccessOrFail(String payOrderId, Byte updateState, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg) {
        return switch (updateState) {
            case PayOrder.STATE_ING -> {yield  true;}
            case PayOrder.STATE_SUCCESS -> {yield updateIng2Success(payOrderId, channelOrderNo, channelUserId);}
            case PayOrder.STATE_FAIL -> {yield updateIng2Fail(payOrderId, channelOrderNo, channelUserId, channelErrCode, channelErrMsg);}
            default -> {throw new BizException("unknownError");}
            };
    }

    @Override
    public PayOrder queryMchOrder(String mchNo, String payOrderId, String mchOrderNo) {
        return null;
    }

    @Override
    public Map<String, Object> payCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        return null;
    }

    @Override
    public List<Map> payTypeCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        return null;
    }

    @Override
    public Integer updateOrderExpired() {
        return null;
    }

    @Override
    public int updateNotifySent(String payOrderId) {
        return 0;
    }

    @Override
    public JSONObject mainPageWeekCount(String mchNo) {
        return null;
    }

    @Override
    public JSONObject mainPageNumCount(String mchNo) {
        return null;
    }

    @Override
    public List<Map> mainPagePayCount(String mchNo, String createdStart, String createdEnd) {
        return null;
    }

    @Override
    public ArrayList mainPagePayTypeCount(String mchNo, String createdStart, String createdEnd) {
        return null;
    }

    @Override
    public List<Map> getReturnList(int daySpace, String createdStart, List<Map> payOrderList, List<Map> refundOrderList) {
        return null;
    }

    @Override
    public Long calMchIncomeAmount(PayOrder dbPayOrder) {
        return null;
    }

    @Override
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {
        return null;
    }
}
