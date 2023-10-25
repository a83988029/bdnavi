package com.dcloud.jiangjie;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.common.BaiduMapSDKException;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNaviInitConfig;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FirstFrag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        try {
            // 在使用 SDK 各组间之前初始化 context 信息，传入
            // 获取ApplicationContext
            Context applicationContext = getApplicationContext();
            SDKInitializer.initialize(applicationContext);
        } catch (BaiduMapSDKException e) {
            Log.e(TAG, "onViewCreated: " + e.getMessage());
        }
        String sdcardRootPath = "/storage/emulated/0";
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // SD卡可用
            sdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG, "SD卡可用，sdcardRootPath: " + sdcardRootPath);
        } else {
            // SD卡不可用
            Log.d(TAG, "SD卡不可用，无法启动百度导航");
        }
        BNaviInitConfig config = new BNaviInitConfig.Builder()
                .sdcardRootPath(sdcardRootPath)
                .naviInitListener(new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int i, String s) {
                        Log.i(TAG, "onAuthResult: " + i + "s: " + s);
                    }

                    @Override
                    public void initStart() {
                        Log.d(TAG, "initStarted");
                    }

                    @Override
                    public void initSuccess() {
                        Log.d(TAG, "initSuccess");
                    }

                    @Override
                    public void initFailed(int i) {
                        Log.d(TAG, "initFailed: " + i);
                    }
                })
                .build();

        BaiduNaviManagerFactory.getBaiduNaviManager().init(MainActivity.this, config);

        // mainactivity没有layout.xml，这种情况下，需要在页面添加一个按钮
        // 点击按钮，触发导航
        Button buttonFirst = new Button(this);
        buttonFirst.setText("导航");
        setContentView(buttonFirst);
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BNRoutePlanNode sNode = new BNRoutePlanNode.Builder()
                        .latitude(32.10221)
                        .longitude(118.759698)
                        .name("滨江花园")
                        .description("滨江花园")
                        .build();
                BNRoutePlanNode eNode = new BNRoutePlanNode.Builder()
                        .latitude(32.098417)
                        .longitude(118.750068)
                        .name("大观天地")
                        .description("大观天地")
                        .build();
                List<BNRoutePlanNode> list = new ArrayList<>();
                list.add(sNode);
                list.add(eNode);
                BaiduNaviManagerFactory.getRoutePlanManager().routePlan(list,
                        IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                        null, new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                        Toast.makeText(MainActivity.this,
                                                "算路开始", Toast.LENGTH_SHORT).show();
                                        break;
                                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                        Toast.makeText(MainActivity.this,
                                                "算路成功", Toast.LENGTH_SHORT).show();
                                        break;
                                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                        Toast.makeText(MainActivity.this,
                                                "算路失败" + msg, Toast.LENGTH_SHORT).show();
                                        break;
                                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                        Toast.makeText(MainActivity.this,
                                                "算路成功准备进入导航", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this,
                                                MainActivity.class);
                                        startActivity(intent);
                                        break;
                                    default:
                                        // nothing
                                        break;
                                }
                            }
                        });
            }
        });
    }
}