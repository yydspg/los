package com.los.core.utils;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.SystemClock;
import com.los.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;



/**
 * @author paul 2024/2/4
 */

public class DateKit extends DateUtil{
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    /** 获取参数时间当天的开始时间  **/
    public static Date getBegin(Date date){

        if(date == null) {
            return null;
        }
        return DateUtil.beginOfDay(date).toJdkDate();
    }

    /** 获取参数时间当天的结束时间 **/
    public static Date getEnd(Date date){
        if(date == null) {
            return null;
        }
        return DateUtil.endOfDay(date).toJdkDate();
    }
    /**
     * 获取自定义查询时间
     * today|0  -- 今天
     * yesterday|0  -- 昨天
     * near2now|7  -- 近xx天， 到今天
     * near2yesterday|30   -- 近xx天， 到昨天
     * customDate|2020-01-01,N  -- 自定义日期格式  N表示为空， 占位使用
     * customDateTime|2020-01-01 23:00:00,2020-01-01 23:00:00 -- 自定义日期时间格式
     *
     * @return
     */
    public static Date[] getQueryDateRange(String queryParamVal){

        //查询全部
        if(StringUtils.isEmpty(queryParamVal)){
            return new Date[]{null, null};
        }

        //根据 | 分割
        String[] valArray = queryParamVal.split("\\|");
        if(valArray.length != 2){ //参数有误
            throw new BizException("查询时间参数有误");
        }
        String dateType = valArray[0];  //时间类型
        String dateVal = valArray[1];  //搜索时间值

        Date nowDateTime = new Date();  //当前时间

        if("today".equals(dateType)){ //今天

            return new Date[]{getBegin(nowDateTime), getEnd(nowDateTime)};

        }else if("yesterday".equals(dateType)){  //昨天

            Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天
            return new Date[]{getBegin(yesterdayDateTime), getEnd(yesterdayDateTime)};

        }else if("near2now".equals(dateType)){  //近xx天， xx天之前 ~ 当前时间

            Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
            Date offsetDayDate = DateUtil.offsetDay(nowDateTime, offsetDay).toJdkDate();
            return new Date[]{getBegin(offsetDayDate), getEnd(nowDateTime)};

        }else if("near2yesterday".equals(dateType)){  //近xx天， xx天之前 ~ 昨天

            Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天

            Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
            Date offsetDayDate = DateUtil.offsetDay(yesterdayDateTime, offsetDay).toJdkDate();
            return new Date[]{getBegin(offsetDayDate), getEnd(yesterdayDateTime)};

        }else if("customDate".equals(dateType) || "customDateTime".equals(dateType)){ //自定义格式

            String[] timeArray = dateVal.split(","); //以逗号分割
            if(timeArray.length != 2) {
                throw new BizException("查询自定义时间参数有误");
            }

            String timeStr1 = "N".equalsIgnoreCase(timeArray[0]) ? null : timeArray[0] ;  //开始时间，
            String timeStr2 = "N".equalsIgnoreCase(timeArray[1]) ? null : timeArray[1];  //结束时间， N表示为空， 占位使用

            Date time1 = null;
            Date time2 = null;

            if(StringUtils.isNotEmpty(timeStr1)){
                time1 = DateUtil.parseDateTime("customDate".equals(dateType) ? (timeStr1 + " 00:00:00" ) : timeStr1);
            }
            if(StringUtils.isNotEmpty(timeStr2)){
                time2 = DateUtil.parse( (  "customDate".equals(dateType) ? (timeStr2 + " 23:59:59.999" ) : timeStr2  + ".999"  ) , DatePattern.NORM_DATETIME_MS_FORMAT);
            }
            return new Date[]{time1, time2};

        }else{
            throw new BizException("查询时间参数有误");
        }
    }
    /**
     * 将一个字符串转换成日期格式
     *
     * @param date    字符串日期
     * @param pattern 日期格式
     * @return date
     */
    public static Date toDate(String date, String pattern) {
        if ("".equals("" + date)) {
            return null;
        }
        if (pattern == null) {
            pattern = STANDARD_DATE_FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Date newDate = new Date();
        try {
            newDate = sdf.parse(date);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newDate;
    }
    public static int getIntervalDate(String startTime,String endTime) {
        startTime = startTime + "00.00.00";
        endTime = endTime + "23:59:59";
        long res = DateUtil.betweenDay(DateUtil.parseDate(startTime), DateUtil.parseDate(endTime), true);
        return (int)res;
    }
    /** 公共函数，获取当前时间。  **/
    public static Long currentTimeMillis(){
//		System.currentTimeMillis(); // fortify 检测属于安全漏洞
        return SystemClock.now();
    }


}


