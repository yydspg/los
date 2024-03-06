
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
 * 支付方式： WX_LITE
 *

 */
@Data
public class WxLiteOrderRQ extends UnifiedOrderRQ {

    /* 微信openid **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /* 标志是否为 subMchLiteAppId的对应 openId， 0-否， 1-是， 默认否  **/
    private Byte isSubOpenId;

    /* 构造函数 **/
    public WxLiteOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.WX_LITE);
    }

    @Override
    public String getChannelUserId() {
        return this.openid;
    }
}
