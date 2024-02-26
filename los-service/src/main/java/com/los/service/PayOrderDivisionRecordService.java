package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.PayOrderDivisionRecord;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author paul 2024/2/25
 */

public interface PayOrderDivisionRecordService extends IService<PayOrderDivisionRecord> {
    /** 更新分账记录为分账成功  ( 单条 )  将：  已受理 更新为： 其他状态    **/
    public void updateRecordSuccessOrFailBySingleItem(Long recordId, Byte state, String channelRespResult);
    /** 更新分账记录( 多条 )为 其他状态 **/
    public void updateRecordSuccessOrFail(List<PayOrderDivisionRecord> records, Byte state, String channelBatchOrderId, String channelRespResult);
    /** 更新分账订单为： 等待分账中状态  **/
    @Transactional
    public void updateResendState(String payOrderId);
}
