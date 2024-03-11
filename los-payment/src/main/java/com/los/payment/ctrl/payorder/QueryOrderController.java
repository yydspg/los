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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/12
 */

@Slf4j
@RestController
public class QueryOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    // 查单接口
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