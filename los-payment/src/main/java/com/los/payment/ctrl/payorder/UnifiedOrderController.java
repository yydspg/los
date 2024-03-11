package com.los.payment.ctrl.payorder;

import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayWay;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.BeanKit;
import com.los.core.utils.SecKit;
import com.los.core.utils.SeqKit;
import com.los.payment.ctrl.ApiController;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import com.los.payment.rqrs.payorder.payway.AutoBarOrderRQ;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.PayWayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聚合支付统一返回
 * @author paul 2024/3/12
 */

@Slf4j
@RestController
public class UnifiedOrderController extends AbstractPayOrderController {

    @Autowired private PayWayService payWayService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    @PostMapping("/api/pay/unifiedOrder")
    public ApiRes unifiedOrder() {
        UnifiedOrderRQ rq = super.getRQByMchSign(UnifiedOrderRQ.class);
        UnifiedOrderRQ bizRQ = this.buildBizRQ(rq);

        ApiRes apiRes = super.unifiedOrder(bizRQ.getWayCode(), bizRQ);
        if(apiRes.getData() == null) {
            return apiRes;
        }
        UnifiedOrderRS bizRes = (UnifiedOrderRS) apiRes.getData();

        UnifiedOrderRS res = new UnifiedOrderRS();
        BeanKit.copyProperties(bizRes,res);
        //只有 订单生成（QR_CASHIER） || 支付中 || 支付成功返回该数据
        if(bizRes.getOrderState() != null && (bizRes.getOrderState() == PayOrder.STATE_INIT || bizRes.getOrderState() == PayOrder.STATE_ING || bizRes.getOrderState() == PayOrder.STATE_SUCCESS) ){
            res.setPayDataType(bizRes.buildPayDataType());
            res.setPayData(bizRes.buildPayData());
        }

        return ApiRes.successWithSign(res, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }
    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq) {
        String wayCode = rq.getWayCode();

        // 若为 收银台聚合支付 (不校验是否存在 payWayCode )
        if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)) {
            return rq.buildBizRQ();
        }

        // 若为自动分类条码

        if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)) {
            AutoBarOrderRQ autoBarOrderRQ = (AutoBarOrderRQ) rq.buildBizRQ();
            wayCode = SecKit.getPayWayCodeByBarCode(autoBarOrderRQ.getAuthCode());
            rq.setWayCode(wayCode.trim());
        }
        if(payWayService.count(PayWay.gw().eq(PayWay::getWayCode,wayCode)) <= 0 ) {
            throw new BizException("UnSupportPayWay");
        }

        // 转化为 bizRq
        return rq.buildBizRQ();
    }
}
