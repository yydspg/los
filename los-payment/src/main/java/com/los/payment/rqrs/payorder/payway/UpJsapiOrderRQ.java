
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
 * 支付方式： UP_JSAPI
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class UpJsapiOrderRQ extends UnifiedOrderRQ {

    /* 支付宝用户ID **/
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /* 构造函数 **/
    public UpJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.UP_JSAPI);
    }

    @Override
    public String getChannelUserId(){
        return this.userId;
    }

}
