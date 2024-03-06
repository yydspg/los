package com.los.payment.rqrs;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/*
 * 基础请求参数
 * @author paul 2024/2/4
 */
/*
    这段Java代码定义了一个名为`AbstractRQ`的抽象类，它是序列化（Serializable）对象，主要用于构建一组公共的请求参数结构
    此类应用于某种远程过程调用（RPC）或API请求场景，作为请求实体的基础结构，其中包含了几个核心属性：

    1. `version`：版本号属性，使用了`@NotBlank`注解进行校验，确保其在实际使用时不能为空，表明每个请求都需要包含版本信息，以便服务端进行版本管理和兼容性处理。

    2. `signType`：签名类型属性，同样使用了`@NotBlank`注解，要求该字段必须存在有效值，表明每个请求都需要指定一种签名算法或方式来确保请求完整性和安全性。

    3. `sign`：签名值属性，同样是通过`@NotBlank`注解进行非空校验，这是对请求内容进行签名后的结果，服务端可以通过验证签名来确保请求未被篡改。

    4. `reqTime`：接口请求时间属性，同样要求不能为空，通常用于记录请求发起的时间戳，可能用于防止重放攻击以及请求时效性校验。

    总之，这段代码是为了规范请求的数据格式，并通过注解实现了一定程度的数据验证，确保发送到后端的请求满足基本的安全性和一致性要
    由于类是抽象的，意味着它会被具体的请求实现类继承和扩展，进一步添加特定的业务请求参数。
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
    /* 版本号 */
    @NotBlank(message="版本号不能为空")
    protected String version;

    /* 签名类型 */
    @NotBlank(message="签名类型不能为空")
    protected String signType;

    /* 签名值 */
    @NotBlank(message="签名值不能为空")
    protected String sign;

    /* 接口请求时间 */
    @NotBlank(message="时间戳不能为空")
    protected String reqTime;
}
