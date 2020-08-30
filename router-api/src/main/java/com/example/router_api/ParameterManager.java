package com.example.router_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 1:32 PM
 * 参数管理器，用于接收参数
 *
 **/
public class ParameterManager {

    private static ParameterManager instance;
    public static ParameterManager getInstance(){
        if(instance == null){
            synchronized (ParameterManager.class){
                if(instance == null){
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    //LRU缓存， key为类名，value为参数加载接口
    private LruCache<String,ParameterGet> cache;

    private ParameterManager(){
        cache = new LruCache<>(100);
    }


    static final String FILE_SUFFIX_NAME = "$$Parameter"; //为了效果MainActivity+$$Parameter

    //使用者只需要使用这一个方法，就可以进行参数的接收。
    public void loadParameter(Activity activity){
        String className = activity.getClass().getName();//如MainActivity
        ParameterGet parameterGet = cache.get(className);
        if(null == parameterGet){ //缓存里没有对应的数据
            try {
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterGet = (ParameterGet)aClass.newInstance();
                cache.put(className,parameterGet);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        parameterGet.getPatameter(activity);
    }






}
