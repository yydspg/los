package com.los.core.utils;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONArray;
import com.los.core.constants.CS;
import com.los.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

/*
 * TODO 实现密钥管理和轮换机制
 * 签名工具类
 * @author paul 2024/1/30
 */
@Slf4j
public class SecKit {
    /*
    对称加密算法AES（Advanced Encryption Standard）是一种常见的选择。AES是对称密钥体制下的加密标准，这意味着在加密和解密数据时使用的是同一个密钥。这种加密方式的特点在于其加解密效率相对较高，并且适合于大量数据的加密。

    在实际应用中，前后端通信如果需要保证敏感信息的安全传输，可以采用AES对称加密的方式：

    前端在发送如密码、敏感用户数据等信息之前，利用一个预共享的密钥（通常是在安全环境下协商并存储在前端和后端）对数据进行加密。
    加密后的密文通过网络传输到后端服务器。
    后端收到密文后，使用相同的密钥进行解密，恢复出原始的明文数据。
    在实现AES加密时，还可以结合不同的模式（如CBC、CFB、CTR等）以及初始化向量（IV）来增强安全性。同时，为了进一步保障密钥的安全性，实践中往往还会配合使用密钥管理策略和密钥轮换机制。
     */
    public static byte[] AES_KEY = "4ChT08phkz59hquD795X7w==".getBytes();


    private static String encodingCharset = "UTF-8";


    /* 加密 **/
    public static String aesEncode(String str){
        return SecureUtil.aes(SecKit.AES_KEY).encryptHex(str);
    }
    /* 解密 **/
    public static String aesDecode(String str) {
        return SecureUtil.aes(SecKit.AES_KEY).decryptStr(str);
    }
    /*
    md5 加密算法
     */
    public static String md5(String value,String charset){
        MessageDigest md = null;
        try {
            byte[] data = value.getBytes(charset);
            md = MessageDigest.getInstance("MD5");
            byte[] digestData = md.digest(data);
            return toHex(digestData);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /*
    字节数组转化为hex字符串
     */
    public static String toHex(byte[] input){
        if (input == null) {
            return null;
        }
        /*
        创建一个StringBuffer对象，其初始容量设置为输入数组长度的两倍。这是因为每个字节（8位）在转换为16进制表示时通常会变成两个字符（如0x3F），所以提前预设容量可以避免频繁扩容操作
         */
        StringBuffer sb = new StringBuffer(input.length * 2);
        for (byte b : input) {
            int unsignedByte = b & 0xff;
            /*
            判断当前无符号整数值（即current）是否小于16。如果小于16，则向StringBuffer中追加一个前导零，以保持输出格式的一致性，比如单个字节值为10，在16进制中会显示为“0A”
             */
            if(unsignedByte < 16){
                sb.append("0");
            }
            sb.append(Integer.toHexString(unsignedByte));
        }
        return sb.toString();
    }
    /*
    map 转化为 url 参数
     */
    public static String genUrlParams(Map<String , Object> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Set<String> keys = paramMap.keySet();
        for (String k : keys) {
            sb.append(k).append("=").append(paramMap.get(k) == null ? "" : doEncode(paramMap.get(k).toString())).append("&");
        }
        /*
        循环结束后，由于循环内每次都会在追加参数后加上"&"，因此最后会多出一个不必要的"&"字符，所以通过sb.deleteCharAt(sb.length()-1)删除最后一个"&"字符。
         */
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    /*
    判断字符是否含有+,如果是,进行编码
     */
    static String doEncode(String str) {
        if(str.contains("+")) {
            return URLEncoder.encode(str, Charset.forName(encodingCharset));
        }
        return str;
    }
    public static String getPayWayCodeByBarCode(String barCode){

        if(StringUtils.isEmpty(barCode)) {
            throw new BizException("BarcodeIsNull");
        }

        //微信 ： 用户付款码条形码规则：18位纯数字，以10、11、12、13、14、15开头
        //文档： https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=5_1
        if(barCode.length() == 18 && Pattern.matches("^(10|11|12|13|14|15)(.*)", barCode)){
            return CS.PAY_WAY_CODE.WX_BAR;
        }
        //支付宝： 25~30开头的长度为16~24位的数字
        //文档： https://docs.open.alipay.com/api_1/alipay.trade.pay/
        else if(barCode.length() >= 16 && barCode.length() <= 24 && Pattern.matches("^(25|26|27|28|29|30)(.*)", barCode)){
            return CS.PAY_WAY_CODE.ALI_BAR;
        }
        //云闪付： 二维码标准： 19位 + 62开头
        //文档：https://wenku.baidu.com/view/b2eddcd09a89680203d8ce2f0066f5335a8167fa.html
        else if(barCode.length() == 19 && Pattern.matches("^(62)(.*)", barCode)){
            return CS.PAY_WAY_CODE.YSF_BAR;
        }
        else{  //暂时不支持的条码类型
            throw new BizException("UnsupportedBarcode");
        }
    }
    /*
    获取签名
     */
    public static String getSign(Map<String,Object> map,String k){
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer tem = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                list.add(tem.append(entry.getKey()).append("=").append(entry.getValue()).append("&").toString());
                tem.delete(0,tem.length()-1);
            }
        }
        int size = list.size();
        /*
        将列表转换为String数组 arrayToSort，并按字母顺序进行排序（忽略大小写）
         */
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort,String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        sb.append("key=").append(k);
        String s = sb.toString();
        log.info("signStr:{}",s);
        s = md5(s,encodingCharset).toUpperCase();
        log.info("sign:{}",s);
        return s;
    }
}
