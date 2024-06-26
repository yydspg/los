package com.los.payment.ctrl.payorder.payway;

import com.los.core.constants.CS;
import com.los.core.ctrls.AbstractCtrl;
import com.los.core.model.ApiRes;
import com.los.payment.ctrl.payorder.AbstractPayOrderController;
import com.los.payment.rqrs.payorder.payway.AliBarOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AliBarOrderController extends AbstractPayOrderController {

    @RequestMapping(value = "/api/pay/aliBarOrder",method = RequestMethod.POST)
    public ApiRes aliBarOrder(){

        //获取参数 & 验证
        AliBarOrderRQ bizRQ = super.getRQByMchSign(AliBarOrderRQ.class);

        //   调起聚合支付接口
        return super.unifiedOrder(CS.PAY_WAY_CODE.ALI_BAR, bizRQ);

    }


}
