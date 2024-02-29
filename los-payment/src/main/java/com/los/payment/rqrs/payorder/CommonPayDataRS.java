package com.los.payment.rqrs.payorder;

import com.los.core.constants.CS;
import com.los.core.utils.StringKit;
import lombok.Data;


/*
    通用支付数据RS,根据set的值，响应不同的payDataType
 * @author paul 2024/2/27
 */
@Data
public class CommonPayDataRS extends UnifiedOrderRS {

    /* 跳转地址 **/
    private String payUrl;

    /* 二维码地址 **/
    private String codeUrl;

    /* 二维码图片地址 **/
    private String codeImgUrl;

    /* 表单内容 **/
    private String formContent;

    @Override
    public String buildPayDataType(){

        if(StringKit.isNotEmpty(payUrl)){
            return CS.PAY_DATA_TYPE.PAY_URL;
        }

        if(StringKit.isNotEmpty(codeUrl)){
            return CS.PAY_DATA_TYPE.CODE_URL;
        }

        if(StringKit.isNotEmpty(codeImgUrl)){
            return CS.PAY_DATA_TYPE.CODE_IMG_URL;
        }

        if(StringKit.isNotEmpty(formContent)){
            return CS.PAY_DATA_TYPE.FORM;
        }

        return CS.PAY_DATA_TYPE.PAY_URL;
    }

    @Override
    public String buildPayData(){

        if(StringKit.isNotEmpty(payUrl)){
            return payUrl;
        }

        if(StringKit.isNotEmpty(codeUrl)){
            return codeUrl;
        }

        if(StringKit.isNotEmpty(codeImgUrl)){
            return codeImgUrl;
        }

        if(StringKit.isNotEmpty(formContent)){
            return formContent;
        }

        return "";
    }
}
