package com.kelvinyang.sunsimulator.activity;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kelvinyang.sunsimulator.R;
import com.kelvinyang.sunsimulator.mapping.TimeMapper;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends AppCompatActivity {
    private static String appName;

    private static final String USERNAME = "2ffd5ca420eaeb0ff4650cc2328ac97";
    private static final String IP = "192.168.1.112";
    private PHHueSDK phHueSDK;
    private PHSDKListener phsdkListener;
    private List<PHLight> lights;
    private PHLight sun;

    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initialize();
    }

    private void initialize() {
        time = (TextView) findViewById(R.id.tv_time);

        appName = getResources().getString(R.string.app_name);
        phHueSDK = PHHueSDK.getInstance();
        phHueSDK.setAppName(appName);
        phHueSDK.setDeviceName(Build.MODEL);
        //startBridgeSearch();
        setLitsener();
        connectKnownBridge();
        // getLights();
        //findSun();
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn:
                changeLightState(1, 500);
                break;
            case R.id.btn_time_travel:
                startTimeTravel();
                disableTimerTravelButton();
                break;

        }
    }

    private void disableTimerTravelButton() {
        Button b = (Button) findViewById(R.id.btn_time_travel);
        b.setEnabled(false);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            calendar.add(Calendar.MINUTE, 10);
            uiUpdateTime();
            changeLightState(getBrightness(), getCT());
        }
    };

    private int getCT() {
        // f(x) = 1.05 sin(0.261799x -1.5707963)
        TimeMapper timeMapper = new TimeMapper(1.1, 0.261799, -1.5707963);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        double ctFactor = timeMapper.map(hour, minute);
        double ctMax = 500;
        double ctMin = 153;
        double delta = ctMax - ctMin;

        Double ct = ctMax - delta * ctFactor;

        return ct.intValue();
    }

    private int getBrightness() {
        // f(x) = 1.05 sin(0.261799x -1.5707963)
        TimeMapper timeMapper = new TimeMapper(1.05, 0.261799, -1.5707963);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        double brightnessFactor = timeMapper.map(hour, minute);

        if (brightnessFactor < 0) {
            return 0;
        }
        if (brightnessFactor > 1) {
            brightnessFactor = 1;
        }

        int maxBrightness = 254;
        Double brightness = maxBrightness * brightnessFactor;
        return brightness.intValue();
    }

    private Calendar calendar;

    private void startTimeTravel() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);

        uiUpdateTime();
        startTimer();
    }

    private void startTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

    private void uiUpdateTime() {
        String s = simpleDateFormat.format(calendar.getTime());
        this.time.setText(s);
    }

    private void changeLightState(int brightness, int ct) {

        PHBridge bridge = phHueSDK.getSelectedBridge();
        PHLightState phLightState = new PHLightState();
        phLightState.setCt(ct);
        phLightState.setBrightness(brightness);

        if (brightness > 0) {
            phLightState.setOn(true);
        } else {
            phLightState.setOn(false);
        }
        bridge.updateLightState(sun, phLightState);
    }

    private void connectKnownBridge() {
        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(IP);
        accessPoint.setUsername(USERNAME);
        phHueSDK.connect(accessPoint);
    }

    private void startBridgeSearch() {
        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }

    private void getLights() {
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        lights = cache.getAllLights();
    }

    private void findSun() {
        if (lights == null) {
            return;
        }

        for (PHLight light : lights) {
            String name = light.getName();
            if ("Sun".equals(name)) {
                this.sun = light;
                enableButton();
            }
        }
    }

    private void enableButton() {
        Button button = (Button) findViewById(R.id.btn);
        button.setEnabled(true);
    }

    private void setLitsener() {
        phsdkListener = new PHSDKListener() {
            @Override
            public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
                int i = 1;
                getLights();
                findSun();


            }

            @Override
            public void onBridgeConnected(PHBridge phBridge, String s) {
                phHueSDK.setSelectedBridge(phBridge);
                phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            }

            @Override
            public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
                phHueSDK.startPushlinkAuthentication(phAccessPoint);
                // Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
                // you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
            }

            @Override
            public void onAccessPointsFound(List<PHAccessPoint> list) {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onConnectionResumed(PHBridge phBridge) {

            }

            @Override
            public void onConnectionLost(PHAccessPoint phAccessPoint) {

            }

            @Override
            public void onParsingErrors(List<PHHueParsingError> list) {

            }
        };
        phHueSDK.getNotificationManager().registerSDKListener(phsdkListener);
    }
}
