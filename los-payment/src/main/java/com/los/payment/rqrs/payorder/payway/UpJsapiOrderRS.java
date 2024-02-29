
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.core.utils.JSONKit;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： UP_JSAPI
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2022/3/17 12:34
 */
@Data
public class UpJsapiOrderRS extends UnifiedOrderRS {

    /* 调起支付插件的云闪付订单号 **/
    private String redirectUrl;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData(){
        return JSONKit.newJsonObject("redirectUrl", redirectUrl).toString();
    }

}
