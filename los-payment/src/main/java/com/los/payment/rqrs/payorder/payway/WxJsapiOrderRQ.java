
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
 * 支付方式： WX_JSAPI
 *
 */
/*
JSAPI支付方式是指微信支付中通过JavaScript API接口实现的一种支付解决方案。在微信生态下，
商户（通常是网站或APP）想要在自己的平台上集成微信支付功能，可以采用JSAPI支付的方式来让用户在不离开商户平台的情况下完成支付流程。

具体实现步骤如下：

1. 商户首先需要在微信公众平台上注册成为商户，并获取对应的APPID和API秘钥等必要信息。
2. 商户在后台服务器端生成预支付交易单，调用微信支付的接口获取预支付交易会话标识（prepay_id）。
3. 前端通过微信JSAPI SDK加载必要的支付配置信息，包括appId、timeStamp、nonceStr、package、signType和paySign等参数，其中sign是根据上述信息按照微信规定的签名算法生成的一个签名。
4. 用户在商户的网页或小程序内点击支付按钮时，前端调用微信提供的`wx.requestPayment`等JSAPI，将上述配置信息传给微信客户端。
5. 微信客户端收到请求后，弹出支付窗口，显示商品详情及应付金额，用户确认支付后，通过微信账号完成支付操作（输入密码或指纹等验证）。
6. 完成支付后，微信会通过预先设定好的异步通知URL（notify_url）向商户服务器发送支付结果通知。

通过这种方式，用户能够在无需跳转至微信外部页面的情况下，利用微信内的账户余额、银行卡或其他支付方式完成支付，极大地提升了支付体验和转化率。
 */
@Data
public class WxJsapiOrderRQ extends UnifiedOrderRQ {

    /* 微信openid **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /* 标志是否为 subMchAppId的对应 openId， 0-否， 1-是， 默认否  **/
    private Byte isSubOpenId;

    /* 构造函数 **/
    public WxJsapiOrderRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.WX_JSAPI);
    }

    @Override
    public String getChannelUserId() {
        return this.openid;
    }
}
