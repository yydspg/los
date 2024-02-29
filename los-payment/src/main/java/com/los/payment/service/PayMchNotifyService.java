package com.los.payment.service;

import com.alibaba.fastjson.JSONObject;
import com.los.components.mq.model.PayOrderMchNotifyMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.entity.MchNotifyRecord;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.entity.TransferOrder;
import com.los.core.utils.SecKit;
import com.los.core.utils.StringKit;
import com.los.payment.rqrs.payorder.QueryPayOrderRS;
import com.los.payment.rqrs.refund.QueryRefundOrderRS;
import com.los.payment.rqrs.transfer.QueryTransferOrderRS;
import com.los.service.MchNotifyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author paul 2024/2/28
 */
@Slf4j
@Service
public class PayMchNotifyService {
    @Autowired private MchNotifyRecordService mchNotifyRecordService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private IMQSender mqSender;

    public void payOrderNotify(PayOrder dbPayOrder) {
        try {

            /* 通知地址为空,返回 */

            if(StringKit.isEmpty(dbPayOrder.getNotifyUrl())) {return ;}


            /* 获取通知对象 */

            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByPayOrder(dbPayOrder.getPayOrderId());

            /* 查重操作 */

            if (mchNotifyRecord != null) {
                log.info("已发送");
                return;
            }

            /* 获取商户私钥 */

            String appSecret = configContextQueryService.queryMchApp(dbPayOrder.getMchNo(), dbPayOrder.getAppId()).getAppSecret();

            /* 封装通知url */

            String notifyUrl = createNotifyUrl(dbPayOrder, appSecret);
            dbPayOrder.setNotifyUrl(notifyUrl);

            mchNotifyRecord = this.buildMchNotifyRecord(dbPayOrder.getPayOrderId(), MchNotifyRecord.TYPE_PAY_ORDER,dbPayOrder.getMchNo(),dbPayOrder.getMchOrderNo(),
                    dbPayOrder.getIsvNo(),dbPayOrder.getAppId(),dbPayOrder.getNotifyUrl(),"",0,MchNotifyRecord.STATE_SUCCESS);
            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            /* 推送到MQ */

            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败:{}",e.getMessage());
        }
    }
    public void refundOrderNotify(RefundOrder dbRefundOrder) {
        try {

            /* 通知地址为空,返回 */

            if(StringKit.isEmpty(dbRefundOrder.getNotifyUrl())) {return ;}


            /* 获取通知对象 */

            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByRefundOrder(dbRefundOrder.getRefundOrderId());

            /* 查重操作 */

            if (mchNotifyRecord != null) {
                log.info("已发送");
                return;
            }

            /* 获取商户私钥 */

            String appSecret = configContextQueryService.queryMchApp(dbRefundOrder.getMchNo(), dbRefundOrder.getAppId()).getAppSecret();

            /* 封装通知url */

            String notifyUrl = createNotifyUrl(dbRefundOrder, appSecret);
            dbRefundOrder.setNotifyUrl(notifyUrl);

            mchNotifyRecord = this.buildMchNotifyRecord(dbRefundOrder.getPayOrderId(), MchNotifyRecord.TYPE_REFUND_ORDER,dbRefundOrder.getMchNo(),dbRefundOrder.getMchRefundNo(),
                    dbRefundOrder.getIsvNo(),dbRefundOrder.getAppId(),dbRefundOrder.getNotifyUrl(),"",0,MchNotifyRecord.STATE_SUCCESS);
            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            /* 推送到MQ */

            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败:{}",e.getMessage());
        }
    }
    public void transferOrderNotify(TransferOrder dbTransferOrder) {
        try {

            /* 通知地址为空,返回 */

            if(StringKit.isEmpty(dbTransferOrder.getNotifyUrl())) {return ;}


            /* 获取通知对象 */

            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByTransferOrder(dbTransferOrder.getTransferId());

            /* 查重操作 */

            if (mchNotifyRecord != null) {
                log.info("已发送");
                return;
            }

            /* 获取商户私钥 */

            String appSecret = configContextQueryService.queryMchApp(dbTransferOrder.getMchNo(), dbTransferOrder.getAppId()).getAppSecret();

            /* 封装通知url */

            String notifyUrl = createNotifyUrl(dbTransferOrder, appSecret);
            dbTransferOrder.setNotifyUrl(notifyUrl);

            mchNotifyRecord = this.buildMchNotifyRecord(dbTransferOrder.getTransferId(), MchNotifyRecord.TYPE_TRANSFER_ORDER,dbTransferOrder.getMchNo(),dbTransferOrder.getMchOrderNo(),
                    dbTransferOrder.getIsvNo(),dbTransferOrder.getAppId(),dbTransferOrder.getNotifyUrl(),"",0,MchNotifyRecord.STATE_SUCCESS);
            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            /* 推送到MQ */

            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败:{}",e.getMessage());
        }
    }

    /**
     * 创建响应URL
     */
    public String createNotifyUrl(PayOrder payOrder, String appSecret) {

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", SecKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(payOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(RefundOrder refundOrder, String appSecret) {

        QueryRefundOrderRS queryRefundOrderRS = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryRefundOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", SecKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(refundOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(TransferOrder transferOrder, String appSecret) {

        QueryTransferOrderRS rs = QueryTransferOrderRS.buildByRecord(transferOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(rs);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", SecKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(transferOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createReturnUrl(PayOrder payOrder, String appSecret) {

        if(StringUtils.isEmpty(payOrder.getReturnUrl())){
            return "";
        }

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", SecKit.getSign(jsonObject, appSecret));   // 签名

        // 生成跳转地址
        return StringKit.appendUrlQuery(payOrder.getReturnUrl(), jsonObject);

    }
    private MchNotifyRecord buildMchNotifyRecord(String orderId,byte orderType,String mchNo,String mchOrderNo,String isvNo,String appId,String notifyUrl,String resResult,Integer notifyCount,byte state ) {
        MchNotifyRecord res = new MchNotifyRecord();
        res.setOrderId(orderId);
        res.setOrderType(orderType);
        res.setMchNo(mchNo);
        res.setMchOrderNo(mchOrderNo);
        res.setIsvNo(isvNo);
        res.setAppId(appId);
        res.setNotifyUrl(notifyUrl);
        res.setResResult(resResult);
        res.setNotifyCount(notifyCount);
        res.setState(state);
        return res;
    }
}
