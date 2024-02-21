package com.los.components.oss.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO 未继承baseEntity
 * @author paul 2024/2/3
 */
@Data
@TableName("t_file")
@Schema(description = "文件")
public class File {
    private static final long serialVersionUID = 1L;
    @TableId
    @Schema(description = "唯一标识")
    private String id;

    @Schema(description = "原文件名")
    private String name;

    @Schema(description = "存储文件名")
    private String fileKey;

    @Schema(description = "大小")
    //TODO 实现自定义序列化器
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "路径")
    private String url;

    @Schema(description = "拥有者id")
    private String ownerId;

    @Schema(description = "用户类型")
    private String userEnums;

    @Schema(description = "文件夹ID")
    private String fileDirectoryId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
