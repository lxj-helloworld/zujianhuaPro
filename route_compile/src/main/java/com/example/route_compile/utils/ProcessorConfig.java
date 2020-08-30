package com.example.route_compile.utils;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 2:11 PM
 **/
public class ProcessorConfig {
    //Router注解的包名+类名
    public static final String ROUTER_PACKAGE = "com.example.route_annotation.Route";
    //接收参数的TAG标记
    public static final String OPTIONS = "moduleName";
    public static final String APT_PACKAGE = "packageNameForAPT";

    public static final String ACTIVITY_PACKAGE = "android.app.Activity";

    public static final String ROUTER_API_PACKAGE = "com.example.router_api";

    public static final String ROUTER_API_GROUP = ROUTER_API_PACKAGE + ".RouterGroup";
    public static final String ROUTER_API_PATH = ROUTER_API_PACKAGE + ".RouterPath";
    public static final String ROUTER_API_PARAMETER_GET = ROUTER_API_PACKAGE + ".ParameterGet";

    //parameterGet方法参数名字
    public static final String PARAMETER_NAME = "targetParameter";

    //parameterGet方法名字
    public static final String PARAMETER_METHOD_NAME = "getParameter";

    //String全类名
    public static final String STRING = "java.lang.String";

    //路由表中Path里面的方法名
    public static final String PATH_MEHTOD_NAME = "getPathMap";
    //路由表中Group里面的方法名
    public static final String GROUP_METHOD_NAME = "getGroupMap";
    public static final String PATH_VAR1 = "pathMap";
    public static final String GROUP_VAR1 = "groupMap";

    //路由表中Path最终要生成的文件名
    public static final String PATH_FILE_NAME = "Router$$Path$$";
    public static final String GROUP_FILE_NAME = "Router$$Group$$";
    public static final String PARAMETER_PACKAGE = "com.example.route_annotation.Parameter";
    public static final String PARAMETER_FILE_NAME = "$$Parameter";

}
