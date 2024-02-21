package com.los.components.oss.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.components.oss.entity.File;
import com.los.components.oss.entity.dto.FileOwnerDTO;

import java.util.List;

/**
 * 文件管理业务
 * @author paul 2024/2/3
 */

public interface FileService extends IService<File> {

    /**
     * 批量删除
     *
     * @param ids
     */
    void batchDelete(List<String> ids);
    /**
     * 根据文件夹ID批量删除
     *
     * @param directoryId 文件夹ID
     */
    void batchDeleteByDirectory(String directoryId);

    /**
     * 所有者批量删除
     *TODO 重构
     * @param ids      ID
     * @param authUser 操作者
     */
//    void batchDelete(List<String> ids, AuthUser authUser);


    /**
     * 自定义搜索分页
     *

     * @param fileOwnerDTO 文件查询

     * @return
     */
    IPage<File> customerPage(FileOwnerDTO fileOwnerDTO);

    /**
     * 所属文件数据查询
     *
     * @param ownerDTO 文件查询
     * @return
     */
    IPage<File> customerPageOwner(FileOwnerDTO ownerDTO);
}
