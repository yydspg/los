package com.los.core.model.vo;


import com.los.core.utils.DateKit;
import com.los.core.utils.StringKit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author paul 2024/2/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVO implements Serializable {
    @Schema(description = "起始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    public Date getConvertStartDate() {
        if (StringKit.isEmpty(startDate)) {
            return null;
        }
        return DateKit.toDate(startDate, DateKit.STANDARD_DATE_FORMAT);
    }

    public Date getConvertEndDate() {
        if (StringKit.isEmpty(endDate)) {
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
