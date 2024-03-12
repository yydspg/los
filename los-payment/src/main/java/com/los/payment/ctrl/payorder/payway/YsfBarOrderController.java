package com.los.payment.ctrl.payorder.payway;

import com.los.core.constants.CS;
import com.los.core.ctrls.AbstractCtrl;
import com.los.core.model.ApiRes;
import com.los.payment.ctrl.payorder.AbstractPayOrderController;
import com.los.payment.rqrs.payorder.payway.YsfBarOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @author paul 2024/2/21
 */
@Slf4j
@RestController
public class YsfBarOrderController extends AbstractPayOrderController {


    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/ysfBarOrder")
    public ApiRes aliBarOrder(){

        //获取参数 & 验证
        YsfBarOrderRQ bizRQ = super.getRQByMchSign(YsfBarOrderRQ.class);

        //   调起聚合支付接口
        return super.unifiedOrder(CS.PAY_WAY_CODE.YSF_BAR, bizRQ);

    }


}