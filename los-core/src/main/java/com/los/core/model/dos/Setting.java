package com.los.core.model.dos;

import com.baomidou.mybatisplus.annotation.TableId;
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
// TODO 2024/3/15 : 此类实际为我单独加的,目前无实际作用,以后会变化
@NoArgsConstructor
public class Setting {
    public static final long serialVersionUID = 1L;
    @Schema(description = "配置Value")
    @TableId
    private String settingValue;
}
