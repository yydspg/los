package com.los.payment.ctrl.payorder;

import com.los.core.ctrls.AbstractCtrl;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.PayMchNotifyService;
import com.los.payment.service.PayOrderProcessService;
import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * 渠道侧 支付结果通知, 同步跳转 doReturn && 异步回调 doNotify
 * @author paul 2024/3/12
 */
@Slf4j
@Controller
public class ChannelNoticeController extends AbstractCtrl {
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private PayOrderProcessService payOrderProcessService;


}
