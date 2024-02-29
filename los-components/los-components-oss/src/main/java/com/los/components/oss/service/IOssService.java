package com.los.components.oss.service;

import com.los.components.oss.constant.OssSavePlaceEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author paul 2024/2/28
 */

public interface IOssService {
    /** 上传文件 & 生成下载/预览URL **/
    String upload2PreviewUrl(OssSavePlaceEnum ossSavePlaceEnum, MultipartFile multipartFile, String saveDirAndFileName);

    /** 将文件下载到本地
     * 返回是否 写入成功
     * false: 写入失败， 或者文件不存在
     * **/
    boolean downloadFile(OssSavePlaceEnum ossSavePlaceEnum, String source, String target);
}
