
package com.los.core.model.params;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

/*
 * 抽象类 普通商户参数定义
 *
 */
@Slf4j
public abstract class NormalMchParams {
    // TODO 2024/3/31 : 此方法的作用

    public static NormalMchParams factory(String ifCode, String paramsStr) {

        try {
            return (NormalMchParams)JSONObject.parseObject(paramsStr, Class.forName(NormalMchParams.class.getPackage().getName() +"."+ ifCode +"."+ StrUtil.upperFirst(ifCode) +"NormalMchParams"));
        } catch (ClassNotFoundException e) {
           log.error("[{}]",e.getMessage());
        }
        return null;
    }

    /*
     *  敏感数据脱敏
     */
    public abstract String deSenData();

}
