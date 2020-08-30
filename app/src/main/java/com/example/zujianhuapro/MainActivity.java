package com.example.zujianhuapro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.food.FoodActivity;
import com.example.route_annotation.Route;
import com.example.router_api.Router;
import com.example.router_api.RouterManager;
import com.example.waimai.WaiMaiActivity;

@Route(value = "/main/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void jumpToFood(View view) {
        RouterManager.getInstance().build("/food/FoodActivity")
                .withString("food","美食")
                .navigation(this);
    }
}
