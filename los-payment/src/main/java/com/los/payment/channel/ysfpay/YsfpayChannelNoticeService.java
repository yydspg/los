
package com.los.payment.channel.ysfpay;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.exception.ResException;
import com.los.core.model.params.ysf.YsfpayIsvParams;
import com.los.payment.channel.AbstractChannelNoticeService;
import com.los.payment.channel.ysfpay.utils.YsfSignUtils;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 云闪付回调
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Service
@Slf4j
public class YsfpayChannelNoticeService extends AbstractChannelNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("orderNo");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResException.build("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {

            ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);

            String logPrefix = "【处理云闪付支付回调】";

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info("{} 回调参数, jsonParams：{}", logPrefix, jsonParams);

            // 校验支付回调
            boolean verifyResult = verifyParams(jsonParams, payOrder, mchAppConfigContext);
            // 验证参数失败
            if(!verifyResult){
                throw ResException.build("ERROR");
            }
            log.info("{}验证支付通知数据及签名通过", logPrefix);

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");
            result.setResponseEntity(okResponse);
            result.setChannelOrderId(jsonParams.getString("transIndex"));
            return result;

        } catch (Exception e) {
            log.error("error", e);
            throw ResException.build("ERROR");
        }
    }

    /**
     * 验证云闪付支付通知参数
     * @return
     */
    public boolean verifyParams(JSONObject jsonParams, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        String orderNo = jsonParams.getString("orderNo");		// 商户订单号
        String txnAmt = jsonParams.getString("txnAmt"); 		// 支付金额
        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        YsfpayIsvParams isvParams = (YsfpayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());

        //验签
        String ysfpayPublicKey = isvParams.getYsfpayPublicKey();

        //验签失败
        if(!YsfSignUtils.validate(JSONObject.from(jsonParams), ysfpayPublicKey)) {
            log.info("【云闪付回调】 验签失败！ 回调参数：parameter = {}, ysfpayPublicKey={} ", jsonParams, ysfpayPublicKey);
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.parseLong(txnAmt)) {
            log.info("订单金额与参数金额不符。 dbPayAmt={}, txnAmt={}, payOrderId={}", dbPayAmt, txnAmt, orderNo);
            return false;
        }
        return true;
    }

}
