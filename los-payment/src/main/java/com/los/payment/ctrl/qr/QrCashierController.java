package com.los.payment.ctrl.qr;

import com.los.payment.ctrl.payorder.AbstractPayOrderController;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.PayMchNotifyService;
import com.los.service.PayOrderService;
import com.los.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聚合支付
 * @author paul 2024/3/14
 */
@RestController
@RequestMapping("/api/cashier")
@Tag(name = "二维码")
public class QrCashierController extends AbstractPayOrderController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private PayMchNotifyService payMchNotifyService;


}
