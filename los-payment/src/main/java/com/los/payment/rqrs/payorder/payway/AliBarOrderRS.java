
package com.los.payment.rqrs.payorder.payway;

import com.los.core.constants.CS;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： ALI_BAR
 *
 */
@Data
public class AliBarOrderRS extends UnifiedOrderRS {

    @Override
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.NONE;
    }

    @Override
    public String buildPayData(){
        return "";
    }

}
