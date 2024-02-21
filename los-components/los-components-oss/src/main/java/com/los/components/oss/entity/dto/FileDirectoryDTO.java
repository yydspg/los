package com.los.components.oss.entity.dto;

import com.los.components.oss.entity.FileDirectory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 表结构优化
 * @author paul 2024/2/3
 */
@Data
@AllArgsConstructor
public class FileDirectoryDTO extends FileDirectory{
    @Schema(description = "文件目录列表")
    private List<FileDirectory> children = new ArrayList<>();
    public FileDirectoryDTO(FileDirectory fd) {}
}
