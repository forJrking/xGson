package com.forjrking.gsonplugin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fojrking.gson.GsonUtils;
import com.fojrking.gsonplugin.R;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    TextView tvTo, tvFrom;
    Button tv1, tv2, tv3, tv4, tv5, tv6;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//    MyGsonSyntaxErrorListener.start();
        tvTo = findViewById(R.id.tv_tojson);
        tvFrom = findViewById(R.id.tv_fromjson);
        tv1 = findViewById(R.id.bt_json_1);
        tv2 = findViewById(R.id.bt_json_2);
        tv3 = findViewById(R.id.bt_json_3);
        tv4 = findViewById(R.id.bt_json_4);
        tv5 = findViewById(R.id.bt_json_5);
        tv6 = findViewById(R.id.bt_json_6);


        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TestBean bo = getBo();
                    String s1 = new Gson().toJson(bo);
                    tvTo.setText("toJson: " + s1);
                    TestBean test2Bean = new Gson().fromJson(s1, TestBean.class);
                    tvFrom.setText("fromJson:" + new Gson().toJson(test2Bean));
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    tvTo.setText(e.getMessage());
                }
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String s = getS2();
                    TestBean test2Bean = new Gson().fromJson(s, TestBean.class);
                    tvFrom.setText("fromJson:" + s);
                    String s1 = new Gson().toJson(test2Bean);
                    tvTo.setText("toJson: " + s1);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    tvFrom.setText(e.getMessage());
                }
            }
        });

        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TestBean bo = getBo();
                    String s1 = new Gson().toJson(bo);
                    tvTo.setText("toJson: " + s1);
                    TestBean test2Bean = GsonUtils.fromJson(s1, TestBean.class);
                    tvFrom.setText("fromJson:" + GsonUtils.toJson(test2Bean));
                } catch (Exception e) {
                    e.printStackTrace();
                    tvTo.setText(e.getMessage());
                }
            }
        });
        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String s = getS2();
                    TestBean test2Bean = GsonUtils.fromJson(s, TestBean.class);
                    tvFrom.setText("fromJson:" + s);
                    String s1 = GsonUtils.toJson(test2Bean);
                    tvTo.setText("toJson: " + s1);
                } catch (Exception e) {
                    e.printStackTrace();
                    tvFrom.setText(e.getMessage());
                }
            }
        });

        tv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               test(new Gson(),1);
            }
        });

        tv6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test(GsonUtils.getGson(),2);
            }
        });
    }

    private void test(Gson gson, int type) {
        try {
            long start = System.currentTimeMillis();
//            序列化
            TestBean bo = getBo();
            for (int i = 1500; i > 0; i--) {
                String toJson = gson.toJson(bo);
            }
            tvTo.setText("toJson: " + (System.currentTimeMillis() - start) + " ms");

            start = System.currentTimeMillis();
            String toJson = getS1();
            for (int i = 1500; i > 0; i--) {
                TestBean testBean = gson.fromJson(toJson, TestBean.class);
            }
//            解析
            tvFrom.setText("fromJsonT:" + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
            tvFrom.setText(e.getMessage());
        }
    }

    private String getS1(){
        return "{\"array\":[\"array1\",\"array2\"],\"bean\":{\"int2\":20,\"list2\":[\"l2--2\",\"l2--1\"],\"mStrings\":[]},\"doubleB\":11.0,\"floatF\":12.001,\"intA\":10,\"isOk\":true,\"list\":[\"list1\",\"list2\"],\"longL\":100000,\"map\":{\"map2\":\"map2\",\"map1\":\"map1\"},\"str\":\"str\"}";
    }


    private String getS2() {
        // DES: 正常数据
//    return "{\"array\":[\"a1\",\"a2\"],\"bean\":{\"int2\":20,\"list2\":[\"l2-2\",\"l2-1\"]},\"doubleB\":11.0,\"floatF\":12.001,\"intA\":10,\"isOk\":false,\"list\":[\"list1\",\"list2\"],\"longL\":200000,\"map\":{\"map2\":\"map2\",\"map1\":\"map1\"},\"str\":\"str\"}";
        // DES: 数字全部异常
//    return "{\"array\":[\"a1\",\"a2\"],\"bean\":{\"int2\":\"-2\",\"list2\":[\"l2-2\",\"l2-1\"]},\"doubleB\":12.0008f,\"floatF\":\"12\",\"intA\":\"\",\"isOk\":false,\"list\":[\"list1\",\"list2\"],\"longL\":-100L,\"map\":{\"map2\":\"map2\",\"map1\":\"map1\"},\"str\":\"str\"}";
        // DES: 字符串异常
//        return "{\"int2\":\"2L\",\"list2\":[],\"mLong\":\"测试文字WEBssldndjj难度饕餮\"}";
        return "{\"array\":[],\"doubleB\":11.0d,\"floatF\":null,\"intA\":10,\"isOk\":1,\"bean\":null,\"list\":true,\"longL\":100000,\"map\":{\"map2\":\"map2\",\"map1\":\"map1\"},\"str\":\"str\"}";
    }

    private TestBean getBo() {
        TestBean innerBean = new TestBean();
        innerBean.intA = 10;
        innerBean.doubleB = 11.0D;
        innerBean.floatF = 12.001F;
        innerBean.longL = 100000L;
        innerBean.str = "str";
        innerBean.isOk = true;
        innerBean.array = new String[2];
        innerBean.array[0] = "array1";
        innerBean.array[1] = "array2";
        innerBean.list = new ArrayList<>();
        innerBean.list.add("list1");
        innerBean.list.add("list2");
        innerBean.map = new HashMap<>();
        innerBean.map.put("map1", "map1");
        innerBean.map.put("map2", "map2");

        Test2Bean testBean = new Test2Bean();
        testBean.int2 = 20;
        testBean.list2 = new ArrayList<>();
        testBean.list2.add("l2--2");
        testBean.list2.add("l2--1");
        innerBean.bean = testBean;

        return innerBean;
    }
}
