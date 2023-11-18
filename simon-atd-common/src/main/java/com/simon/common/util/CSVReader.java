package com.simon.common.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiangpeng
 * @date 2022/03/15
 */
@Slf4j
public class CSVReader {

    public static List<String[]> readCSV(String path, String delimiter, String charset) {
        List<String[]> results = Lists.newArrayList();
        try {
            List<String> res = FileUtils.readLines(new File(path), charset);
            for (String re : res) {
                results.add(StringUtils.split(re, delimiter, -1));
            }
        } catch (IOException e) {
            log.error("readCSV failed", e);
        }
        return results;
    }

}
