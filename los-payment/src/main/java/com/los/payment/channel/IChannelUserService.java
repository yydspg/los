package com.los.payment.channel;

import com.alibaba.fastjson2.JSONObject;
import com.los.payment.model.MchAppConfigContext;

/**
 * 301方式获取渠道侧用户ID， 如微信openId 支付宝的userId等
 * @author paul 2024/2/28
 */
/*
    是的，301确切地说就是HTTP协议中的301重定向状态码。
    在互联网和网站技术中，301 Moved Permanently（永久移动）是一个HTTP响应状态码，
    表示被请求的资源已被永久性地移至新的URL，而且未来的所有请求都应该使用新的URL来进行访问。当服务器返回301状态码时，
    还会在响应头部包含新的URL地址，浏览器或搜索引擎在接收到这个响应后，通常会自动将请求指向新的URL地址，从而达到网页或资源地址变更后的无缝迁移，同时有助于搜索引擎优化（SEO），因为它可以传递网页权重，确保旧链接的搜索引擎排名不会因为URL变化而丢失。
 */
public interface IChannelUserService {
    /** 获取到接口code **/
    String getIfCode();

    /** 获取重定向地址 **/
    String buildUserRedirectUrl(String callbackUrlEncode, MchAppConfigContext mchAppConfigContext);

    /** 获取渠道用户ID **/
    String getChannelUserId(JSONObject reqParams, MchAppConfigContext mchAppConfigContext);

}
