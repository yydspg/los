
package com.los.payment.channel.wxpay;

import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.los.payment.channel.AbstractRefundService;
import com.los.payment.channel.wxpay.kits.WxpayKit;
import com.los.payment.channel.wxpay.kits.WxpayV3Util;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.refund.RefundOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * 退款接口： 微信官方
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/17 16:38
 */
@Slf4j
@Service
public class WxpayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    /** 微信退款接口 **/
    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {

            ChannelRetMsg channelRetMsg = new ChannelRetMsg();

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayRefundRequest req = new WxPayRefundRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payOrder.getPayOrderId());    // 商户订单号
                req.setOutRefundNo(refundOrder.getRefundOrderId()); // 退款单号
                req.setTotalFee(payOrder.getAmount().intValue());   // 订单总金额
                req.setRefundFee(refundOrder.getRefundAmount().intValue()); // 退款金额
                req.setNotifyUrl(getNotifyUrl(refundOrder.getRefundOrderId()));   // 回调url
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayRefundResult result = wxPayService.refundV2(req);
                if("SUCCESS".equals(result.getResultCode())){ // 退款发起成功,结果主动查询
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelOrderId(result.getRefundId());
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(result.getErrCode());
                    channelRetMsg.setChannelErrMsg(WxpayKit.appendErrMsg(result.getReturnMsg(), result.getErrCodeDes()));
                }
            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3
                // 微信统一下单请求对象
                JSONObject reqJSON = new JSONObject();
                reqJSON.put("out_trade_no", refundOrder.getPayOrderId());   // 订单号
                reqJSON.put("out_refund_no", refundOrder.getRefundOrderId()); // 退款订单号
                reqJSON.put("notify_url", getNotifyUrl(refundOrder.getRefundOrderId())); // 回调地址

                JSONObject amountJson = new JSONObject();
                amountJson.put("refund", refundOrder.getRefundAmount());// 退款金额
                amountJson.put("total", payOrder.getAmount());// 订单总金额
                amountJson.put("currency", "CNY");// 币种
                reqJSON.put("amount", amountJson);

                if(mchAppConfigContext.isIsvSubMch()){ // 特约商户
                    WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams)configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
                    reqJSON.put("sub_mchid", isvsubMchParams.getSubMchId());
                }

                JSONObject resultJSON = WxpayV3Util.refundV3(reqJSON, wxServiceWrapper.getWxPayService());
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    channelRetMsg.setChannelOrderId(refundId);
                }else if ("PROCESSING".equals(status)){ // 退款处理中
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelOrderId(refundId);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrMsg(status);
                }

            }
            return channelRetMsg;
        } catch (WxPayException e) {
            log.error("微信退款WxPayException异常: ", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
            return channelRetMsg;

        } catch (Exception e) {
            log.error("微信退款Exception异常: ", e);
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

    /** 微信退款查单接口 **/
    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {
            ChannelRetMsg channelRetMsg = new ChannelRetMsg();

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);


            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayRefundQueryRequest req = new WxPayRefundQueryRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutRefundNo(refundOrder.getRefundOrderId()); // 退款单号
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayRefundQueryResult result = wxPayService.refundQueryV2(req);
                if("SUCCESS".equals(result.getResultCode())){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(result.getReturnMsg());
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3
                WxPayService wxPayService = wxServiceWrapper.getWxPayService();
                JSONObject resultJSON = null;
                if (mchAppConfigContext.isIsvSubMch()) {
                    WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams)configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
                    wxPayService.getConfig().setSubMchId(isvsubMchParams.getSubMchId());
                    resultJSON = WxpayV3Util.refundQueryV3Isv(refundOrder.getRefundOrderId(), wxPayService);
                }else {
                    resultJSON = WxpayV3Util.refundQueryV3(refundOrder.getRefundOrderId(), wxPayService);
                }
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(status);
                }
            }
            return channelRetMsg;
        } catch (WxPayException e) {
            log.error("微信退款查询WxPayException异常: ", e);
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            log.error("微信退款查询Exception异常: ", e);
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
