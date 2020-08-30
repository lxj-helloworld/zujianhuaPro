package com.example.food;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.route_annotation.Route;
import com.example.router_api.Router;
import com.example.router_api.RouterManager;

@Route(value = "/food/FoodActivity")
public class FoodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        Log.d("FoodActivity",getIntent().getStringExtra("food"));
    }

    public void jumoToWaimai(View view) {
        RouterManager.getInstance().build("/waimai/WaiMaiActivity")
                .withString("waimai","这是我点的外卖")
                .navigation(this);
    }
}
