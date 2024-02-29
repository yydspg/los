
package com.los.core.model.params.wxpay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.model.params.IsvParams;
import com.los.core.utils.StringKit;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
* 微信官方支付 配置参数
*
* @author paul 2024/1/31
*/
@Data
public class WxpayIsvParams extends IsvParams {

    /* 应用App ID */
    private String appId;

    /* 应用AppSecret */
    private String appSecret;

    /* 微信支付商户号 */
    private String mchId;

    /* oauth2地址 */
    private String oauth2Url;

    /* API密钥 */
    private String key;

    /* 签名方式 **/
    private String signType;

    /* 微信支付API版本 **/
    private String apiVersion;

    /* API V3秘钥 **/
    private String apiV3Key;

    /* 序列号 **/
    private String serialNo;

    /* API证书(.p12格式)**/
    private String cert;

    /* 证书文件(.pem格式) **/
    private String apiClientCert;

    /* 私钥文件(.pem格式) **/
    private String apiClientKey;


    @Override
    public String deSenData() {

        WxpayIsvParams isvParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            isvParams.setAppSecret(StringKit.str2Star(this.appSecret, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.key)) {
            isvParams.setKey(StringKit.str2Star(this.key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.apiV3Key)) {
            isvParams.setApiV3Key(StringKit.str2Star(this.apiV3Key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.serialNo)) {
            isvParams.setSerialNo(StringKit.str2Star(this.serialNo, 4, 4, 6));
        }
        return ((JSONObject)JSON.toJSON(isvParams)).toJSONString();
    }
}
