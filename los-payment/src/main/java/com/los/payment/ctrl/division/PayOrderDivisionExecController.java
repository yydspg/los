package com.los.payment.ctrl.division;

import com.los.components.mq.vender.IMQSender;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.impl.SysConfigService;
import com.los.service.MchPayPassageService;
import com.los.service.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * @author paul 2024/2/8
 */

public class PayOrderDivisionExecController {
    @Autowired
    private MchPayPassageService mchPayPassageService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    //TODO 暂时注释防止报错
//    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private IMQSender mqSender;
}
