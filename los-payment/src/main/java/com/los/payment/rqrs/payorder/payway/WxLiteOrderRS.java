
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： WX_LITE
 *

 */
@Data
public class WxLiteOrderRS extends UnifiedOrderRS {

    /* 预支付数据包 **/
    private String payInfo;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.WX_APP;
    }

    @Override
    public String buildPayData(){
        return payInfo;
    }

}
