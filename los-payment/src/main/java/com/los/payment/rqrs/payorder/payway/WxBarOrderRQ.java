
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： WX_BAR
 *

 */
@Data
public class WxBarOrderRQ extends UnifiedOrderRQ {

    /* 用户 支付条码 **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /* 构造函数 **/
    public WxBarOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.WX_BAR); //默认 wx_bar, 避免validate出现问题
    }

}
