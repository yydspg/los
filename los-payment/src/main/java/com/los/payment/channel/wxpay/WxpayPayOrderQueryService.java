
package com.los.payment.channel.wxpay;

import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.los.payment.channel.IPayOrderQueryService;
import com.los.payment.channel.wxpay.kits.WxpayKit;
import com.los.payment.channel.wxpay.kits.WxpayV3Util;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.msg.ChannelRetMsg;

import com.los.payment.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 微信查单接口
*
* @author zhuxiao
* @site https://www.jeequan.com
* @date 2021/6/8 18:10
*/
@Service
public class WxpayPayOrderQueryService implements IPayOrderQueryService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayOrderQueryRequest req = new WxPayOrderQueryRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payOrder.getPayOrderId());

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayOrderQueryResult result = wxPayService.queryOrder(req);

                if("SUCCESS".equals(result.getTradeState())){ //支付成功
                    return ChannelRetMsg.confirmSuccess(result.getTransactionId());
                }else if("USERPAYING".equals(result.getTradeState())){ //支付中，等待用户输入密码
                    return ChannelRetMsg.waiting(); //支付中
                }else if("CLOSED".equals(result.getTradeState())
                        || "REVOKED".equals(result.getTradeState())
                        || "PAYERROR".equals(result.getTradeState())){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
                    return ChannelRetMsg.confirmFail(); //支付失败
                }else{
                    return ChannelRetMsg.unknown();
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3

                String reqUrl;
                String query;
                if(mchAppConfigContext.isIsvSubMch()){ // 特约商户
                    WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
                    reqUrl = String.format("/v3/pay/partner/transactions/out-trade-no/%s", payOrder.getPayOrderId());
                    query = String.format("?sp_mchid=%s&sub_mchid=%s", wxServiceWrapper.getWxPayService().getConfig().getMchId(), isvsubMchParams.getSubMchId());
                }else {
                    reqUrl = String.format("/v3/pay/transactions/out-trade-no/%s", payOrder.getPayOrderId());
                    query = String.format("?mchid=%s", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                }
                JSONObject resultJSON = WxpayV3Util.queryOrderV3(reqUrl + query, wxServiceWrapper.getWxPayService());

                String channelState = resultJSON.getString("trade_state");
                if ("SUCCESS".equals(channelState)) {
                    return ChannelRetMsg.confirmSuccess(resultJSON.getString("transaction_id"));
                }else if("USERPAYING".equals(channelState)){ //支付中，等待用户输入密码
                    return ChannelRetMsg.waiting(); //支付中
                }else if("CLOSED".equals(channelState)
                        || "REVOKED".equals(channelState)
                        || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
                    return ChannelRetMsg.confirmFail(); //支付失败
                }else{
                    return ChannelRetMsg.unknown();
                }

            }else {
                return ChannelRetMsg.unknown();
            }

        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
