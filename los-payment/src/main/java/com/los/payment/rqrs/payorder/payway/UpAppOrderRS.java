
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.CommonPayDataRS;
import lombok.Data;

/*
 * 支付方式： UPACP_APP
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/12/1 19:57
 */
@Data
public class UpAppOrderRS extends CommonPayDataRS {

    private String payData;

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData(){
        return payData;
    }

}
