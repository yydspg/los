package com.los.payment.rqrs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * 商户获取渠道用户id的请求对象
 * @author paul 2024/2/27
 */
@Data
public class ChannelUserIdRQ extends AbstractMchAppRQ{
    /* 接口代码 ,auto-->自动获取*/
    @NotBlank(message = "接口代码不能为空")
    private String ifCode;
    /* 商户扩展参数 */
    private String extParam;
    /* 回调地址 */
    @NotBlank(message = "回调地址不能为空")
    private String redirectUrl;
}
