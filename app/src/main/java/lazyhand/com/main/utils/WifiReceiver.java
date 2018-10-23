package lazyhand.com.main.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class WifiReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("ACTION: "+intent.getAction());

        if(intent.getAction().equals("android.net.wifi.supplicant.STATE_CHANGE")){
            int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                System.out.println("ERROR_AUTHENTICATING");
                EventBus.getDefault().post(new MessageEvent("WIFI_PASSWORD_ERROR"));
            }
        }else
        if(intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            switch (info.getDetailedState()) {
                case AUTHENTICATING:
                    System.out.println("AUTHENTICATING");
                    break;
                case BLOCKED:
                    System.out.println("BLOCKED");
                    break;
                case CAPTIVE_PORTAL_CHECK:
                    System.out.println("CAPTIVE_PORTAL_CHECK");
                    break;
                case CONNECTED:
                    System.out.println("CONNECTED");
                    EventBus.getDefault().post(new MessageEvent("WIFI_CONNECTED"));
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    break;
                case CONNECTING:
                    System.out.println("CONNECTING");
                    break;
                case DISCONNECTED:
                    System.out.println("DISCONNECTED");
                    break;
                case DISCONNECTING:
                    System.out.println("DISCONNECTING");
                    break;
                case FAILED:
                    System.out.println("FAILED");
                    break;
                case IDLE:
                    System.out.println("IDLE");
                    break;
                case OBTAINING_IPADDR:
                    System.out.println("OBTAINING_IPADDR");
                    break;
                case SCANNING:
                    System.out.println("SCANNING");
                    break;
                case SUSPENDED:
                    System.out.println("SUSPENDED");
                    break;
                case VERIFYING_POOR_LINK:
                    System.out.println("VERIFYING_POOR_LINK");
                    break;
            }
        }
    }


    //@Override
    public void _onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if(intent.getAction()!=null && intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
            //signal strength changed
            System.out.println("signal strength changed");
        }else
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
            System.out.println("网络状态改变");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                System.out.println("wifi网络连接断开");
            } else
            if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称
                    System.out.println("连接到网络 " + wifiInfo.getSSID());
                    EventBus.getDefault().post(new MessageEvent(""));
                }
            }else
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
            }

        } else
        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

            if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                System.out.println("系统关闭wifi");
            }
            else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                System.out.println("系统开启wifi");
            }
        }else
        if(intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
            int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                System.out.println("WIFI密码错误");
                //密码错误时  清空networkId的相关信息
                Toast.makeText(context, "WIFI密码错误", Toast.LENGTH_SHORT).show();
            }
        }
    }



}

