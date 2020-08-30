package com.example.router_api;

import java.util.Map;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 1:08 PM
 * 路由表的分组
 **/
public interface RouterGroup {
    Map<String,Class<? extends RouterPath>> getGroupMap();
}
