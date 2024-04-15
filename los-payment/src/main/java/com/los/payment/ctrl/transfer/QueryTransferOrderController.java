package com.los.payment.ctrl.transfer;

import com.los.core.entity.TransferOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.payment.ctrl.ApiController;
import com.los.payment.rqrs.transfer.QueryTransferOrderRQ;
import com.los.payment.rqrs.transfer.QueryTransferOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.TransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/13
 */
@RestController
public class QueryTransferOrderController extends ApiController {

    @Autowired private TransferOrderService transferOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;



    @RequestMapping(value = "/api/transfer/query",method = RequestMethod.POST)
    public ApiRes queryTransferOrder() {

        QueryTransferOrderRQ rq = super.getRQByMchSign(QueryTransferOrderRQ.class);

        if(StringKit.isAllEmpty(rq.getMchOrderNo(),rq.getTransferId())) {
            throw new BizException("mchOrder&&transferOrderIdMayBeNull");
        }
        TransferOrder transferOrder = transferOrderService.queryMchOrder(rq.getMchNo(), rq.getMchOrderNo(), rq.getTransferId());

        if (transferOrder == null) {
            throw new BizException("TransferOrderNoExists");
        }
        QueryTransferOrderRS res = QueryTransferOrderRS.buildByRecord(transferOrder);
        return ApiRes.successWithSign(res,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
    }
}
