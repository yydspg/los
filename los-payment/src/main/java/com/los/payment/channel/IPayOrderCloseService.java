package com.los.payment.channel;

import com.los.core.entity.PayOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;

/**
 * 关闭订单（渠道侧）接口定义
 * @author paul 2024/2/28
 */
/*
    在支付系统中，“渠道侧”通常是指支付处理链条中涉及到的具体支付渠道服务提供商这一侧。支付渠道是用户、商户之间完成支付交易的重要环节，它负责连接商户系统和银行或其他支付清算组织，实现资金的流动。

    举例来说：

    1. **线上支付渠道**：如微信支付、支付宝、银联在线支付等，它们为商户提供支付接口，处理用户的支付请求，验证交易合法性，并与银行系统交互完成扣款和清算。

    2. **线下支付渠道**：如POS机收单渠道、银行卡快捷支付等，这些渠道承担着用户在实体店消费时刷卡或扫码支付的信息传输与处理。

    “渠道侧”的具体内容包括但不限于：
    - **交易处理**：接收、验证、授权、记账等支付交易全流程处理。
    - **风险控制**：实施实名认证、防欺诈监控、限额管理等风控措施。
    - **资金清算**：与商户、银行或其他支付清算机构进行资金的划拨与结算。
    - **技术支持**：提供相应的API接口、SDK工具包等技术方案，供商户进行支付功能的集成。

    在支付系统的上下文中，“渠道侧”与“商户侧”相对应，商户侧即指使用支付服务的一方，包括线上线下商家以及各类需要接受支付的服务提供者。
    渠道侧与商户侧通过支付接口进行交互，共同确保支付过程的安全、稳定和高效。
 */
public interface IPayOrderCloseService {
    /** 获取到接口code **/
    String getIfCode();

    /** 关闭订单 **/
    ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
