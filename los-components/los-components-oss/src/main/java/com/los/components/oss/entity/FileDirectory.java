package com.los.components.oss.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * TODO 创建表
 * @author paul 2024/2/3
 */
@Data
@TableName("t_file_directory")
public class FileDirectory {
    /**
     *
     */
    @Schema(description = "主键")
    private String id;
    @Schema(description = "文件目录类型")
    private String directoryType;
    @Schema(description = "拥有者名称")
    private String directoryName;
    @Schema(description = "拥有者id")
    private String ownerId;
    @Schema(description = "父分类ID")
    private String parentId;
    @Schema(description = "层级" )
    @Min(value = 0, message = "层级最小为0")
    @Max(value = 2, message = "层级最大为2")
    private Integer level;
}
