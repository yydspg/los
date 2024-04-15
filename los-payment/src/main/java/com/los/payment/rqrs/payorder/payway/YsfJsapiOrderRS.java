
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.core.utils.JSONKit;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： YSF_JSAPI
 *
 */
@Data
public class YsfJsapiOrderRS extends UnifiedOrderRS {

    /* 调起支付插件的云闪付订单号 **/
    private String redirectUrl;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData(){
        return JSONKit.convert("redirectUrl", redirectUrl).toString();
    }

}
