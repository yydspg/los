package com.los.payment.model;

import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 商户配置信息,存储于内存
 * @author paul 2024/2/8
 */
@Data
public class MchInfoConfigContext {

    /* 商户基本信息缓存 */

    private String mchNo;
    private Byte mchType;
    private MchInfo mchInfo;

    /* 商户所属应用缓存 */
    private Map<String, MchApp> appMap = new ConcurrentHashMap<>();

    /* 添加商户应用 */
    public void putMchApp(MchApp mchApp){
        appMap.put(mchApp.getAppId(), mchApp);
    }

    /* 获取商户application */
    public MchApp getOneMchAppByAppId(String appId){
        return appMap.get(appId);
    }
    /* 删除所有应用,一般为商户被删除时操作*/
    public void deleteAllMchApp() {
        appMap.clear();
    }
    /* 根据appId删除应用 */
    public void deleteMchApp(String appId)  {
        appMap.remove(appId);
    }
}
