package com.example.router_api;

import com.example.route_annotation.bean.RouterBean;

import java.util.Map;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 1:05 PM
 *
 * 路由表中每一个分组下对应的一组path和Activity的对应关系
 **/
public interface RouterPath {
    Map<String, RouterBean> getPathMap();
}
