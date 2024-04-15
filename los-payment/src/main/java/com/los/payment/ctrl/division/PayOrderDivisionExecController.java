package com.los.payment.ctrl.division;

import com.los.components.mq.vender.IMQSender;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.PayOrderProcessService;
import com.los.service.impl.SysConfigService;
import com.los.service.MchPayPassageService;
import com.los.service.PayOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/*
 * @author paul 2024/2/8
 */
@Controller
@Tag(name = "分账")
public class PayOrderDivisionExecController {
    @Autowired
    private MchPayPassageService mchPayPassageService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private IMQSender mqSender;
}
