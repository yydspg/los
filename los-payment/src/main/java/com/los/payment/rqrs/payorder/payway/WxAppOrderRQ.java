
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
 *  支付方式： WX_APP
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/12/20 8:12
 */
@Data
public class WxAppOrderRQ extends UnifiedOrderRQ {

    /* 微信openid **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /* 构造函数 **/
    public WxAppOrderRQ(){
        this.setWayCode(CS.PAY_DATA_TYPE.WX_APP); //默认 wayCode, 避免validate出现问题
    }


    @Override
    public String getChannelUserId(){
        return this.openid;
    }
}
