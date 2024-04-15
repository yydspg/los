package com.los.payment.rqrs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * 通用RQ, 包含mchNo和appId 必填项
 * @author paul 2024/2/27
 */
@Data
public class AbstractMchAppRQ extends AbstractRQ {
    @NotBlank(message = "商户号mchNo不能为空")
    private String mchNo;
    @NotBlank(message = "商户应用appId不能为空")
    private String appId;
}
