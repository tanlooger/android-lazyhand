package lazyhand.com.main.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import lazyhand.com.main.R;
import lazyhand.com.main.controller.DevicesController;
import lazyhand.com.main.controller.WifiController;
import lazyhand.com.main.model.db.DeviceEntity;
import lazyhand.com.main.model.db.WifiEntity;
import lazyhand.com.main.utils.MessageEvent;
import lazyhand.com.main.utils.WifiAdmin;

public class WifiAddActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{
    WifiManager wifiManager;
    String ssid="";
    String password="";
    WifiController wifiController;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_add);
        RadioGroup radioGroup = findViewById(R.id.wifi_add_radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        progressDialog=new ProgressDialog(this);
        wifiController = ViewModelProviders.of(this).get(WifiController.class);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(wifiManager!=null){
            wifiManager.setWifiEnabled(true);
            List<ScanResult> wifiList = wifiManager.getScanResults();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            for (int i = 0; i < wifiList.size(); i++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(wifiList.get(i).SSID);
                radioGroup.addView(radioButton);
                if(wifiList.get(i).BSSID.equals(wifiInfo.getBSSID())){
                    radioGroup.check(radioButton.getId());
                }
            }
        }

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton tempRadioButton = (RadioButton)findViewById(checkedId);

        TextView ssidTextView = findViewById(R.id.wifi_add_ssid);
        ssidTextView.setText(tempRadioButton.getText());
    }

    public void onWifiSave(View view){
        progressDialog.show();
        Toast.makeText(WifiAddActivity.this, "item点击:" , Toast.LENGTH_SHORT).show();
        wifiManager.disconnect();
        WifiAdmin wifiAdmin = new WifiAdmin(wifiManager);
        ssid = ((TextView)findViewById(R.id.wifi_add_ssid)).getText().toString();
        password = ((TextView)findViewById(R.id.wifi_add_password)).getText().toString();

        int wcnetworkid = wifiManager.addNetwork(wifiAdmin.createWifiInfo(ssid, password));

        wifiManager.enableNetwork(wcnetworkid,true);
        wifiManager.reconnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiReconnected1(MessageEvent event) {
        Log.e("kaka", "onWifiReconnected: ");
        if(event.message.equals("WIFI_PASSWORD_ERROR")) {
            Toast.makeText(WifiAddActivity.this, "密码错误:" , Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();



        WifiEntity we = new WifiEntity();
        we.networkid = wifiInfo.getNetworkId();
        we.mac_address = wifiInfo.getMacAddress();
        we.bssid = wifiInfo.getBSSID();
        we.ssid = ssid;
        we.ipaddr = wifiInfo.getIpAddress();
        we.password = password;
        wifiController.addWifi(we);
        Toast.makeText(WifiAddActivity.this, "保存成功:" , Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //Log.d("s", "onStart: ");
        //setKeyguardEnable(true);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        progressDialog.dismiss();

        //Log.d("s", "onStop: ");
        //setKeyguardEnable(false);
        //progressDialog.dismiss();
    }
}
