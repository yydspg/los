package com.los.payment.rqrs;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 基础请求参数
 * @author paul 2024/2/4
 */
@Data
public abstract  class AbstractRQ implements Serializable {
    /*
    在Java中，@NotBlank注解是Bean Validation（JSR 303/JSR 349/JSR 380）规范的一部分，
    主要用于对字符串类型的字段或参数进行非空白验证。具体含义如下：
    当@NotBlank注解应用于字符串类型的属性或方法参数时，它会检查该字符串是否满足以下条件：
    字符串不为null。
    字符串经过trim()方法处理后，其长度大于0，即字符串中至少包含一个非空白字符（换言之，不允许仅由空白字符组成的字符串）。
     */
    /** 版本号 **/
    @NotBlank(message="版本号不能为空")
    protected String version;

    /** 签名类型 **/
    @NotBlank(message="签名类型不能为空")
    protected String signType;

    /** 签名值 **/
    @NotBlank(message="签名值不能为空")
    protected String sign;

    /** 接口请求时间 **/
    @NotBlank(message="时间戳不能为空")
    protected String reqTime;
}
