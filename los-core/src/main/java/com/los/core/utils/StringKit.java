package com.los.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson2.JSONObject;
import io.micrometer.common.util.StringUtils;

import java.util.Map;
import java.util.UUID;

/*
 * @author paul 2024/1/31
 */

public class StringKit  extends StrUtil {
    /* 获取uuid **/
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();
    }

    public static String getUUID(int endAt){
        return getUUID().substring(0, endAt);
    }
    //TODO 未测试
    /* 拼接url参数 **/
    public static String appendUrlQuery(String url, Map<String,Object> map) {
        if (StringUtils.isEmpty(url) || map == null || map.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        /* 是否包含查询符号? **/
        if (!url.contains("?")) {
            sb.append("?");
        }
        /* 是否包含查询参数组 = **/
        if (url.contains("=")) {
            sb.append("&");
        }
        for (String s : map.keySet()) {
            if (s != null && map.get(s) != null) {
                sb.append(s).append("=").append(URLUtil.encodeQuery(map.get(s).toString())).append("&");
            }
        }
        /* 删除多加入的 & **/
        if("&".contentEquals(String.valueOf(sb.charAt(sb.length()-1)))) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }
    /* 是否 http 或 https连接 **/
    public static boolean isAvailableUrl(String url){

        if(org.apache.commons.lang3.StringUtils.isEmpty(url)){
            return false;
        }

        return url.startsWith("http://") ||url.startsWith("https://");
    }

    /*
     * TODO 未测试
     * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
     * @param content 传入的字符串
     * @param frontN 保留前面字符的位数
     * @param endN 保留后面字符的位数
     * @param starN 指定star的数量
     * @return 带星号的字符串
     */
    public static String str2Star(String content,int frontN,int endN,int starN){
        if(frontN >= content.length() || frontN < 0) {
            return content;
        }
        if(endN >= content.length() || endN < 0 ) {
            return content;
        }
        if(frontN + endN >= content.length()) {
            return content;
        }
        StringBuilder sb = new StringBuilder(frontN + starN + endN);
        sb.append(content.substring(0,frontN-1));
        //TODO jdk11,后新特性
        sb.append("*".repeat(Math.max(0, starN)));
        sb.append(content.substring(content.length()-endN,content.length()-1));
        return  sb.toString();
    }

    /*
     *合并两个json字符串
     * key相同，则后者覆盖前者的值
     * key不同，则合并至前者
     *
     * @param originStr 原始json字符串
     * @param mergeStr  原始json字符串
     * @return  合并后的json字符串
     */
    public static String merge(String originStr,String mergeStr) {
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(originStr, mergeStr)) {
            return null;
        }

        JSONObject originJSON = JSONObject.parseObject(originStr);
        JSONObject mergeJSON = JSONObject.parseObject(mergeStr);

        if (originJSON == null || mergeJSON == null) {
            return null;
        }

        originJSON.putAll(mergeJSON);
        return originJSON.toJSONString();
    }
    public static String camel2Underline(String str) {

        if (StrUtil.isBlank(str)) {
            return "";
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                sb.append("_" + Character.toLowerCase(str.charAt(i)));
            } else {
                sb.append(str.charAt(i));
            }
        }
        return (str.charAt(0) + sb.toString()).toLowerCase();
    }

}
