package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.MchNotifyRecord;
import com.los.service.mapper.MchNotifyRecordMapper;
import com.los.service.service.MchNotifyRecordService;
import org.springframework.stereotype.Service;

/**
* <p>
    * 商户通知记录表 服务实现类
    * </p>
* 关于商户回调消息处理逻辑
* @author paul
* @since 2024-02-05
*/
@Service
public class MchNotifyRecordServiceImpl extends ServiceImpl<MchNotifyRecordMapper, MchNotifyRecord> implements MchNotifyRecordService {

    @Override
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType) {
        return this.getOne(
                MchNotifyRecord.gw().eq(MchNotifyRecord::getOrderId, orderId).eq(MchNotifyRecord::getOrderType, orderType)
        );
    }

    @Override
    public MchNotifyRecord findByPayOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_PAY_ORDER);
    }

    @Override
    public MchNotifyRecord findByRefundOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_REFUND_ORDER);
    }

    @Override
    public MchNotifyRecord findByTransferOrder(String transferId) {
        return findByOrderAndType(transferId, MchNotifyRecord.TYPE_TRANSFER_ORDER);
    }

    @Override
    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult) {
        //TODO 为何不需注入 Mapper
        return baseMapper.updateNotifyResult(notifyId,state,resResult);
    }

}
