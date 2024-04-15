package com.los.payment.utils;

import cn.hutool.core.util.StrUtil;
import com.los.core.utils.SpringBeansKit;
import com.los.payment.channel.IPaymentService;

/**
 * @author paul 2024/4/11
 */

public class PaymentKit {

    private static final String PAYWAY_PACKAGE_NAME = "payway";
    private static final String PAYWAYV3_PACKAGE_NAME = "paywayV3";

    /** 获取真实的支付方式Service **/
    public static IPaymentService getRealPayWayService(Object obj, String wayCode){

        try {

            //下划线转换驼峰 & 首字母大写
            String clsName = StrUtil.upperFirst(StrUtil.toCamelCase(wayCode.toLowerCase()));
            return (IPaymentService) SpringBeansKit.getBean(
                    Class.forName(obj.getClass().getPackage().getName()
                            + "." + PAYWAY_PACKAGE_NAME
                            + "." + clsName)
            );

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** 获取微信V3真实的支付方式Service **/
    public static IPaymentService getRealPayWayV3Service(Object obj, String wayCode){

        try {

            //下划线转换驼峰 & 首字母大写
            String clsName = StrUtil.upperFirst(StrUtil.toCamelCase(wayCode.toLowerCase()));
            return (IPaymentService) SpringBeansKit.getBean(
                    Class.forName(obj.getClass().getPackage().getName()
                            + "." + PAYWAYV3_PACKAGE_NAME
                            + "." + clsName)
            );

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
