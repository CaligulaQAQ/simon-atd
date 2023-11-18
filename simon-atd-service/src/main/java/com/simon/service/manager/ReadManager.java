package com.simon.service.manager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.simon.common.util.DateUtils;
import com.simon.common.util.RouteUtils;
import lombok.Cleanup;
import org.apache.commons.io.FileUtils;

/**
 * @Author yzy
 * @Date 2023/11/14
 */
public class ReadManager {

    public static List<BigDecimal> readActualRealPrice(String date, String station) {
        String path = RouteUtils.getRootPath(date, DateUtils.format1(date) + "交易结果查询",
            "发电侧实时交易结果-" + station + ".xls");
        return getBigDecimals(path);
    }

    private static List<BigDecimal> getBigDecimals(String path) {
        @Cleanup
        ExcelReader reader = ExcelUtil.getReader(path);
        List<Object> objects = reader.readColumn(7, 1);
        return objects.stream().map(e -> new BigDecimal(e.toString())).collect(Collectors.toList());
    }

    public static List<BigDecimal> readActualAheadPrice(String date, String station) {
        String path = RouteUtils.getRootPath(date, DateUtils.format1(date) + "交易结果查询",
            "一次发电侧日前交易结果-" + station + ".xls");
        return getBigDecimals(path);
    }

    public static List<BigDecimal> readPredictRealPrice(String date) throws IOException {
        List<String> stringList = FileUtils.readLines(
            new File(RouteUtils.getRootPath("real_time_prices", date + ".csv")),
            StandardCharsets.UTF_8);
        return stringList.stream().map(e -> new BigDecimal(e).setScale(2, RoundingMode.HALF_UP))
            .collect(Collectors.toList());
    }

    public static List<BigDecimal> readPredictAheadPrice(String date) throws IOException {
        List<String> stringList = FileUtils.readLines(
            new File(RouteUtils.getRootPath("day_ahead_prices", date + ".csv")),
            StandardCharsets.UTF_8);
        return stringList.stream().map(e -> new BigDecimal(e).setScale(2, RoundingMode.HALF_UP))
            .collect(Collectors.toList());
    }

}
