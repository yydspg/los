
package com.los.core.model.params.xxpay;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.model.params.NormalMchParams;
import com.los.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 小新支付 普通商户参数定义
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/9/10 21:51
 */
@Data
public class XxpayNormalMchParams extends NormalMchParams {

    /* 商户号 */
    private String mchId;

    /* 私钥 */
    private String key;

    /* 支付网关地址 */
    private String payUrl;

    @Override
    public String deSenData() {
        XxpayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.key)) {
            mchParams.setKey(StringKit.str2Star(this.key, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
