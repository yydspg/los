package com.los.payment.ctrl.transfer;

import com.los.core.model.ApiRes;
import com.los.payment.ctrl.ApiController;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.payment.service.PayMchNotifyService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/14
 */
@Slf4j
@RestController
public class TransferOrderController extends ApiController {
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private OrderProcessService orderProcessService;

    // 转账接口
    @PostMapping("/api/transferOrder")
    public ApiRes transferOrder(){
            return null;
    }
}
