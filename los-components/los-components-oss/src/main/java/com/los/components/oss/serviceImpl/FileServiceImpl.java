package com.los.components.oss.serviceImpl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.components.oss.entity.File;
import com.los.components.oss.entity.dto.FileOwnerDTO;
import com.los.components.oss.mapper.FileMapper;
import com.los.components.oss.plugin.FilePluginFactory;
import com.los.components.oss.service.FileService;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.SysUser;
import com.los.core.exception.BizException;
import com.los.core.utils.PageKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.los.core.constants.UserEnums;
import java.util.ArrayList;
import java.util.List;

/**
 * @author paul 2024/2/3
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Autowired
    private FilePluginFactory filePluginFactory;

    @Override
    public void batchDelete(List<String> ids) {

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(File::getId, ids);

        List<File> files = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        files.forEach(item -> keys.add(item.getFileKey()));
        filePluginFactory.filePlugin().deleteFile(keys);
        this.remove(queryWrapper);
    }

    @Override
    public void batchDeleteByDirectory(String directoryId) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getFileDirectoryId, directoryId);

        List<File> files = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        files.forEach(item -> keys.add(item.getFileKey()));
        filePluginFactory.filePlugin().deleteFile(keys);
        this.remove(queryWrapper);
    }

//    @Override
//    public void batchDelete(List<String> ids, SysUser authUser) {
//        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.in(File::getId, ids);
//
//        queryWrapper.eq(File::getUserEnums, authUser.getSysType());
//
//        借鉴lilishop oss 操作,此处角色定义未处理 TODO
//        //操作图片属性判定
//        switch (UserEnums.valueOf(authUser.getSysType())) {
//            case MANAGER:
//                queryWrapper.eq(File::getOwnerId, authUser.getId());
//                break;
//            case MERCHANT:
//                queryWrapper.eq(File::getOwnerId, authUser.getStoreId());
//                break;
//            default:
//                throw new BizException(ApiCodeEnum.USER_AUTHORITY_ERROR);
//        }
//        List<File> files = this.list(queryWrapper);
//        List<String> keys = new ArrayList<>();
//        files.forEach(item -> keys.add(item.getFileKey()));
//        filePluginFactory.filePlugin().deleteFile(keys);
//        this.remove(queryWrapper);
//    }

    @Override
    public IPage<File> customerPage(FileOwnerDTO fileOwnerDTO) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getName()), File::getName, fileOwnerDTO.getName())
                .eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileDirectoryId()),File::getFileDirectoryId, fileOwnerDTO.getFileDirectoryId())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileKey()), File::getFileKey, fileOwnerDTO.getFileKey())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileType()), File::getFileType, fileOwnerDTO.getFileType())
                .between(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getStartDate()) && CharSequenceUtil.isNotEmpty(fileOwnerDTO.getEndDate()),
                        File::getCreatedAt, fileOwnerDTO.getStartDate(), fileOwnerDTO.getEndDate());
        return this.page(PageKit.initPage(fileOwnerDTO), queryWrapper);
    }

    @Override
    public IPage<File> customerPageOwner(FileOwnerDTO fileOwnerDTO) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getOwnerId()), File::getOwnerId, fileOwnerDTO.getOwnerId())
                .eq(File::getUserEnums, fileOwnerDTO.getUserEnums())
                .eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileDirectoryId()),File::getFileDirectoryId, fileOwnerDTO.getFileDirectoryId())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getName()), File::getName, fileOwnerDTO.getName())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileKey()), File::getFileKey, fileOwnerDTO.getFileKey())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileType()), File::getFileType, fileOwnerDTO.getFileType())
                .between(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getStartDate()) && CharSequenceUtil.isNotEmpty(fileOwnerDTO.getEndDate()),
                        File::getCreatedAt, fileOwnerDTO.getStartDate(), fileOwnerDTO.getEndDate());
        return this.page(PageKit.initPage(fileOwnerDTO), queryWrapper);
    }
}
