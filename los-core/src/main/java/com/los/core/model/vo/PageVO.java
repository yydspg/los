package com.los.core.model.vo;

import cn.hutool.core.text.CharSequenceUtil;
import com.los.core.utils.StringKit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/*
 * @author paul 2024/2/4
 */
@Data
public class PageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "页号")
    private Integer pageNumber = 1;

    @Schema(description = "页面大小")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String sort;

    @Schema(description = "排序方式 asc/desc")
    private String order;

    @Schema(description = "需要驼峰转换蛇形")
    private Boolean notConvert;

    public String getSort() {
        if (CharSequenceUtil.isNotEmpty(sort)) {
            if (notConvert == null || Boolean.FALSE.equals(notConvert)) {
                return StringKit.camel2Underline(sort);
            } else {
                return sort;
            }
        }
        return sort;
    }
}
