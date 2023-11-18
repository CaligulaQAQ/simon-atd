package com.simon.common.util;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

public class RouteUtils {

    private static String rootPath = YamlUtils.getConfig("excel.rootpath");

    /**
     * 返回完整的路径
     *
     * @param appendPath 附加的路径参数
     * @return
     */
    public static String getWholePath(String appendPath) {
        return Paths.get(rootPath, appendPath).toString();
    }

    public static String path(String... args) {
        return StringUtils.join(args, File.separator);
    }

    public static String getRootPath(String... args) {
        return Paths.get(rootPath, args).toString();
    }

}
