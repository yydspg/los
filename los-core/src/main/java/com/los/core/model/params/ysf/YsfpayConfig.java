
package com.los.core.model.params.ysf;

import lombok.Data;

/*
 * 云闪付 通用配置信息
 *
 * @author paul 2024/1/30
 */
@Data
public class YsfpayConfig {


    /** 网关地址 */
    public static String PROD_SERVER_URL = "https://partner.95516.com";
    public static String SANDBOX_SERVER_URL = "http://ysf.bcbip.cn:10240";

}
