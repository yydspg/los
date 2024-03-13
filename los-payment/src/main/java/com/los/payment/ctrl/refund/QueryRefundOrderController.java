package com.los.payment.ctrl.refund;

import com.los.core.entity.RefundOrder;
import com.los.core.entity.TransferOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.payment.ctrl.ApiController;
import com.los.payment.rqrs.refund.QueryRefundOrderRQ;
import com.los.payment.rqrs.refund.QueryRefundOrderRS;
import com.los.payment.rqrs.transfer.QueryTransferOrderRQ;
import com.los.payment.rqrs.transfer.QueryTransferOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/13
 */
@RestController
public class QueryRefundOrderController extends ApiController {
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    // 退款查询接口
    @RequestMapping("/api/refund/query")
    public ApiRes queryTransferOrder() {
        // 获取请求request,并对其参数校验
        QueryRefundOrderRQ rq = super.getRQByMchSign(QueryRefundOrderRQ.class);

        if(StringKit.isAllEmpty(rq.getMchRefundNo(),rq.getRefundOrderId())) {
            throw new BizException("mchOrder&&refundOrderIdMayBeNull");
        }
        RefundOrder refundOrder = refundOrderService.queryMchOrder(rq.getMchNo(), rq.getMchRefundNo(),rq.getRefundOrderId());

        if (refundOrder == null) {
            throw new BizException("RefundOrderNoExists");
        }
        QueryRefundOrderRS res = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        return ApiRes.successWithSign(res,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
    }
}
