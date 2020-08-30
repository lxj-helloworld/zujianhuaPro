package com.example.router_api;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.invoke.ConstantCallSite;

/**
 * 项目名称 zujianhuaPro
 * 创建人 xiaojinli
 * 创建时间 2020/8/29 1:15 PM
 * 跳转时 用于参数传递
 **/
public class BundleManager {
    //携带的参数保存在这里，用Intent传输
    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return bundle;
    }

    //对外界提供的方法，录入参数
    //存入String类型参数
    public BundleManager withString(@NonNull String key, @Nullable String value){
        bundle.putString(key,value);
        return this;
    }
    //存入布尔类型参数
    public BundleManager withBoolean(@NonNull String key,@Nullable Boolean value){
        bundle.putBoolean(key,value);
        return this;
    }

    //存在int类型参数
    public BundleManager withInt(@NonNull String key,@Nullable int value){
        bundle.putInt(key,value);
        return this;
    }

    //大招
    public BundleManager withBundle(Bundle bundle){
        this.bundle = bundle;
        return this;
    }

    public Object navigation(Context context){
        //单一原则，把自己的行为交给了路由管理器
        return RouterManager.getInstance().navigation(context,this);
    }






}
