package com.example.router_api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.example.route_annotation.bean.RouterBean;

import java.lang.invoke.ConstantCallSite;
import java.math.RoundingMode;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 12:58 PM
 * 路由管理器 辅助完成交互通信
 * 详细流程
 **/
public class RouterManager {
    private static final String TAG = "RouterManager";

    private String group; //路由的组名
    private String path; //路由的路径

    private static RouterManager instance;

    //获取单例对象，因为需要被上层所有的业务组件共享
    public static RouterManager getInstance(){
        if(instance == null){
            synchronized (RouterManager.class){
                if(instance == null){
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }


    //性能 LRU缓存
    private LruCache<String,RouterGroup> groupLruCache;
    private LruCache<String,RouterPath> routerPathLruCache;

    private final static String FILE_GROUP_NAME = "Router$$Group$$";

    private RouterManager(){
        groupLruCache = new LruCache<>(100);
        routerPathLruCache = new LruCache<>(100);
    }

    public BundleManager build(String path){
        if(TextUtils.isEmpty(path) || !path.startsWith("/")){
            throw new IllegalArgumentException("Path路径错误，正确写法为：/main/MainActivity   path = " + path);
        }
        if(path.lastIndexOf("/") == 0){ //只写了一个斜杠，体现不出分组信息
            throw new IllegalArgumentException("Path路径格式错误，正确写法为：/main/MainActivity   path = " + path);
        }

        //截取组名
        String finalGroup = path.substring(1,path.indexOf("/",1));//如finalGroup为main

        if(TextUtils.isEmpty(finalGroup)){
            throw new IllegalArgumentException("无法截取组名信息，Path路径格式错误，正确写法为：/main/MainActivity");
        }
        //TODO 证明没有问题，没有抛出异常
        this.path = path;
        this.group = finalGroup;
        return new BundleManager();
    }

    //真正完成跳转
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context,BundleManager bundleManager){
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;
        Log.d(TAG,"groupClassName = " + groupClassName);

        try{
            //读取路由表Group类文件
            RouterGroup routerGroup = groupLruCache.get(group);
            if(null == routerGroup){
                //加载路由表Group类文件
                Class<?> aClass = Class.forName(groupClassName);
                //初始化类文件
                routerGroup = (RouterGroup)aClass.newInstance();
                //保存到缓存
                groupLruCache.put(group,routerGroup);
            }
            if(routerGroup.getGroupMap().isEmpty()){
                throw new RuntimeException("路由表报废了");
            }

            //读取路由Path类文件
            RouterPath routerPath = routerPathLruCache.get(path);
            if(null == routerPath){
                Class<? extends RouterPath> clazz = routerGroup.getGroupMap().get(group);
                //从map里面获取
                routerPath = clazz.newInstance();
                //保存到缓存
                routerPathLruCache.put(path,routerPath);
            }

            if(routerPath != null){
                if(routerPath.getPathMap().isEmpty()){
                    throw new RuntimeException("路由表报废了");
                }

                RouterBean routerBean = routerPath.getPathMap().get(path);
                if(routerBean != null){
                    switch (routerBean.getTypeEnum()){
                        case ACTIVITY:
                            Intent intent = new Intent(context,routerBean.getMyClass());
                            intent.putExtras(bundleManager.getBundle());
                            context.startActivity(intent);//携带参数
                            break;
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
