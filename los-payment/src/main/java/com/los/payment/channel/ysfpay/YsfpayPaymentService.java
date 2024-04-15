
package com.los.payment.channel.ysfpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.params.ysf.YsfpayConfig;
import com.los.core.model.params.ysf.YsfpayIsvParams;
import com.los.core.model.params.ysf.YsfpayIsvsubMchParams;
import com.los.payment.channel.AbstractPaymentService;
import com.los.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.los.payment.channel.ysfpay.utils.YsfSignUtils;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.utils.PaymentKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class YsfpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
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
        return PaymentKit.getRealPayWayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
    }


    /** 封装参数 & 统一请求 **/
    public JSONObject packageParamAndReq(String apiUri, JSONObject reqParams, String logPrefix, MchAppConfigContext mchAppConfigContext) throws Exception {

        YsfpayIsvParams isvParams = (YsfpayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());

        if (isvParams.getSerProvId() == null) {
            log.error("服务商配置为空：isvParams：{}", isvParams);
            throw new BizException("服务商配置为空。");
        }

        reqParams.put("serProvId", isvParams.getSerProvId()); //云闪付服务商标识
        YsfpayIsvsubMchParams isvsubMchParams = (YsfpayIsvsubMchParams) configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
        reqParams.put("merId", isvsubMchParams.getMerId()); // 商户号

        //签名
        String isvPrivateCertFile = channelCertConfigKitBean.getCertFilePath(isvParams.getIsvPrivateCertFile());
        String isvPrivateCertPwd = isvParams.getIsvPrivateCertPwd();
        reqParams.put("signature", YsfSignUtils.signBy256(reqParams, isvPrivateCertFile, isvPrivateCertPwd)); //RSA 签名串

        // 调起上游接口
        log.info("{} reqJSON={}", logPrefix, reqParams);
        String resText = YsfHttpUtil.doPostJson(getYsfpayHost4env(isvParams) + apiUri, null, reqParams);
        log.info("{} resJSON={}", logPrefix, resText);

        if(StringUtils.isEmpty(resText)){
            return null;
        }
        return JSONObject.parseObject(resText);
    }

    /** 获取云闪付正式环境/沙箱HOST地址   **/
    public static String getYsfpayHost4env(YsfpayIsvParams isvParams){
        return CS.YES == isvParams.getSandbox() ? YsfpayConfig.SANDBOX_SERVER_URL : YsfpayConfig.PROD_SERVER_URL;
    }

    /** 云闪付 jsapi下单请求统一发送参数 **/
    public static void jsapiParamsSet(JSONObject reqParams, PayOrder payOrder, String notifyUrl, String returnUrl) {
        String orderType = YsfHttpUtil.getOrderTypeByJSapi(payOrder.getWayCode());
        reqParams.put("orderType", orderType); //订单类型： alipayJs-支付宝， wechatJs-微信支付， upJs-银联二维码
        ysfPublicParams(reqParams, payOrder);
        reqParams.put("backUrl", notifyUrl); //交易通知地址
        reqParams.put("frontUrl", returnUrl); //前台通知地址
    }

    /** 云闪付 bar下单请求统一发送参数 **/
    public static void barParamsSet(JSONObject reqParams, PayOrder payOrder) {
        String orderType = YsfHttpUtil.getOrderTypeByBar(payOrder.getWayCode());
        reqParams.put("orderType", orderType); //订单类型： alipay-支付宝， wechat-微信支付， -unionpay银联二维码
        ysfPublicParams(reqParams, payOrder);
        // TODO 终端编号暂时写死
        reqParams.put("termId", "01727367"); // 终端编号
    }

    /** 云闪付公共参数赋值 **/
    public static void ysfPublicParams(JSONObject reqParams, PayOrder payOrder) {
        //获取订单类型
        reqParams.put("orderNo", payOrder.getPayOrderId()); //订单号
        reqParams.put("orderTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN)); //订单时间 如：20180702142900
        reqParams.put("txnAmt", payOrder.getAmount()); //交易金额 单位：分，不带小数点
        reqParams.put("currencyCode", "156"); //交易币种 不出现则默认为人民币-156
        reqParams.put("orderInfo", payOrder.getSubject()); //订单信息 订单描述信息，如：京东生鲜食品
    }
}
