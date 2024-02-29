package com.los.payment.service;

import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/2/28
 */
@Slf4j
@Service
public class ChannelOrderReissueService {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private RefundOrderProcessService refundOrderProcessService;


}
