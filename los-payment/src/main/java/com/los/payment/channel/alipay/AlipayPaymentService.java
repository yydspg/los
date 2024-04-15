
package com.los.payment.channel.alipay;

import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.payment.channel.AbstractPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.utils.PaymentKit;
import org.springframework.stereotype.Service;

/*
* 支付接口： 支付宝官方
* 支付方式： 自适应
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:19
*/
@Service
public class AlipayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaymentKit.getRealPayWayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaymentKit.getRealPayWayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
    }

}
