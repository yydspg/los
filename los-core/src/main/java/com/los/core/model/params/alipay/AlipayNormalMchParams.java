
package com.los.core.model.params.alipay;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.model.params.NormalMchParams;
import com.los.core.utils.StringKit;
import lombok.Data;


/*
 * 支付宝 普通商户参数定义
 *
 */
@Data
public class AlipayNormalMchParams extends NormalMchParams {

    /* 是否沙箱环境 */
    private Byte sandbox;

    /* appId */
    private String appId;

    /* privateKey */
    private String privateKey;

    /* alipayPublicKey */
    private String alipayPublicKey;

    /* 签名方式 **/
    private String signType;

    /* 是否使用证书方式 **/
    private Byte useCert;

    /* app 证书 **/
    private String appPublicCert;

    /* 支付宝公钥证书（.crt格式） **/
    private String alipayPublicCert;

    /* 支付宝根证书 **/
    private String alipayRootCert;

    @Override
    public String deSenData() {

        AlipayNormalMchParams mchParams = this;
        if (StringKit.isNotBlank(this.privateKey)) {
            mchParams.setPrivateKey(StringKit.str2Star(this.privateKey, 4, 4, 6));
        }
        if (StringKit.isNotBlank(this.alipayPublicKey)) {
            mchParams.setAlipayPublicKey(StringKit.str2Star(this.alipayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
