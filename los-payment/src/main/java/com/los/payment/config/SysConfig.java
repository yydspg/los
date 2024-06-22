package com.los.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * 系统Yml配置参数定义Bean
 * @author paul 2024/2/4
 */
@Data
@Component
@ConfigurationProperties(prefix="isys")
public class SysConfig {

    public Boolean allowCors;

    public Boolean cacheConfig;

    /* 超时时间 **/
    public Integer timeout;
    /* redis 前缀 **/
    public String Prefix;
}
