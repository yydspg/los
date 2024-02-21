package com.los.payment.model;

import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商户配置信息,存储于内存
 * @author paul 2024/2/8
 */
@Data
public class MchInfoConfigContext {
    /** 商户信息缓存 */
    private String mchNo;
    private Byte mchType;
    private MchInfo mchInfo;
    private Map<String, MchApp> appMap = new ConcurrentHashMap<>();

    /** 重置商户APP **/
    public void putMchApp(MchApp mchApp){
        appMap.put(mchApp.getAppId(), mchApp);
    }

    /** get商户APP **/
    public MchApp getMchApp(String appId){
        return appMap.get(appId);
    }
}
