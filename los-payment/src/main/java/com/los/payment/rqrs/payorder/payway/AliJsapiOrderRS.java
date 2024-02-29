
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.core.utils.JSONKit;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： ALI_JSAPI
 *

 */
@Data
public class AliJsapiOrderRS extends UnifiedOrderRS {

    /* 调起支付插件的支付宝订单号 **/
    private String alipayTradeNo;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData(){
        return JSONKit.newJsonObject("alipayTradeNo", alipayTradeNo).toString();
    }

}
