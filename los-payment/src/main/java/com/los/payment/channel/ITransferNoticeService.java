package com.los.payment.channel;

import com.los.core.entity.TransferOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;

/**
 *  转账订单通知解析实现 异步回调
 * @author paul 2024/2/28
 */

public interface ITransferNoticeService {
    /** 获取到接口code **/
    String getIfCode();

    /** 解析参数： 转账单号 和 请求参数
     *  异常需要自行捕捉，并返回null , 表示已响应数据。
     * **/
    MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId);

    /** 返回需要更新的订单状态 和响应数据 **/
    ChannelRetMsg doNotice(HttpServletRequest request,
                           Object params, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext);

    /** 数据库订单数据不存在  (仅异步通知使用) **/
    ResponseEntity doNotifyOrderNotExists(HttpServletRequest request);

}
