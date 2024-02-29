package com.los.core.model.dos;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * TODO 未实现对应数据库
 * @author paul 2024/2/4
 */
@Data
@TableName("t_oss_setting")
@Schema(description = "配置")
@NoArgsConstructor
public class Setting {
    public static final long serialVersionUID = 1L;
    @Schema(description = "配置Value")
    private String settingValue;
}
