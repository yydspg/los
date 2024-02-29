
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： UPACP_BAR
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/12/1 19:57
 */
@Data
public class UpBarOrderRQ extends CommonPayDataRQ {

    /* 用户 支付条码 **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /* 构造函数 **/
    public UpBarOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.UP_BAR);
    }

}
