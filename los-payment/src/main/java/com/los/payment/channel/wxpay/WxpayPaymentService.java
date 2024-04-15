
package com.los.payment.channel.wxpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.los.payment.channel.AbstractPaymentService;
import com.los.payment.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.utils.PaymentKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
* 支付接口： 微信官方
* 支付方式： 自适应
*
* @author zhuxiao
* @site https://www.jeequan.com
* @date 2021/6/8 18:10
*/
@Service
public class WxpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaymentKit.getRealPayWayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        // 微信API版本

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        String apiVersion = wxServiceWrapper.getApiVersion();
        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) {
            return PaymentKit.getRealPayWayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else if (CS.PAY_IF_VERSION.WX_V3.equals(apiVersion)) {
            return PaymentKit.getRealPayWayV3Service(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else {
            throw new BizException("不支持的微信支付API版本");
        }

    }

    /**
     * 构建微信统一下单请求数据
     * @param payOrder
     * @return
     */
    public WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setOutTradeNo(payOrderId);
        request.setBody(payOrder.getSubject());
        request.setDetail(payOrder.getBody());
        request.setFeeType("CNY");
        request.setTotalFee(payOrder.getAmount().intValue());
        request.setSpbillCreateIp(payOrder.getClientIp());
        request.setNotifyUrl(getNotifyUrl());
        request.setProductId(System.currentTimeMillis()+"");
        request.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.PURE_DATETIME_PATTERN));

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            request.setProfitSharing("Y");
        }

        // 特约商户
        if(mchAppConfigContext.isIsvSubMch()){
            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
            request.setSubMchId(isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                request.setSubAppId(isvsubMchParams.getSubMchAppId());
            }
        }

        return request;
    }

    /**
     * 构建微信APIV3接口  统一下单请求数据
     * @author terrfly
     * @param payOrder
     * @return
     */
    public WxpayV3OrderRequestModel buildV3OrderRequestModel(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {


        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        WxpayV3OrderRequestModel result = new WxpayV3OrderRequestModel();
        result.setOutTradeNo(payOrderId);
        result.setDescription(payOrder.getSubject());

        // 订单失效时间，遵循rfc3339标准格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE,示例值：2018-06-08T10:34:56+08:00
        result.setTimeExpire(String.format("%sT%s+08:00", DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATE_FORMAT), DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_TIME_FORMAT)));

        result.setNotifyUrl(getNotifyUrl(payOrderId));

        // 订单金额
        result.setAmount(new WxpayV3OrderRequestModel.Amount().setCurrency("CNY").setTotal(payOrder.getAmount().intValue()));

        // 场景信息
        result.setSceneInfo(new WxpayV3OrderRequestModel.SceneInfo().setPayerClientIp(payOrder.getClientIp()));

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            result.setSettleInfo(new WxpayV3OrderRequestModel.SettleInfo().setProfitSharing(true));
        }

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();

        if(mchAppConfigContext.isIsvSubMch()){ // 特约商户

            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

            // 服务商相关参数
            result.setSpAppid(wxPayService.getConfig().getAppId());
            result.setSpMchid(wxPayService.getConfig().getMchId());
            result.setSubMchid(isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                result.setSubAppid(isvsubMchParams.getSubMchAppId());
            }

        }else { // 普通商户

            result.setNormalMchid(wxPayService.getConfig().getMchId());
            result.setNormalAppid(wxPayService.getConfig().getAppId());

        }

        return result;
    }


}
