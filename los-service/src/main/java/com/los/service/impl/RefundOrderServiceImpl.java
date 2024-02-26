package com.los.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.RefundOrder;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.service.mapper.PayOrderMapper;
import com.los.service.mapper.RefundOrderMapper;
import com.los.service.RefundOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

/**
* <p>
    * 退款订单表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class RefundOrderServiceImpl extends ServiceImpl<RefundOrderMapper, RefundOrder> implements RefundOrderService {
    @Resource
    private PayOrderMapper payOrderMapper;

    @Override
    public RefundOrder queryMchOrder(String mchNo, String mchRefundNo, String refundOrderId) {
        if(StringKit.isNotEmpty(refundOrderId)){
            return getOne(RefundOrder.gw().eq(RefundOrder::getMchNo, mchNo).eq(RefundOrder::getRefundOrderId, refundOrderId));
        }else if(StringKit.isNotEmpty(mchRefundNo)){
            return getOne(RefundOrder.gw().eq(RefundOrder::getMchNo, mchNo).eq(RefundOrder::getMchRefundNo, mchRefundNo));
        }else{
            return null;
        }
    }

    @Override
    public boolean updateInit2Ing(String refundOrderId, String channelOrderNo) {
        RefundOrder updateRecord = new RefundOrder();
        updateRecord.setState(RefundOrder.STATE_ING);
        updateRecord.setChannelOrderNo(channelOrderNo);

        return update(updateRecord, new LambdaUpdateWrapper<RefundOrder>()
                .eq(RefundOrder::getRefundOrderId, refundOrderId).eq(RefundOrder::getState, RefundOrder.STATE_INIT));

    }

    @Override
    @Transactional
    public boolean updateIng2Success(String refundOrderId, String channelOrderNo) {
        RefundOrder updateRecord = new RefundOrder();
        updateRecord.setState(RefundOrder.STATE_SUCCESS);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setSuccessTime(new Date());

        /*1. 更新"退款订单表"数据 */
        if(! update(updateRecord, new LambdaUpdateWrapper<RefundOrder>()
                .eq(RefundOrder::getRefundOrderId, refundOrderId).eq(RefundOrder::getState, RefundOrder.STATE_ING))
        ){
            return false;
        }

        /*2. 更新"订单表"数据（更新退款次数,退款状态,如全额退款更新支付状态为已退款） */
        RefundOrder refundOrder = getOne(RefundOrder.gw().select(RefundOrder::getPayOrderId, RefundOrder::getRefundAmount).eq(RefundOrder::getRefundOrderId, refundOrderId));
        int updateCount = payOrderMapper.updateRefundAmountAndCount(refundOrder.getPayOrderId(), refundOrder.getRefundAmount());
        if(updateCount <= 0){
            throw new BizException("更新订单数据异常");
        }

        return true;
    }

    @Override
    //TODO 此处需要事务吗
    @Transactional
    public boolean updateIng2Fail(String refundOrderId, String channelOrderNo, String channelErrCode, String channelErrMsg) {
        RefundOrder updateRecord = new RefundOrder();
        updateRecord.setState(RefundOrder.STATE_FAIL);
        updateRecord.setErrCode(channelErrCode);
        updateRecord.setErrMsg(channelErrMsg);
        updateRecord.setChannelOrderNo(channelOrderNo);

        return update(updateRecord, new LambdaUpdateWrapper<RefundOrder>()
                .eq(RefundOrder::getRefundOrderId, refundOrderId).eq(RefundOrder::getState, RefundOrder.STATE_ING));
    }

    @Override
    //TODO 此处需要事务吗
    @Transactional
    public boolean updateIng2SuccessOrFail(String refundOrderId, Byte updateState, String channelOrderNo, String channelErrCode, String channelErrMsg) {
        if(updateState == RefundOrder.STATE_ING){
            return true;
        }else if(updateState == RefundOrder.STATE_SUCCESS){
            return updateIng2Success(refundOrderId, channelOrderNo);
        }else if(updateState == RefundOrder.STATE_FAIL){
            return updateIng2Fail(refundOrderId, channelOrderNo, channelErrCode, channelErrMsg);
        }
        return false;
    }

    @Override
    public Integer updateOrderExpired() {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setState(RefundOrder.STATE_CLOSED);

        return baseMapper.update(refundOrder,
                RefundOrder.gw()
                        .in(RefundOrder::getState, Arrays.asList(RefundOrder.STATE_INIT, RefundOrder.STATE_ING))
                        .le(RefundOrder::getExpiredTime, new Date())
        );
    }

    @Override
    public IPage<RefundOrder> pageList(IPage iPage, LambdaQueryWrapper<RefundOrder> wrapper, RefundOrder refundOrder, JSONObject paramJSON) {
        return null;
    }
}
