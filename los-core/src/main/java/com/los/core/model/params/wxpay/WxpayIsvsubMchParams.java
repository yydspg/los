
package com.los.core.model.params.wxpay;

import com.los.core.model.params.IsvsubMchParams;
import lombok.Data;

/*
 * 微信官方支付 配置参数
 *
 * @author paul 2024/1/31
 */
@Data
public class WxpayIsvsubMchParams extends IsvsubMchParams {

    /* 子商户ID **/
    private String subMchId;

    /* 子账户appID **/
    private String subMchAppId;


}
