package com.los.payment.ctrl.refund;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.SummaryBillOpenApiDTO;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.SeqKit;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.IRefundService;
import com.los.payment.ctrl.ApiController;
import com.los.payment.exception.ChannelException;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.refund.RefundOrderRQ;
import com.los.payment.rqrs.refund.RefundOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.payment.service.PayMchNotifyService;
import com.los.service.PayOrderService;
import com.los.service.RefundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author paul 2024/3/14
 */
@Slf4j
@RestController
public class RefundOrderController extends ApiController {
    @Resource private PayOrderService payOrderService;
    @Resource private RefundOrderService refundOrderService;
    @Resource private ConfigContextQueryService configContextQueryService;
    @Resource private OrderProcessService orderProcessService;


}
