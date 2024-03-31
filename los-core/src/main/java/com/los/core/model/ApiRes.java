package com.los.core.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.utils.JSONKit;
import com.los.core.utils.SecKit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 * 接口返回对象,并提供对应操作接口
 * @author paul 2024/1/30
 */
// TODO 2024/3/11 : 调整ApiRes类无泛型
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRes implements Serializable {
    /*
    业务响应码
     */
    private Integer code;

    /*
    业务响应信息
     */
    private String msg;

    /*
    数据对象
     */
    private Object data;
    /*
    签名值
    数据完整性：

    签名是由发送方根据接口返回内容和一些私有信息（如密钥）通过特定算法生成的。它能够证明返回的数据在传输过程中没有被篡改或丢失。
    接收方收到响应后，会使用相同的算法对收到的数据进行计算，得到一个新的签名，并与接收到的签名进行比较。如果二者匹配，则说明数据在传输过程中是完整的。
    身份验证：

    通过签名可以验证数据来源的真实性。每个签名都与一个特定的实体相关联，通常包含了发送者的标识信息，比如密钥或者证书等。只有持有正确密钥的实体才能生成正确的签名。
    这样一来，接收方可以根据签名确认消息确实来自预期的、经过授权的发送者，而不是冒充的第三方。
    防重放攻击：

    在签名中可能会包含时间戳或其他一次性参数，使得每次请求都有不同的签名。这样可以防止恶意用户截获并重放之前的有效响应。
    加密通信：

    尽管签名本身并不提供数据加密，但它可以与加密技术结合使用，以增强安全保护。例如，在HTTPS通信中，除了对传输内容进行加密外，还会对传输的消息体生成数字签名，以确保即使数据被解密，也能验证其未被篡改。
     */
    private String sign;

    /*
    输出json格式字符串
     */
    public String toJSONString() {return JSON.toJSONString(this);}
    /*
    业务处理成功
     */
    public static <T> ApiRes success(T data) {
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(),ApiCodeEnum.SUCCESS.getMsg(),data,null);
    }
    public static ApiRes success() {
        return success(null);
    }
    /*
    业务处理成功并自动签名
     */
    public static ApiRes successWithSign(Object data,String mchKey) {
        //数据 == null ,无需签名
        if(data == null){
            return success();
        }
        JSONObject o = JSONObject.from(data);
        String sign = SecKit.getSign(o, mchKey);
        return  new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, sign);
    }
    /*
    业务处理成功,返回json格式,一对k,v
     */
    public static ApiRes successWithJSON(String k,Object val) {
        return success(JSONKit.newJsonObject(k,val));
    }
    /*
    业务处理异常,直接返回错误信息
     */
    public static ApiRes customFail(String customMsg) {
        return new ApiRes(ApiCodeEnum.CUSTOM_FAIL.getCode(), ApiCodeEnum.CUSTOM_FAIL.getMsg(), null,null);
    }
    /*
    业务处理失败,返回对应错误信息
     */
    public static ApiRes fail(ApiCodeEnum ace,String... params) {
        return new ApiRes(ace.getCode(),(params == null || params.length == 0) ? ace.getMsg():String.format(ace.getMsg(),params),null,null);
    }
}
