package com.los.components.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/28
 */

@Data
@Component
@ConfigurationProperties(prefix="isys.oss")
// TODO 2024/4/11 : 修改工程逻辑,oss不适合独立作为一个 module ,应该附属于 core 内部
public class OssYmlConfig {

    @NestedConfigurationProperty //指定该属性为嵌套值, 否则默认为简单值导致对象为空（外部类不存在该问题， 内部static需明确指定）
    private Oss oss;

    /** 系统oss配置信息 **/
    @Data
    public static class Oss{

        /** 存储根路径 **/
        private String fileRootPath;

        /** 公共读取块 **/
        private String filePublicPath;

        /** 私有读取块 **/
        private String filePrivatePath;

        /** oss类型 **/
        private String serviceType;

    }
}
