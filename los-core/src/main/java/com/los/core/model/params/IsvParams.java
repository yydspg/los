
package com.los.core.model.params;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象类 isv参数定义
 *TODO 理解类含义
 */
/*
在后端开发中，ISV（Independent Software Vendor，独立软件开发商）相关的表设计通常与管理ISV合作伙伴、认证信息、授权情况以及相关服务有关。由于每个SaaS平台或API服务提供商的具体业务和合作模式不同，ISV相关的表结构会有所差异，但通常可能包含以下字段：

ISV基本信息表：

isv_id：ISV的唯一标识符
name：ISV名称
contact_info：联系人信息（如电子邮件、电话等）
registration_date：注册日期
status：状态（如已认证、待审核、已禁用等）
description：描述或简介
website：官方网站地址
 */
@Slf4j
public abstract class IsvParams {
    /*
   方法内部逻辑：
     - 使用 `JSONObject.parseObject()` 尝试将 `paramsStr` 字符串解析为对应类的实例。通过 `Class.forName()` 动态加载类，类名是根据 `ifCode` 和包名拼接而成的，格式为 "包名.接口码.首字母大写的接口码+IsvParams"。
     - 如果能找到并成功创建相应类的实例，则返回该实例；否则，如果发生 `ClassNotFoundException`，打印堆栈跟踪信息，并返回 `null`。
综上所述，这个抽象类的设计意图是在后端处理ISV提供的参数时提供统一的入口点，并要求所有的ISV参数实现敏感数据脱敏的处理逻辑。同时，通过工厂方法可以根据不同的接口码动态地创建对应的参数对象实例。
     */

    public static IsvParams factory(String ifCode, String paramsStr){

        try {
            return (IsvParams)JSONObject.parseObject(paramsStr, Class.forName(IsvParams.class.getPackage().getName() +"."+ ifCode +"."+ StrUtil.upperFirst(ifCode) +"IsvParams"));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     *  敏感数据脱敏
    */
    public abstract String deSenData();

}
