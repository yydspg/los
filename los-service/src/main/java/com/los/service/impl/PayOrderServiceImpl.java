package com.los.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.IsvInfo;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayWay;
import com.los.core.exception.BizException;
import com.los.core.utils.DateKit;
import com.los.core.utils.StringKit;
import com.los.service.mapper.*;
import com.los.service.PayOrderService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/*
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
        if(StringUtils.isNotEmpty(payOrderId)){
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getPayOrderId, payOrderId));
        }else if(StringUtils.isNotEmpty(mchOrderNo)){
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getMchOrderNo, mchOrderNo));
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> payCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        return payOrderMapper.payCount(this.genPayCountParams(mchNo,state,refundState,dayStart,dayEnd));
    }

    @Override
    public List<Map<String, Object>> payTypeCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        return payOrderMapper.payTypeCount(this.genPayCountParams(mchNo,state,refundState,dayStart,dayEnd));
    }
    private Map<String,Object> genPayCountParams(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        Map<String ,Object> param = new HashMap<>();
        if (state != null) {
            param.put("state", state);
        }
        if (refundState != null) {
            param.put("refundState", refundState);
        }
        if (StrUtil.isNotBlank(mchNo)) {
            param.put("mchNo", mchNo);
        }
        if (StrUtil.isNotBlank(dayStart)) {
            param.put("createTimeStart", dayStart);
        }
        if (StrUtil.isNotBlank(dayEnd)) {
            param.put("createTimeEnd", dayEnd);
        }
        return param;
    }
    @Override
    public Integer updateOrderExpired() {
        PayOrder payOrder = new PayOrder();
        payOrder.setState(PayOrder.STATE_CLOSED);
        return payOrderMapper.update(payOrder,PayOrder.gw()
                .in(PayOrder::getState,Arrays.asList(PayOrder.STATE_INIT,PayOrder.STATE_ING))
                .le(PayOrder::getExpiredTime,new Date()));
    }

    @Override
    public int updateNotifySent(String payOrderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setNotifyState(CS.YES);
        payOrder.setPayOrderId(payOrderId);
        return payOrderMapper.updateById(payOrder);
    }

    @Override
    public JSONObject mainPageWeekCount(String mchNo) {
        return null;
    }

    @Override
    public JSONObject mainPageNumCount(String mchNo) {
        JSONObject resJson = new JSONObject();
        /* 商户总数*/
        long mchCount = mchInfoMapper.selectCount(MchInfo.gw());
        /* 服务商总数 */
        long isvCount = isvInfoMapper.selectCount(IsvInfo.gw());
        /* 总交易成功金额 */
        Map<String, Object> payCountMap = this.payCount(mchNo, PayOrder.STATE_SUCCESS, null, null, null);
        resJson.put("totalMch",mchCount);
        resJson.put("totalIsv",isvCount);
        resJson.put("totalAmount",payCountMap.get("payAmount"));
        resJson.put("totalCount",payCountMap.get("payCount"));
        return resJson;
    }

    @Override
    public List<Map<Object, Object>> mainPagePayCount(String mchNo, String createdStart, String createdEnd) {
        /* 参数构造 */
        HashMap<String, Object> params = new HashMap<>();
        /* 默认天数 */
        int intervalDay = 6;
        if(StringKit.isNotEmpty(createdStart) && StringKit.isNotEmpty(createdEnd)) {
            createdStart = createdStart + " 00:00:00";
            createdEnd = createdEnd + " 23:59:59";
            intervalDay = DateKit.getIntervalDate(createdStart,createdEnd);
        }else {
            Date today = new Date();
            createdStart = DateKit.formatDate(DateKit.offsetDay(today,-intervalDay)) + "00:00:00";
            createdEnd = DateKit.formatDate(today) + "23:59:59";
        }
        if (StringKit.isNotBlank(mchNo)) {
            params.put("mchNo",mchNo);
        }
        params.put("createdTimeStart",createdStart);
        params.put("createdTimeEnd",createdEnd);
        /* 收款记录 */
        List<Map<Object,Object>> payOrderList = payOrderMapper.selectOrderCount(params);
        /* 退款记录 */
        List<Map<Object,Object>> refundOrderList = payOrderMapper.selectOrderCount(params);
        /* 返回前端请求参数 */
        return this.getReturnList(intervalDay, createdEnd, payOrderList, refundOrderList);
    }

    @Override
    public ArrayList<Map<String,Object>> mainPagePayTypeCount(String mchNo, String createdStart, String createdEnd) {

        if(StringKit.isNotEmpty(createdStart) && StringKit.isNotEmpty(createdEnd)) {
            createdStart = createdStart + "00:00:00";
            createdEnd = createdEnd + "23:59:59";
        } else {
            /* current date */
            Date endDay = new Date();
            /* one week ago */
            Date startOneWeekDay = DateKit.lastWeek().toJdkDate();
            String end = DateKit.formatDate(endDay);
            String start = DateKit.formatDate(startOneWeekDay);
            createdStart = start + "00:00:00";
            createdEnd = end + "23:59:59";
        }
        /* 统计列表 TODO 学习理解sql查询结果*/
        List<Map<String, Object>> payCountMap = this.payTypeCount(mchNo, PayOrder.STATE_SUCCESS, null, createdStart, createdEnd);
        /* 获取所有支付方式 TODO 此处可以做本地缓存*/
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayMapper.selectList(PayWay.gw());
        for (PayWay payWay:payWayList) {
            payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
        }
        for (Map<String, Object> payCount : payCountMap) {
            //爆红原因是未检查为null
            if(StringKit.isNotEmpty(payWayNameMap.get( payCount.get("wayCode")))){
                payCount.put("typeName",payWayNameMap.get(payCount.get("wayCode")));
            }else {
                payCount.put("typeName",payCount.get("wayCode"));
            }
        }
        //TODO 为何封装为ArrayList
        return new ArrayList<Map<String, Object>>(payCountMap);
    }

    @Override
    public List<Map<Object,Object>> getReturnList(int daySpace, String createdStart, List<Map<Object,Object>> payOrderList, List<Map<Object,Object>> refundOrderList) {

        List<Map<String,String>> dayList = new ArrayList<>();
        DateTime endDay = DateKit.parseDateTime(createdStart);
        //todo 存时间能否用别的数据结构
        for (int i = 0; i <= daySpace; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("date",DateKit.format(DateKit.offsetDay(endDay,-i),"MM-dd"));
            dayList.add(map);
        }
        //日期倒序排列
        Collections.reverse(dayList);
        List<Map<Object, Object>> payListMap = new ArrayList<>();
        List<Map<Object, Object>> refundListMap = new ArrayList<>();
        for (Map<String, String> dayMap : dayList) {
            // 为收款列和退款列赋值默认参数【payAmount字段切记不可为string，否则前端图表解析不出来】
            Map<Object, Object> payMap = new HashMap<>();
            payMap.put("date", dayMap.get("date").toString());
            payMap.put("type", "收款");
            payMap.put("payAmount", 0);

            Map<Object, Object> refundMap = new HashMap<>();
            refundMap.put("date", dayMap.get("date").toString());
            refundMap.put("type", "退款");
            refundMap.put("payAmount", 0);
            //TODO 显然存在性能问题,急需优化
            for (Map<Object, Object> payOrderMap : payOrderList) {
                if(dayMap.get("date").equals(payOrderMap.get("groupDate"))) {
                    payMap.put("payAmount",payOrderMap.get("payAmount"));
                }
            }
            payListMap.add(payMap);
            for (Map<Object, Object> refundOrderMap : refundListMap) {
                if (dayMap.get("date").equals(refundOrderMap.get("groupDate"))) {
                    refundMap.put("payAmount", refundOrderMap.get("refundAmount"));
                }
            }
            refundListMap.add(refundMap);
        }
        payListMap.addAll(refundListMap);
        return payListMap;
    }
    /* 计算商家实际收入 */
    @Override
    public Long calMchIncomeAmount(PayOrder dbPayOrder) {
        /*
        商家订单入账金额 = 支付金额 - 手续费 - 退款金额 - 总分账金额
         */
        long mchIncomeAmount = dbPayOrder.getAmount()  - dbPayOrder.getMchFeeAmount() - dbPayOrder.getRefundAmount();
        mchIncomeAmount -= payOrderDivisionRecordMapper.sumSuccessDivisionAmount(dbPayOrder.getPayOrderId());
        return mchIncomeAmount <= 0 ? 0 : mchIncomeAmount;
    }

    @Override
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {
        return null;
        //TODO 未实现
    }
}
