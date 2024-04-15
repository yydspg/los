
package com.los.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.utils.AmountKit;
import com.los.payment.channel.alipay.AlipayKit;
import com.los.payment.channel.alipay.AlipayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliQrOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliQrOrderRS;
import org.springframework.stereotype.Service;

/*
* 支付宝 QR支付
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:21
*/
@Service("alipayPaymentByAliQrService") //Service Name需保持全局唯一性
public class AliQr extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext){

        AliQrOrderRQ aliQrOrderRQ = (AliQrOrderRQ)rq;

        AlipayTradePrecreateRequest req = new AlipayTradePrecreateRequest();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountKit.convertCent2Dollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        //调起支付宝 （如果异常， 将直接跑出   ChannelException ）
        AlipayTradePrecreateResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 构造函数响应数据
        AliQrOrderRS res = AbstractRS.build(AliQrOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(alipayResp.getBody());

        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓

        if(alipayResp.isSuccess()){ //处理成功

            if(CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(aliQrOrderRQ.getPayDataType())){ //二维码地址
                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(alipayResp.getQrCode()));

            }else{ //默认都为跳转地址方式
                res.setCodeUrl(alipayResp.getQrCode());
            }

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        }else{  //其他状态, 表示下单失败
            res.setOrderState(PayOrder.STATE_FAIL);  //支付失败
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
        }

        return res;
    }

}
