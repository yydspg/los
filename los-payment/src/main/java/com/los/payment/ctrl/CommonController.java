package com.los.payment.ctrl;

import cn.hutool.core.codec.Base64;
import com.los.core.ctrls.AbstractCtrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/11
 */
// TODO 2024/3/11 : 此处代码作用
@RestController
@RequestMapping("/api/common")
public class CommonController extends AbstractCtrl {


    //网关支付form 表单输出
    @RequestMapping(value = "/payForm/{payData}")
    private String toPayForm( @PathVariable("payData") String payData) {
        super.request.setAttribute("payHtml", Base64.decodeStr(payData));
        return "common/toPay";
    }


    // 微信H5跳转与referer一致,重定向
    @RequestMapping(value = "/payUrl/{payData}")
    private String toPayUrl(@PathVariable("payData") String payData) {
        String payUrl = Base64.decodeStr(payData);
        super.request.setAttribute("payHtml","<script>window.location.href = '"+payUrl+"';</script>");
        return "common/toPay";
    }
}
