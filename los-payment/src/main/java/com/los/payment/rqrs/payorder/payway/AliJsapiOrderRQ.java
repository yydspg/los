
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： ALI_JSAPI
 *

 */
@Data
public class AliJsapiOrderRQ extends UnifiedOrderRQ {

    /* 支付宝用户ID **/
    @NotBlank(message = "用户ID不能为空")
    private String buyerUserId;

    /* 构造函数 **/
    public AliJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_JSAPI);
    }

    @Override
    public String getChannelUserId(){
        return this.buyerUserId;
    }

}
