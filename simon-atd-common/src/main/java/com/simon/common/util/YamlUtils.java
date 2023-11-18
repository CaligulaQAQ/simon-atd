package com.simon.common.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

public class YamlUtils {
    private static final Map<String, String> confMap = new HashMap<>(256);

    static {
        ClassLoader classLoader = YamlUtils.class.getClassLoader();
        Yaml yaml = new Yaml();
        InputStream applicationIn = classLoader.getResourceAsStream("application.yml");
        fillConfMap(yaml.loadAs(applicationIn, Map.class), confMap, null);
        IOUtils.closeQuietly(applicationIn);
        String currentProfile = confMap.get("spring.profiles.active");
        if (currentProfile != null) {
            InputStream currentProfileIn = classLoader.getResourceAsStream(
                String.format("application-%s.yml", currentProfile));
            fillConfMap(yaml.loadAs(currentProfileIn, Map.class), confMap, null);
            IOUtils.closeQuietly(currentProfileIn);
        }

    }

    private static void fillConfMap(Map<String, Object> sourceMap, Map<String, String> destinationMap, String key) {
        // 遍历yaml文件存入confMap
        sourceMap.forEach((key1, v) -> {
            String k = key != null ? key + "." + key1 : key1;
            if (v instanceof Map) {
                fillConfMap((Map)v, destinationMap, k);
            } else {
                destinationMap.put(k, String.valueOf(v));
            }
        });
    }

    public static String getConfig(String key) {
        return confMap.get(key);
    }
}
