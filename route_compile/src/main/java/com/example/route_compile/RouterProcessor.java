package com.example.route_compile;

import com.example.route_annotation.Route;
import com.example.route_annotation.bean.RouterBean;
import com.example.route_compile.utils.ProcessorConfig;
import com.example.route_compile.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

//自动注册，以便该类可以在编译器干活
@AutoService(Processor.class)
//允许支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes(ProcessorConfig.ROUTER_PACKAGE)
//指定JDK编译版本，必须写
@SupportedSourceVersion(SourceVersion.RELEASE_7)
//注解处理器接收的参数，从APP传递参数到该类
@SupportedOptions({ProcessorConfig.OPTIONS,ProcessorConfig.APT_PACKAGE})

public class RouterProcessor extends AbstractProcessor {
    //操作Element的工具类，（类、函数和属性其实都是Element）
    private Elements elementTool;
    //type（类信息）的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;
    //Messager用来打印日志相关信息，类似于安卓中的Log.d和Log.e
    private Messager messager;
    //文件生成器，类、资源等，就是最终要生成的文件，是需要Filer来完成的
    private Filer filer;

    private String options;//模块传递过来的模块名
    private String aptPackage;//模块传递过来的包名

    //仓库 Path
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();
    //仓库 Group
    private Map<String,String> mAllGroupMap = new HashMap<>();

    //做初始化工作，作用同Activity中的onCreate函数
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化对象
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        //获取从build.grale中传递进来的参数
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE,">>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE,">>>>>>>>>>>>>>> aptPackage:" + aptPackage);

        if(options != null && aptPackage != null){
            messager.printMessage(Diagnostic.Kind.NOTE,"APT环境搭建完成");
        }else{
            messager.printMessage(Diagnostic.Kind.NOTE,"APT环境有问题");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //扫描结果，返回使用了Route注解的类集合
        if(set.isEmpty()){
            messager.printMessage(Diagnostic.Kind.NOTE,"没有发现被Router注解修饰的地方");
            return false;
        }
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeMirror activityMirror = activityType.asType();

        //获取所有被@Route注解修饰的类的集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        //遍历所有的类节点
        for(Element element : elements){
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"被@Router注解修饰的类有：" + className);
            //拿到注解
            Route route = element.getAnnotation(Route.class);
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(route.group())
                    .addPath(route.value())
                    .addElement(element)
                    .build();
            //必须是Activity
            TypeMirror elementMirror = element.asType();
            if(typeTool.isSubtype(elementMirror,activityMirror)){
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            }else{
                throw new RuntimeException("@Route注解目前仅限于在Activity类上使用");
            }

            if(checkRouterPath(routerBean)){
                messager.printMessage(Diagnostic.Kind.NOTE,"校验通过" + routerBean.toString());
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());
                if(ProcessorUtils.isEmpty(routerBeans)){
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(),routerBeans);
                }else{
                    routerBeans.add(routerBean);
                }
            }else{
                messager.printMessage(Diagnostic.Kind.ERROR,"@Route注解未按照规定配置");
            }
        }

        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.ROUTER_API_PATH);
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.ROUTER_API_GROUP);
        try{
            createPathFile(pathType);
        }catch (IOException e){
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR,"生成Path模板时异常" + e.getMessage());
        }

        try{
            createGroupFile(groupType,pathType);
        }catch (IOException e){
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR,"生成Group模板时异常" + e.getMessage());
        }

        return true;
    }

    //校验
    private final boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup(); //  同学们，一定要记住： "app"   "order"   "personal"
        String path = bean.getPath();   //  同学们，一定要记住： "/app/MainActivity"   "/order/Order_MainActivity"   "/personal/Personal_MainActivity"

        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            // ERROR 故意去奔溃的
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        // finalGroup == app, personal, order

        // @ARouter注解中的group有赋值情况   用户传递进来时 order，  我截取出来的也必须是 order
        if (! ProcessorUtils.isEmpty(group) && ! group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup); // 赋值  order 添加进去了
        }

        return true;
    }

    /**
     * TODO　PATH　生成
     * @param pathType
     * @throws IOException
     */
    private final void createPathFile(TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        // Map<String, RouterBean>  返回值
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class), // Map
                ClassName.get(String.class), // Map<String,
                ClassName.get(RouterBean.class) // Map<String, RouterBean>
        );

        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            // 1.方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_MEHTOD_NAME) //方法名  getPathMap
                    .addAnnotation(Override.class) // 给方法上添加注解
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn);
            //  Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",  //$T 表示类
                    ClassName.get(Map.class), // Map
                    ClassName.get(String.class), // String
                    ClassName.get(RouterBean.class), // RouterBean
                    ProcessorConfig.PATH_VAR1,
                    ClassName.get(HashMap.class)
            );

            List<RouterBean> pathList = entry.getValue();

            /**
             * public class Router$$Path$$food implements RouterPath {
             *   @Override
             *   public Map<String, RouterBean> getPathMap() {
             *     Map<String, RouterBean> pathMap = new HashMap<>();
             *     pathMap.put("/food/FoodActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, FoodActivity.class, "/food/FoodActivity", "food"));
             *     return pathMap;
             *   }
             * }
             */
            for (RouterBean routerBean : pathList) {
                messager.printMessage(Diagnostic.Kind.NOTE,"routerBean = " + routerBean.toString());
                // 给方法添加代码
                methodBuilder.addStatement(
                        "$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        routerBean.getPath(), // "/app/MainActivity"
                        ClassName.get(RouterBean.class), // RouterBean
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        routerBean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) routerBean.getElement()), // MainActivity.class
                        routerBean.getPath(), // 路径名
                        routerBean.getGroup() // 组名
                );
            } // for end

            //  return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // 最终生成的类文件名  ARouter$$Path$$  + personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            //生成类
            TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName) // 类名
                    .addSuperinterface(ClassName.get(pathType)) // 实现ARouterLoadPath接口
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                    .build();

            // 生成 和 类 等等，结合一体,
            JavaFile.builder(aptPackage, typeSpec) // 类构建完成，接收包名和类作为参数
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

            // 告诉Group
            mAllGroupMap.put(entry.getKey(), finalClassName);

            // PATH 全部结束
        }
    }

    /**
     * TODO GROUP 生成
     * @param groupType
     * @param pathType
     * @throws IOException
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {

        // 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;

        // Map<String, Class<? extends ARouterPath>>  返回参数
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class), // Map
                ClassName.get(String.class), // Map<String,
                // 第二个参数：Class<? extends ARouterLoadPath>
                // 某某Class是否属于ARouterLoadPath接口的实现类
                // <>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))));

        // 方法架子
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                HashMap.class);

        // 方法内容配置
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            // groupMap.put("personal", ARouter$$Path$$personal.class);
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(),
                    // 类文件在指定包名下
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // 遍历之后：return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名   ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackage, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }
}
