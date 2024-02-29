
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： YSF_BAR
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class YsfBarOrderRQ extends UnifiedOrderRQ {

    /* 用户 支付条码 **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /* 构造函数 **/
    public YsfBarOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.YSF_BAR);
    }

}
