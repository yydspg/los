package com.los.components.oss.entity.dto;

import com.los.components.oss.entity.File;
import com.los.core.model.vo.SearchVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author paul 2024/2/3
 */
@Data
public class FileSearchParams implements Serializable {
    @Schema(description = "文件")
    private File file;
    @Schema(description = "搜索VO")
    private SearchVO searchVO;
    @Schema(description = "文件夹ID")
    private String fileDirectoryId;
}
