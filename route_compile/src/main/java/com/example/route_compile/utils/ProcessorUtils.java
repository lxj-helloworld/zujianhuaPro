package com.example.route_compile.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 2:29 PM
 * 字符串、集合判空工具
 **/
public class ProcessorUtils {
    public static boolean isEmpty(CharSequence cs){
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(final Map<?,?> map){
        return map == null || map.isEmpty();
    }
}

