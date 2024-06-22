package com.los.payment.ctrl.payorder;

import com.los.payment.ctrl.AbstractCtrl;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.PayMchNotifyService;
import com.los.payment.service.PayOrderProcessService;
import com.los.service.PayOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道侧 支付结果通知, 同步跳转 doReturn && 异步回调 doNotify
 * @author paul 2024/3/12
 */
@Slf4j
@RestController
@Tag(name = "支付")
public class ChannelNoticeController extends AbstractCtrl {
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private PayOrderProcessService payOrderProcessService;

    // 同步通知入口 方法

    public String deReturn(HttpServletRequest request,String ifCode,String urlOrderId) {
        return null;
    }



}
