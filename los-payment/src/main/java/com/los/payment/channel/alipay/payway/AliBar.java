
package com.los.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.utils.AmountKit;
import com.los.payment.channel.alipay.AlipayKit;
import com.los.payment.channel.alipay.AlipayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliBarOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliBarOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 支付宝 条码支付
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:20
 */
@Service("alipayPaymentByAliBarService") //Service Name需保持全局唯一性
public class AliBar extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext){

        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;

        AlipayTradePayRequest req = new AlipayTradePayRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setScene("bar_code"); //条码支付 bar_code ; 声波支付 wave_code
        model.setAuthCode(bizRQ.getAuthCode().trim()); //支付授权码
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountKit.convertCent2Dollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        //调起支付宝 （如果异常， 将直接跑出   ChannelException ）
        AlipayTradePayResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 构造函数响应数据
        AliBarOrderRS res = AbstractRS.build(AliBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(alipayResp.getBody());
        channelRetMsg.setChannelOrderId(alipayResp.getTradeNo());
        channelRetMsg.setChannelUserId(alipayResp.getBuyerUserId()); //渠道用户标识

        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓

        //当条码重复发起时，支付宝返回的code = 10003, subCode = null [等待用户支付], 此时需要特殊判断 = = 。
        if("10000".equals(alipayResp.getCode()) && alipayResp.isSuccess()){ //支付成功, 更新订单成功 || 等待支付宝的异步回调接口

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);


        }else if("10003".equals(alipayResp.getCode())){ //10003 表示为 处理中, 例如等待用户输入密码

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        }else{  //其他状态, 表示下单失败

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
        }

        return res;

    }
}
