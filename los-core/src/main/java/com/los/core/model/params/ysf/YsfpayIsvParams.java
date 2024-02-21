
package com.los.core.model.params.ysf;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.model.params.IsvParams;
import com.los.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 云闪付 配置信息
 *
 * @author paul 2024/1/30
 */
@Data
public class YsfpayIsvParams extends IsvParams {

    /** 是否沙箱环境 */
    private Byte sandbox;

    /** serProvId **/
    private String serProvId;

    /** isvPrivateCertFile 证书 **/
    private String isvPrivateCertFile;

    /** isvPrivateCertPwd **/
    private String isvPrivateCertPwd;

    /** ysfpayPublicKey **/
    private String ysfpayPublicKey;

    /** acqOrgCodeList 支付机构号 **/
    private String acqOrgCode;

    @Override
    public String deSenData() {

        YsfpayIsvParams isvParams = this;
        if (StringUtils.isNotBlank(this.isvPrivateCertPwd)) {
            isvParams.setIsvPrivateCertPwd(StringKit.str2Star(this.isvPrivateCertPwd, 0, 3, 6));
        }
        if (StringUtils.isNotBlank(this.ysfpayPublicKey)) {
            isvParams.setYsfpayPublicKey(StringKit.str2Star(this.ysfpayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }

}
