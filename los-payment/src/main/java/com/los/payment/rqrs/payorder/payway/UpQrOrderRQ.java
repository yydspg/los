
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： UPACP_QR
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/12/1 19:57
 */
@Data
public class UpQrOrderRQ extends CommonPayDataRQ {

    /* 构造函数 **/
    public UpQrOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.UP_QR);
    }

}
