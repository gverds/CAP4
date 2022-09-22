package com.iisigroup.cap.base.configuration;

import java.util.ArrayList;
import java.util.List;

public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    public static List<String> dataSourceNames = new ArrayList<>();

    public static void setDataSourceName(String name) {
        contextHolder.set(name);
    }

    public static String getDataSourceName() {
        return contextHolder.get();
    }

    public static void clearDataSourceName() {
        contextHolder.remove();
    }

    public static boolean containsDataSource(String dataSourceName) {
        return dataSourceNames.contains(dataSourceName);
    }
}
