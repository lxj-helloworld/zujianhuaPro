package com.example.route_compile.factory;

import com.example.route_annotation.Parameter;
import com.example.route_compile.utils.ProcessorConfig;
import com.example.route_compile.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 2:34 PM
 * 参数工厂
 **/
public class ParameterFactory {
    //方法的构建
    private MethodSpec.Builder method;

    //类名
    private ClassName className;

    ///用来报告错误，警告和提示信息
    private Messager messager;
    public ParameterFactory(Builder builder){
        this.messager = builder.messager;
        this.className = builder.className;
        //通过方法参数体构建方法体，
        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }


    public void addFirstStatement(){
        method.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME,className,className);
    }

    public MethodSpec build(){
        return method.build();
    }

    //多行循环
    public void buildStatement(Element element){
        //遍历注解的属性节点，生成函数体
        TypeMirror typeMirror = element.asType();
        //获取TypeKind枚举类型的序列号
        int type = typeMirror.getKind().ordinal();
        //获取属性名
        String fieldName = element.getSimpleName().toString();
        //获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();
        //判断注解的值为空的情况下的处理
        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        //最终拼接的前缀
        String finalValue = "t." + fieldName;
        String methodContent = finalValue + " = t.getIntent().";

        if(type == TypeKind.INT.ordinal()){
            methodContent = methodContent + "getIntExtra($S, " + finalValue + ")";
        }else if(type == TypeKind.BOOLEAN.ordinal()){
            methodContent = methodContent + "getBooleanExtra($S, " + finalValue + ")";
        }else{
            if(typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)){
                methodContent = methodContent + "getStringExtra($S)";
            }
        }
        if(methodContent.endsWith(")")){
            //添加最终拼接方法内容语句
            method.addStatement(methodContent,annotationValue);
        }else{
            messager.printMessage(Diagnostic.Kind.ERROR,"目前支持String、int、boolean传参");
        }
    }



    public static class Builder{
        private Messager messager;
        private ClassName className;
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec){
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager){
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className){
            this.className = className;
            return this;
        }

        public ParameterFactory build(){
            if(parameterSpec == null){
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }
            if(className == null){
                throw new IllegalArgumentException("方法内容中的className为空");
            }
            if(messager == null){
                throw new IllegalArgumentException("messager为空");
            }
            return new ParameterFactory(this);
        }


    }
}
