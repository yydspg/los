package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayOrderDivisionRecord;
import com.los.core.exception.BizException;
import com.los.core.utils.SeqKit;
import com.los.service.PayOrderDivisionRecordService;
import com.los.service.mapper.PayOrderDivisionRecordMapper;
import com.los.service.mapper.PayOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author paul 2024/2/25
 */

public class PayOrderDivisionRecordServiceImpl extends ServiceImpl<PayOrderDivisionRecordMapper,PayOrderDivisionRecord> implements PayOrderDivisionRecordService {

    @Autowired private PayOrderMapper payOrderMapper;


    @Override
    public void updateRecordSuccessOrFailBySingleItem(Long recordId, Byte state, String channelRespResult) {
        PayOrderDivisionRecord updateRecord = new PayOrderDivisionRecord();
        updateRecord.setState(state);
        updateRecord.setChannelRespResult( state == PayOrderDivisionRecord.STATE_SUCCESS ? "" : channelRespResult); // 若明确成功，清空错误信息。
        this.update(updateRecord, PayOrderDivisionRecord.gw().eq(PayOrderDivisionRecord::getRecordId, recordId).eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_ACCEPT));

    }

    @Override
    public void updateRecordSuccessOrFail(List<PayOrderDivisionRecord> records, Byte state, String channelBatchOrderId, String channelRespResult) {
        if(records == null || records.isEmpty()){
            return ;
        }

        List<Long> recordIds = new ArrayList<>();
        records.forEach(r -> recordIds.add(r.getRecordId()));

        PayOrderDivisionRecord updateRecord = new PayOrderDivisionRecord();
        updateRecord.setState(state);
        updateRecord.setChannelBatchOrderId(channelBatchOrderId);
        updateRecord.setChannelRespResult(channelRespResult);
        //更新代处理状态-->state
        this.update(updateRecord, PayOrderDivisionRecord.gw().in(PayOrderDivisionRecord::getRecordId, recordIds).eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_WAIT));

    }

    @Override
    public void updateResendState(String payOrderId) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setDivisionState(PayOrder.DIVISION_STATE_WAIT_TASK);

        // 更新订单
        int payOrderUpdateRow = payOrderMapper.update(updateRecord, PayOrder.gw().eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_FINISH));

        if(payOrderUpdateRow <= 0){
            throw new BizException("更新订单分账状态失败");
        }

        PayOrderDivisionRecord updateRecordByDiv = new PayOrderDivisionRecord();
        updateRecordByDiv.setBatchOrderId(SeqKit.genDivisionBatchId()); // 重新生成batchOrderId, 避免部分失败导致： out_trade_no重复。
        updateRecordByDiv.setState(PayOrderDivisionRecord.STATE_WAIT); //待分账
        updateRecordByDiv.setChannelRespResult("");
        updateRecordByDiv.setChannelBatchOrderId("");
        boolean recordUpdateFlag = update(updateRecordByDiv,
                PayOrderDivisionRecord.gw().eq(PayOrderDivisionRecord::getPayOrderId, payOrderId).eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_FAIL)
        );

        if(!recordUpdateFlag){
            throw new BizException("更新分账记录状态失败");
        }
    }
}
