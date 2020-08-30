package com.example.route_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 10:36 AM
 **/
@Target(ElementType.TYPE) //作用在类上
@Retention(RetentionPolicy.CLASS) //保留到CLASS
public @interface Route {
    String value(); //详细路径名，比如/main/MainActivity，接收一个字符串参数
    String group() default "";//路由组名，比如main
}
