
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

/*
 * 支付方式： YSF_JSAPI
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class YsfJsapiOrderRQ extends UnifiedOrderRQ {

    /* 构造函数 **/
    public YsfJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.YSF_JSAPI);
    }

}
