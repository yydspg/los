package com.los.payment.model;

import com.los.core.entity.IsvInfo;
import com.los.core.model.params.IsvParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
 * Isv支付参数信息 放置到内存， 避免多次查询操作
 * ISV（Independent Software Vendor） 是独立软件开发商。这类企业不依赖于特定的平台提供商或硬件制造商，
 * 而是专注于开发、销售和维护自己的软件产品及解决方案。在与大型平台合作时，ISV也常扮演着为这些平台提供增值服务的角色
 * @author paul 2024/2/4
 */
@Data
public class IsvConfigContext {

    /* isv信息缓存 */
    private String isvNo;
    private IsvInfo isvInfo;

    /* 商户支付配置信息缓存 */
    private Map<String, IsvParams> isvParamsMap = new HashMap<>();


    /* 缓存支付宝client 对象 */
    private AlipayClientWrapper alipayClientWrapper;

    /* 缓存 wxServiceWrapper 对象 */
    private WxServiceWrapper wxServiceWrapper;


    /* 获取isv配置信息 */
    public IsvParams getIsvParamsByIfCode(String ifCode){
        return isvParamsMap.get(ifCode);
    }

    /* 获取isv配置信息 */
    public <T> T getIsvParamsByIfCode(String ifCode, Class<? extends IsvParams> cls){
        return (T)isvParamsMap.get(ifCode);
    }
}
