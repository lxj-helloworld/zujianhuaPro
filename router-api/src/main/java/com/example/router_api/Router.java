package com.example.router_api;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 9:31 AM
 * 路由表，需要被所有的组件依赖，所以需要使用单例模式
 **/
public class Router {
    private static Router mRouter;
    private static Context mContext;




    //路由表
    private static Map<String,Class<? extends Activity>> routers = new HashMap<>();

    public void init(Application application){
        mContext = application;
    }

    public static Router getInstance(){
        if(mRouter == null){
            synchronized (Router.class){
                if(mRouter == null){
                    mRouter = new Router();
                }
            }
        }
        return mRouter;
    }

    //向路由表中注册一个Activity
    public void register(String path,Class<? extends Activity> cls){
        routers.put(path,cls);
    }

    //启动路由表中的一个Activity
    public void startActivity(String path){
        Class<? extends Activity> cls = routers.get(path);
        if(cls == null){
            return;
        }
        Intent intent = new Intent(mContext,cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
