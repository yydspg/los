package com.los.components.oss.plugin;

import com.los.components.oss.entity.enums.OssVenderEnum;

import java.io.InputStream;
import java.util.List;

/**
 *  文件插件接口
 * @author paul 2024/2/3
 */

public interface FilePlugin {
    /**
     * 插件名称
     */
    OssVenderEnum pluginName();
    /**
     * 文件路径上传
     *
     * @param filePath 文件路径
     * @param key 唯一标识
     * @return  getUrlPrefix() + key
     */
    String pathUpload(String filePath,String key);

    /**
     * 文件流上传
     *
     * @param inputStream 输入流
     * @param key 唯一标识
     * @return    getUrlPrefix() + key
     */
    String inputStreamUpload(InputStream inputStream,String key);
    /**
     * 删除文件
     *
     * @param key 唯一标识
     */
    void deleteFile(List<String> key);
}
