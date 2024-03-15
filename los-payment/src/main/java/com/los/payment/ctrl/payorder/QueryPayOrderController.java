package com.los.payment.ctrl.payorder;

import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.payment.ctrl.ApiController;
import com.los.payment.rqrs.payorder.QueryPayOrderRQ;
import com.los.payment.rqrs.payorder.QueryPayOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/12
 */

@Slf4j
@RestController
@Tag(name = "支付")
public class QueryPayOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    @Operation(summary = "查询支付")
    @PostMapping("/api/pay/query")
    public ApiRes queryOrder() {
        QueryPayOrderRQ rq = super.getRQByMchSign(QueryPayOrderRQ.class);
        if(StringKit.isAllEmpty(rq.getMchOrderNo(),rq.getPayOrderId())) {
            throw new BizException("MchOrderNoOrMchPayOrderIdIsEmpty");
        }
        PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());

        if(payOrder == null) {
            return ApiRes.customFail("PayOrderNoExists");
        }
        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        return ApiRes.successWithSign(queryPayOrderRS,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
    }
}