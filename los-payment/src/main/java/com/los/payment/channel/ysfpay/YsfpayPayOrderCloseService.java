
package com.los.payment.channel.ysfpay;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.payment.channel.IPayOrderCloseService;
import com.los.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class YsfpayPayOrderCloseService implements IPayOrderCloseService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")关闭订单】";

        try {
            reqParams.put("orderNo", payOrder.getPayOrderId()); //订单号
            reqParams.put("orderType", orderType); //订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/closeOrder", reqParams, logPrefix, mchAppConfigContext);
            log.info("关闭订单 payorderId:{}, 返回结果:{}", payOrder.getPayOrderId(), resJSON);
            if(resJSON == null){
                return ChannelRetMsg.sysError("【云闪付】请求关闭订单异常");
            }

            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            if(("00").equals(respCode)){// 请求成功
                return ChannelRetMsg.confirmSuccess(null);  //关单成功
            }
            return ChannelRetMsg.sysError(respMsg); // 关单失败
        }catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage()); // 关单失败
        }
    }

}
