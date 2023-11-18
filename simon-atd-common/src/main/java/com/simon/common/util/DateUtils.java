package com.simon.common.util;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.compress.utils.Lists;

/**
 * @Author yzy
 * @Date 2023/11/15
 */
public class DateUtils {

    public static final String dateStr1 = "yyyy-MM-dd";
    public static final String dateStr2 = "yyyy-MM-dd HH:mm";
    public static final String dateStr3 = "yyyyMMdd";

    /**
     * 转化时间字符串为yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String format1(String date) {
        DateTime dateTime = DateUtil.parse(date);
        return DateUtil.format(dateTime, dateStr1);
    }

    /**
     * 转化时间字符串为yyyy-MM-dd HH:mm
     *
     * @param date
     * @return
     */
    public static String format2(String date) {
        DateTime dateTime = DateUtil.parse(date);
        return DateUtil.format(dateTime, dateStr2);
    }

    /**
     * 转换时间戳为字符串
     *
     * @param timestamp
     * @return
     */
    public static String format3(Long timestamp) {
        DateTime date = DateUtil.date(timestamp);
        return DateUtil.format(date, dateStr2);
    }

    /**
     * 获取当天96个时间点
     *
     * @param date
     * @return
     */
    public static List<Long> getDateTimeList(String date) {
        List<Long> dateList = Lists.newArrayList();
        Date dateTime = DateUtil.parse(date);
        for (int i = 1; i <= 96; i++) {
            DateTime time = DateUtil.offsetMinute(dateTime, 15 * i);
            dateList.add(time.getTime());
        }
        return dateList;
    }

    /**
     * 获取范围内每天的日期
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<String> getDateList(String startDate, String endDate) {
        DateTime start = DateUtil.parse(startDate);
        DateTime end = DateUtil.parse(endDate);
        DateRange range = DateUtil.range(start, end, DateField.DAY_OF_MONTH);
        List<String> list = Lists.newArrayList();
        for (DateTime next : range) {
            String format = DateUtil.format(next, dateStr3);
            list.add(format);
        }
        return list;
    }

    public static void main(String[] args) {
        List<String> dateList = getDateList("20231016", "20231016");
        String jsonString = JSON.toJSONString(dateList);
        System.out.println(jsonString);
    }

}
