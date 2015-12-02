package com.kelvinyang.sunsimulator;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private static String appName;

    private static final String USERNAME = "2ffd5ca420eaeb0ff4650cc2328ac97";
    private static final String IP = "192.168.1.112";
    private PHHueSDK phHueSDK;
    private PHSDKListener phsdkListener;
    private List<PHLight> lights;
    private PHLight sun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initialize();
    }

    private void initialize() {
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
                changeLightState();
                break;

        }
    }

    private void changeLightState() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        PHLightState phLightState = new PHLightState();
        phLightState.setCt(500);
        phLightState.setBrightness(1);

        bridge.updateLightState(sun,phLightState);
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
            if ("Sun".equals(name)){
                this.sun = light;
                enableButton();
            }
        }
    }

    private void enableButton(){
        Button button = (Button)findViewById(R.id.btn);
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
