package com.los.components.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/28
 */

@Data
@Component
@ConfigurationProperties(prefix="isys.oss.aliyun-oss")
public class AliyunOssYmlConfig {

    private String endpoint;
    private String publicBucketName;
    private String privateBucketName;
    private String accessKeyId;
    private String accessKeySecret;
}
