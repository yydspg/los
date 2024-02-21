package com.los.components.oss.entity.dto;

import com.alipay.api.internal.util.StringUtils;
import com.los.core.model.vo.PageVO;
import com.los.core.utils.DateKit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

/**
 * TODO 建表
 * @author paul 2024/2/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileOwnerDTO extends PageVO {
    @Schema(description = "拥有者id")
    private String ownerId;

    @Schema(description = "用户类型")
    private String userEnums;

    @Schema(description = "原文件名")
    private String name;

    @Schema(description = "存储文件名")
    private String fileKey;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件夹ID")
    private String fileDirectoryId;

    @Schema(description = "起始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    public Date getConvertStartDate() {
        if (StringUtils.isEmpty(startDate)) {
            return null;
        }
        return DateKit.toDate(startDate, DateKit.STANDARD_DATE_FORMAT);
    }

    public Date getConvertEndDate() {
        if (StringUtils.isEmpty(endDate)) {
            return null;
        }
        //结束时间等于结束日期+1天 -1秒，
        Date date = DateKit.toDate(endDate, DateKit.STANDARD_DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.SECOND, -1);
        return calendar.getTime();
    }

}
