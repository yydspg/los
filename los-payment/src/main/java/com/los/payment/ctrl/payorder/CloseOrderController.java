package com.los.payment.ctrl.payorder;


import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.IPayOrderCloseService;
import com.los.payment.ctrl.ApiController;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.ClosePayOrderRQ;
import com.los.payment.rqrs.payorder.ClosePayOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/12
 */
@Slf4j
@RestController
public class CloseOrderController extends ApiController {
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    // 关闭订单
    @RequestMapping("/api/pay/close")
    public ApiRes closeOrder(){

        // 获取请求 && 验签
        ClosePayOrderRQ rq = getRQByMchSign(ClosePayOrderRQ.class);

        if(StringKit.isAnyBlank(rq.getMchOrderNo(),rq.getMchOrderNo())) {
            throw new BizException("mchOrderNoOrMchOrderNoIsEmpty");
        }

        PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());

        if(payOrder == null) {
            throw new BizException("PayOrderNoExists");
        }
        byte state = payOrder.getState();
        if(state != PayOrder.STATE_ING && state!= PayOrder.STATE_INIT) {
            throw new BizException("PayOrderCanNotBeClosed");
        }
        ClosePayOrderRS closePayOrderRS = new ClosePayOrderRS();

        //初始化 --> 关闭

        String payOrderId = payOrder.getPayOrderId();
        if(state == PayOrder.STATE_INIT) {
            payOrderService.updateIng2Close(payOrderId);
            closePayOrderRS.setChannelRetMsg(ChannelRetMsg.confirmSuccess(payOrder.getChannelOrderNo()));
            return ApiRes.successWithSign(closePayOrderRS,configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
        }

        try {

            // 查询退款接口是否存在
            IPayOrderCloseService closeService = SpringBeansKit.getBean(payOrder.getIfCode() + "PayOrderCloseService", IPayOrderCloseService.class);

            if (closeService == null) {
                log.error("[{}]closeInterfaceNoExists",payOrder.getIfCode());
                throw new BizException("closeServiceError");
            }

            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

            ChannelRetMsg channelRetMsg = closeService.close(payOrder, mchAppConfigContext);

            if (channelRetMsg == null) {
                throw new BizException("NoChannelRetMsg");
            }
            log.info("[{}]closeRes:[{}]",payOrderId,channelRetMsg);

            //关闭订单成功

            if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                payOrderService.updateIng2Close(payOrderId);
            } else {
                return ApiRes.customFail(channelRetMsg.getChannelErrMsg());
            }
            closePayOrderRS.setChannelRetMsg(channelRetMsg);
        } catch (Exception e) {
            log.error("[{}]closeError:[{}]",payOrderId,e.getMessage());
            return ApiRes.customFail(e.getMessage());
        }
        return ApiRes.successWithSign(closePayOrderRS,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
    }
}
