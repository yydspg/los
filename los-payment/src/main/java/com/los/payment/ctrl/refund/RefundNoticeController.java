package com.los.payment.ctrl.refund;


import com.los.payment.ctrl.AbstractCtrl;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.service.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author paul 2024/3/14
 */
@Slf4j
@RestController
public class RefundNoticeController extends AbstractCtrl {
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private OrderProcessService orderProcessService;



}
