
package com.los.core.model.params.alipay;


import com.los.core.model.params.IsvsubMchParams;
import lombok.Data;

/*
 * 支付宝 特约商户参数定义
 *
 * @author paul 2024/1/31
 */
@Data
public class AlipayIsvsubMchParams  extends IsvsubMchParams {

    private String appAuthToken;


}
