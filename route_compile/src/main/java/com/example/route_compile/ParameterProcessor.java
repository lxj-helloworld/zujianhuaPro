package com.example.route_compile;

import com.example.route_annotation.Parameter;
import com.example.route_compile.factory.ParameterFactory;
import com.example.route_compile.utils.ProcessorConfig;
import com.example.route_compile.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 3:27 PM
 **/

@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {
    private Elements elementUtils; //类信息
    private Types typesUtils;//具体类型
    private Messager messager;//日志
    private Filer filer;//生成文件

    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //扫描的时候，看哪些地方使用了Parameter注解
        if(!ProcessorUtils.isEmpty(set)){
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            //有地方使用到了@Parameter
            if(!ProcessorUtils.isEmpty(elements)){
                for(Element element : elements){
                    //注解在属性上面，属性节点父节点是类节点
                    TypeElement enclosingElement = (TypeElement) element.getEnclosedElements();
                    if(tempParameterMap.containsKey(enclosingElement)){
                        tempParameterMap.get(enclosingElement).add(element);
                    }else{
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement,fields);
                    }
                }

                //生成类文件
                //判断是否需要生成类文件
                if(ProcessorUtils.isEmpty(tempParameterMap)) return true;
                //通过Element工具类，获取Parameter类型
                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.ROUTER_API_PARAMETER_GET);

                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,ProcessorConfig.PARAMETER_NAME).build();

                for(Map.Entry<TypeElement,List<Element>> entry : tempParameterMap.entrySet()){
                    //Map集合中的key是类名，比如MainActivity
                    TypeElement typeElement = entry.getKey();
                    //如果类名的类型和Activity类型不匹配
                    if(!typesUtils.isSubtype(typeElement.asType(),activityType.asType())){
                        throw new RuntimeException("@Paramater注解目前仅限用于Activity类之上");
                    }
                    //获取类名
                    ClassName className = ClassName.get(typeElement);

                    //方法架子
                    ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();
                    //添加方法的第一行
                    factory.addFirstStatement();
                    //多行，循环

                    for(Element fieldElement : entry.getValue()){
                        factory.buildStatement(fieldElement);
                    }
                    //最终生成的类文件名（类名$$Parameter） 比如MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE,"APT生成获取参数类文件"  + className.packageName() + "." + finalClassName);
                    //开始生成文件
                    try {
                        JavaFile.builder(className.packageName(),   //包名
                                TypeSpec.classBuilder(finalClassName)   //类名
                        .addSuperinterface(ClassName.get(parameterType)) //实现ParameterLoad接口
                        .addModifiers(Modifier.PUBLIC) //public修饰符
                        .addMethod(factory.build()) //方法的构建（方法参数 + 方法体）
                        .build())  //类构建完成
                                .build()//JavaFile构建完成
                                .writeTo(filer); //文件生成器开始生成类文件
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
        return false;
    }
}
