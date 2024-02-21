package com.los.payment.model;

import com.alipay.api.AlipayClient;
import com.los.core.constants.CS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付包Client 包装类
 * @author paul 2024/2/4
 */
/*
API Wrapper： API wrapper是一种设计模式或实践，它将一个复杂的API或者难以直接使用的接口封装起来，提供更简洁
、易于理解和使用的接口。比如，开发者可能为某个服务或库创建一个wrapper类或模块，该类或模块会隐藏原始API的复杂性，暴露更为直观的方法给上层应用调用。
 */
@Slf4j
@Data
@AllArgsConstructor
public class AlipayClientWrapper {

    //默认为 不使用证书方式
    private Byte useCert = CS.NO;

    /** 缓存支付宝client 对象 **/
    private AlipayClient alipayClient;
}
