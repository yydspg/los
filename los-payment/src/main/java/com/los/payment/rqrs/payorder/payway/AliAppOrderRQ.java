
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
* 支付方式： ALI_APP
*
*/
@Data
public class AliAppOrderRQ extends UnifiedOrderRQ {

    /* 支付宝用户ID **/
    @NotBlank(message = "用户ID不能为空")
    private String buyerUserId;

    /* 构造函数 **/
    public AliAppOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.ALI_APP); //默认 wayCode, 避免validate出现问题
    }

    @Override
    public String getChannelUserId(){
        return this.buyerUserId;
    }
}
