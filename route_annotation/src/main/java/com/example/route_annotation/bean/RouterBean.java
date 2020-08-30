package com.example.route_annotation.bean;

import javax.lang.model.element.Element;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 12:14 PM
 * 路由路径Path的最终实体封装类
 **/

public class RouterBean {
    public enum TypeEnum{
        ACTIVITY
    }

    private TypeEnum typeEnum; //枚举类型，Activity
    private Element element;//类节点，依靠javapoet可以拿到很多的信息
    private Class<?> myClass;//被注解的Class对象，例如MainActivity.class
    private String path;//路由地址，标识Activity.class的key值
    private String group;//路由组，标识Activity归属于哪一部分


    public RouterBean(TypeEnum typeEnum, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(TypeEnum typeEnum, Class<?> myClass, String path, String group){
        return new RouterBean(typeEnum,myClass,path,group);
    }


    //借由建造者模式初始化
    public RouterBean(Builder builder){
        this.typeEnum = builder.activity;
        this.element = builder.element;
        this.myClass = builder.clazz;
        this.path =  builder.path;
        this.group = builder.group;
    }

    //构造者模式生成对象相关
    public static class Builder{
        private TypeEnum activity;
        private Element element;
        private Class<?> clazz;
        private String path;
        private String group;

        public Builder addType(TypeEnum typeEnum){
            this.activity = typeEnum;
            return this;
        }

        public Builder addElement(Element element){
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz){
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path){
            this.path = path;
            return this;
        }

        public Builder addGroup(String group){
            this.group = group;
            return this;
        }

        public RouterBean build(){
            if(path == null || path.length() == 0){
                throw new IllegalArgumentException("path 必填项为空");
            }
            return new RouterBean(this);
        }

    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
