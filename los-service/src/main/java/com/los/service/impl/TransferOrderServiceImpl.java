package com.los.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.TransferOrder;
import com.los.service.mapper.TransferOrderMapper;
import com.los.service.TransferOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/*
* <p>
    * 转账订单表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrder> implements TransferOrderService {

    @Override
    public boolean updateInit2Ing(String transferId) {
        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_ING);

        return update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_INIT));
    }

    @Override
    public boolean updateIng2Success(String transferId, String channelOrderNo) {
        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_SUCCESS);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setSuccessTime(new Date());

        //更新转账订单表数据
        if(! update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_ING))
        ){
            return false;
        }

        return true;
    }

    @Override
    public boolean updateIng2Fail(String transferId, String channelOrderNo, String channelErrCode, String channelErrMsg) {
        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_FAIL);
        updateRecord.setErrCode(channelErrCode);
        updateRecord.setErrMsg(channelErrMsg);
        updateRecord.setChannelOrderNo(channelOrderNo);

        return update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_ING));
    }

    @Override
    public boolean updateIng2SuccessOrFail(String transferId, Byte updateState, String channelOrderNo, String channelErrCode, String channelErrMsg) {
        if(updateState == TransferOrder.STATE_ING){
            return true;
        }else if(updateState == TransferOrder.STATE_SUCCESS){
            return this.updateIng2Success(transferId, channelOrderNo);
        }else if(updateState == TransferOrder.STATE_FAIL){
            return this.updateIng2Fail(transferId, channelOrderNo, channelErrCode, channelErrMsg);
        }
        return false;
    }

    @Override
    public TransferOrder queryMchOrder(String mchNo, String mchOrderNo, String transferId) {
        if(StringUtils.isNotEmpty(transferId)){
            return this.getOne(TransferOrder.gw().eq(TransferOrder::getMchNo, mchNo).eq(TransferOrder::getTransferId, transferId));
        }else if(StringUtils.isNotEmpty(mchOrderNo)){
            return this.getOne(TransferOrder.gw().eq(TransferOrder::getMchNo, mchNo).eq(TransferOrder::getMchOrderNo, mchOrderNo));
        }else{
            return null;
        }
    }

    @Override
    public IPage<TransferOrder> pageList(IPage iPage, LambdaQueryWrapper<TransferOrder> wrapper, TransferOrder transferOrder, JSONObject paramJSON) {
        return null;
    }
}
