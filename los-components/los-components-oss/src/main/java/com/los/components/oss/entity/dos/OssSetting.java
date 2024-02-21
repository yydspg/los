package com.los.components.oss.entity.dos;

import com.los.components.oss.entity.enums.OssVenderEnum;
import com.los.core.utils.StringKit;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * TODO oss模块存在重大逻辑错误,与com.los.service存在相同类
 * @author paul 2024/2/4
 */
@Data
public class OssSetting implements Serializable {
    @Serial
    private static final long serialVersionUID = 2975271656230801861L;

    /**
     * oss类型
     */
    private String type;

    /**
     * 阿里云-域名
     */
    private String aliyunOSSEndPoint = "";
    /**
     * 阿里云-储存空间
     */
    private String aliyunOSSBucketName = "";
//    /**
//     * 阿里云-存放路径路径
//     */
//    private String aliyunOSSPicLocation = "";
    /**
     * 阿里云-密钥id
     */
    private String aliyunOSSAccessKeyId = "";
    /**
     * 阿里云-密钥
     */
    private String aliyunOSSAccessKeySecret = "";

    /**
     * minio服务地址
     */
    private String m_endpoint;

    /**
     * minio 前端请求地址
     */
    private String m_frontUrl;

    /**
     * minio用户名
     */
    private String m_accessKey;

    /**
     * minio密码
     */
    private String m_secretKey;

    /**
     * minio bucket名称
     */
    private String m_bucketName;
    public String getType() {
        //默认给阿里云oss存储类型
        if (StringKit.isEmpty(type)) {
            return OssVenderEnum.ALI_OSS.name();
        }
        return type;
    }
}
