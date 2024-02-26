package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.MchNotifyRecord;

/**
 * <p>
 * 商户通知记录表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface MchNotifyRecordService extends IService<MchNotifyRecord> {
    /** 根据订单号和类型查询 */
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType);
    /** 查询支付订单 */
    public MchNotifyRecord findByPayOrder(String orderId);
    /** 查询退款订单订单 */
    public MchNotifyRecord findByRefundOrder(String orderId);
    /** 查询退款订单订单 */
    public MchNotifyRecord findByTransferOrder(String transferId);
    /** 更新商户回调的结果即状态 **/
    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult);

}
