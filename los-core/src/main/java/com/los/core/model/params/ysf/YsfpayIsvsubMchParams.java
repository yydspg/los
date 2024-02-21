
package com.los.core.model.params.ysf;


import com.los.core.model.params.IsvsubMchParams;
import lombok.Data;

/*
 * 云闪付 配置信息
 * @author paul 2024/1/30
 */
@Data
public class YsfpayIsvsubMchParams extends IsvsubMchParams {

    private String merId;   // 商户编号

}
