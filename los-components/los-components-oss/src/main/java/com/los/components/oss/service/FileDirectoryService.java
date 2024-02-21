package com.los.components.oss.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.components.oss.entity.FileDirectory;
import com.los.components.oss.entity.dto.FileDirectoryDTO;
import com.los.core.constants.UserEnums;

import java.util.List;

/**
 * 文件目录业务
 * @author paul 2024/2/3
 */

public interface FileDirectoryService extends IService<FileDirectory> {/**
 * 添加目录
 *
 * @param userEnum
 * @param id
 * @param ownerName
 */
void addFileDirectory(UserEnums userEnum, String id, String ownerName);

    /**
     * 获取文件目录
     *
     * @param ownerId 拥有者
     * @return
     */
    List<FileDirectoryDTO> getFileDirectoryList(String ownerId);

}
