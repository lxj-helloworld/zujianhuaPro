package com.example.waimai;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.route_annotation.Route;
import com.example.router_api.Router;
import com.example.router_api.RouterManager;

@Route(value = "/waimai/WaiMaiActivity")
public class WaiMaiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wai_mai);
        Log.d("FoodActivity",getIntent().getStringExtra("waimai"));
    }

    public void jumpToFood(View view) {
        RouterManager.getInstance().build("/food/FoodActivity")
                .withString("food","这是我点的美食")
                .navigation(this);
    }
}
