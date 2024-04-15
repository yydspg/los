
package com.los.payment.channel.wxpay;

import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayOrderCloseRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderCloseResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.los.payment.channel.IPayOrderCloseService;
import com.los.payment.channel.wxpay.kits.WxpayKit;
import com.los.payment.channel.wxpay.kits.WxpayV3Util;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.msg.ChannelRetMsg;

import com.los.payment.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 微信关闭订单
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/24 17:25
 */
@Service
public class WxpayPayOrderCloseService implements IPayOrderCloseService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayOrderCloseRequest req = new WxPayOrderCloseRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payOrder.getPayOrderId());

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayOrderCloseResult result = wxPayService.closeOrder(req);

                if("SUCCESS".equals(result.getResultCode())){ //关闭订单成功
                    return ChannelRetMsg.confirmSuccess(null);
                }else if("FAIL".equals(result.getResultCode())){ //关闭订单失败
                    return ChannelRetMsg.confirmFail(); //关闭失败
                }else{
                    return ChannelRetMsg.waiting(); //关闭中
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3

                String reqUrl;
                JSONObject reqJson = new JSONObject();
                if(mchAppConfigContext.isIsvSubMch()){ // 特约商户
                    WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
                    reqUrl = String.format("/v3/pay/partner/transactions/out-trade-no/%s/close", payOrder.getPayOrderId());
                    reqJson.put("sp_mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                    reqJson.put("sub_mchid", isvsubMchParams.getSubMchId());
                }else {
                    reqUrl = String.format("/v3/pay/transactions/out-trade-no/%s/close", payOrder.getPayOrderId());
                    reqJson.put("mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                }

                WxpayV3Util.closeOrderV3(reqUrl, reqJson, wxServiceWrapper.getWxPayService());
                return ChannelRetMsg.confirmSuccess(null);
            }
            return ChannelRetMsg.confirmFail();
        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
