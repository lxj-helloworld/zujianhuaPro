package com.example.route_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 12:53 PM
 **/
@Target(ElementType.FIELD) //该注解作用在属性上
@Retention(RetentionPolicy.CLASS) //该注解会在class文件中存在
public @interface Parameter {
    String name() default "";//以该参数作为跳转过程中传递参数的参数名
}
