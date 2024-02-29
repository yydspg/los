
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

/*
 * 支付方式： AUTO_BAR
 *

 */
@Data
public class AutoBarOrderRQ extends UnifiedOrderRQ {

    /* 条码值 **/
    private String authCode;

    /* 构造函数 **/
    public AutoBarOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.AUTO_BAR);
    }

}
