package com.los.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.SysConfig;
import com.los.core.model.DBApplicationConfig;
import com.los.core.service.ISysConfigService;
import com.los.service.mapper.SysConfigMapper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/*
* <p>
 * 系统配置表 服务实现类
 * </p>
* @author paul
* @since 2024-02-05
*/
/*
使用MutablePair,同时封装分组名和数据库配置
 TODO 以后可以尝试使用caffine进行本地缓存
 */
@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    /* 是否启用缓存
      true: 表示将使用内存缓存， 将部分系统配置项 或 商户应用/服务商信息进行缓存并读取
      false: 直接查询DB
     */
    //TODO 是否可以将所有配置项可以在线修改
    public static boolean IS_USE_CACHE = false;


    /*
    DB 配置参数
    MutablePair 是一种数据结构，通常用作存储一对可变数据的容器。
    这个概念来源于Apache Commons Lang库中的org.apache.commons.lang3.tuple.MutablePair类。
    顾名思义，MutablePair允许用户在其生命周期内更改其包含的两个元素值。
    MutablePair 类有两个泛型参数，比如 <T, U>，表示它可以存储两种不同类型的数据作为一对
     */
    private static MutablePair<String,DBApplicationConfig> APPLICATION_CONFIG = new MutablePair<>("applicationConfig",null);

    /*
    加锁初始化
     */
    public synchronized void initDBConfig(String groupKey) {
        //当前系统不缓存,直接返回
        if(!IS_USE_CACHE) {
            return;
        }
        if(APPLICATION_CONFIG.getLeft().equalsIgnoreCase(groupKey)) {
            APPLICATION_CONFIG.right = this.selectByGroupKey(groupKey).toJavaObject(DBApplicationConfig.class);
        }
    }

    /*
    按照组别查找,返回JsonObject
     */
    public JSONObject selectByGroupKey(String groupKey) {
        JSONObject res = new JSONObject();
        this.list(SysConfig.gw().select(SysConfig::getConfigKey,SysConfig::getConfigVal).eq(SysConfig::getGroupKey,groupKey))
                .forEach(t->{res.put(t.getConfigKey(),t.getConfigVal());});
        return res;
    }
    /*
    更新配置,返回更新正确的结果
     */
    public int updateByConfigKey(Map<String,String> updateMap) {
        int count = 0;
        Set<String> set = updateMap.keySet();
        for (String k : set) {
            if(k != null && !k.isEmpty()) {
                SysConfig sysConfig = new SysConfig();
                sysConfig.setConfigKey(k);
                sysConfig.setConfigVal(updateMap.get(k));
                boolean update = this.saveOrUpdate(sysConfig);
                if(update){ count++;}
            }
        }
        return count;
    }
    /*
    获取DB 数据
     */
    @Override
    public DBApplicationConfig getDBApplicationConfig() {
        //未使用缓存,查询DB
        if (!IS_USE_CACHE) {
            return this.selectByGroupKey(APPLICATION_CONFIG.getLeft()).toJavaObject(DBApplicationConfig.class);
        }
        //使用缓存
        if (null == APPLICATION_CONFIG.getRight()) {
            initDBConfig(APPLICATION_CONFIG.getLeft());
        }
        return APPLICATION_CONFIG.getRight();
    }
}
