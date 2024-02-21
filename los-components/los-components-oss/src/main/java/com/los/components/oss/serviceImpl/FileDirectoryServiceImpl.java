package com.los.components.oss.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.components.oss.entity.FileDirectory;
import com.los.components.oss.entity.dto.FileDirectoryDTO;
import com.los.components.oss.mapper.FileDirectoryMapper;
import com.los.components.oss.service.FileDirectoryService;

import com.los.core.constants.UserEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
/**
 * TODO 学习功能
 *文件管理业务层实现
 * @author paul 2024/2/3
 */

@Service
@RequiredArgsConstructor
public class FileDirectoryServiceImpl extends ServiceImpl<FileDirectoryMapper, FileDirectory> implements FileDirectoryService {


    @Override
    public void addFileDirectory(UserEnums userEnum, String id, String ownerName) {
        FileDirectory fileDirectory = new FileDirectory();
        fileDirectory.setOwnerId(id);
        fileDirectory.setDirectoryName(ownerName);
        fileDirectory.setDirectoryType(userEnum.name());
        this.save(fileDirectory);
    }

    @Override
    public List<FileDirectoryDTO> getFileDirectoryList(String scene) {

        List<FileDirectory> fileDirectoryList = this.list();
        List<FileDirectoryDTO> fileDirectoryDTOList = new ArrayList<>();

        fileDirectoryList.forEach(item -> {
            if (item.getLevel() == 0) {
                FileDirectoryDTO fileDirectoryDTO = new FileDirectoryDTO(item);
                initChild(fileDirectoryDTO, fileDirectoryList);
                fileDirectoryDTOList.add(fileDirectoryDTO);
            }
        });

        return fileDirectoryDTOList;
    }


    /**
     * 递归初始化子树
     */
    private void initChild(FileDirectoryDTO fileDirectoryDTO, List<FileDirectory> fileDirectoryList) {
        if (fileDirectoryList == null) {
            return;
        }
        fileDirectoryList.stream()
                .filter(item -> (item.getParentId().equals(fileDirectoryDTO.getId())))
                .forEach(child -> {
                    FileDirectoryDTO childTree = new FileDirectoryDTO(child);
                    initChild(childTree, fileDirectoryList);
                    fileDirectoryDTO.getChildren().add(childTree);
                });
    }
}
