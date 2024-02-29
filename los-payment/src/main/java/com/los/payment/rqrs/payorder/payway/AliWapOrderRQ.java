
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： ALI_WAP
 *

 */
@Data
public class AliWapOrderRQ extends CommonPayDataRQ {

    /* 构造函数 **/
    public AliWapOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_WAP); //默认 ALI_WAP, 避免validate出现问题
    }

}
