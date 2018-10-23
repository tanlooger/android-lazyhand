package lazyhand.com.main.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lazyhand.com.main.R;
import lazyhand.com.main.controller.DevicesController;
import lazyhand.com.main.model.db.DeviceEntity;
import lazyhand.com.main.model.db.WifiEntity;
import lazyhand.com.main.controller.WifiController;
import lazyhand.com.main.utils.DigitalTrans;
import lazyhand.com.main.utils.MessageEvent;
import lazyhand.com.main.utils.WifiAdmin;

public class NfcActivity extends AppCompatActivity {

    Tag tag;
    private  boolean nfcLock = false;
    private  boolean isOnSave = false;
    private MifareUltralight mifare;

    private String ssid;
    private String password;

    private WifiManager wifiManager;

    ProgressDialog progressDialog;

    TextView nfcDeviceid;
    TextView nfcSsid;
    EditText nfcPassword;

    Toast toast;

    WifiController wifiController;

    WifiInfo info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_nfc);

        nfcDeviceid = findViewById(R.id.nfc_deviceid);
        nfcSsid = findViewById(R.id.nfc_ssid);
        nfcPassword = findViewById(R.id.nfc_password);

        wifiController = ViewModelProviders.of(this).get(WifiController.class);

        Intent intent = getIntent();
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        mifare = MifareUltralight.get(tag);
        progressDialog=new ProgressDialog(this);

        //Log.e("e", readTag(44));

        byte[] nfcUid = read4page(0);
        Log.e("code", "onWifiReconnected: "+DigitalTrans.byte2hex(nfcUid).substring(0, 14));
        nfcDeviceid.setText(DigitalTrans.byte2hex(read4page(0)).substring(0, 14));


        toast = Toast.makeText(getApplicationContext(), "点击按钮", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -280);
        toast.setDuration(Toast.LENGTH_LONG);

        checkTag();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(wifiManager != null && connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo!=null && wifiManager.isWifiEnabled() && networkInfo.isConnected()) {
                //int wifiState = wifiMgr.getWifiState();
                info = wifiManager.getConnectionInfo();

                LiveData<WifiEntity> we = wifiController.getWifiByNetworkid(info.getNetworkId());
                we.observe(this, (WifiEntity wifiEntity)->{

                    if(wifiEntity!=null){
                        //Log.e("e", wifiEntity.password);

                        nfcSsid.setText(wifiEntity.ssid);
                        nfcPassword.setText(wifiEntity.password);
                        ssid = wifiEntity.ssid;
                        //password = wifiEntity.password;
                    }else {
                        ssid = info.getSSID();
                        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                            ssid = ssid.substring(1, ssid.length() - 1);
                        }
                        nfcSsid.setText(ssid);
                    }
                });


                nfcPassword.setEnabled(true);
                ((Button)findViewById(R.id.nfc_save)).setEnabled(true);

            }
        }

    }


    //44
    public void onSwitchClick(View view) {
        Button button = findViewById(R.id.nfc_switch);
        String switchStatus = button.getTag().toString();
        try{
            nfcLock = true;
            if(mifare.isConnected())mifare.close();
            while(!mifare.isConnected())
            mifare.connect();
            byte[] b = {'o', '\0', '\0', '\0'};
            if(switchStatus.equals("on")) {
                b[1] = 'f';b[2] = 'f';
                button.setTag("off");
                mifare.writePage(44, b);
                runOnUiThread(()->{
                    button.setBackgroundResource(R.drawable.gray_circle);
                });
            }else {
                b[1] = 'n';
                button.setTag("on");
                mifare.writePage(44, b);
                runOnUiThread(()->{
                    button.setBackgroundResource(R.drawable.green_circle);
                });
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                nfcLock = false;
                mifare.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }


    public void onSave(View view) {
        progressDialog.show();

        wifiManager.disconnect();
        password = nfcPassword.getText().toString();

        WifiAdmin wifiAdmin = new WifiAdmin(wifiManager);
        int wcnetworkid = wifiManager.addNetwork(wifiAdmin.createWifiInfo(ssid, password));

        wifiManager.enableNetwork(wcnetworkid,true);
        isOnSave = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiReconnected(MessageEvent event) {
        if(ssid == null || !isOnSave)return;
        if(event.message.equals("WIFI_PASSWORD_ERROR")){
            try {
                if(mifare.isConnected())mifare.close();
                nfcLock = false;
            }catch (IOException e){
                e.printStackTrace();
            }
            toast.setText("密码错误");
            toast.show();
            progressDialog.dismiss();
            return;
        }
        nfcLock = true;

        writeWifi2tag();

        String hashCode = readTag(60);
        Log.e("code", "onWifiReconnected: "+hashCode);





        WifiEntity we = new WifiEntity();
        we.networkid = info.getNetworkId();
        we.mac_address = info.getMacAddress();
        we.bssid = info.getBSSID();
        we.ssid = ssid;
        we.ipaddr = info.getIpAddress();
        we.password = password;
        wifiController.addWifi(we);

        DevicesController devicesController = ViewModelProviders.of(this).get(DevicesController.class);
        DeviceEntity de = new DeviceEntity();
        de.deviceid = nfcDeviceid.getText().toString();
        de.name = de.deviceid;
        de.hashcode = readTag(60);
        de.networkid = we.networkid;
        de.mac_address = we.mac_address;
        de.bssid = we.bssid;
        de.used = true;
        de.update_time = System.currentTimeMillis();
        devicesController.addDevice(de);



        toast.setText("保存成功");
        toast.show();

        try {
            if(mifare.isConnected())mifare.close();
            nfcLock = false;
        }catch (IOException e){
            e.printStackTrace();
        }
        progressDialog.dismiss();
        isOnSave = false;
    }

    //12---27   28---43
    public void writeWifi2tag() {
        try {
            nfcLock = true;
            while (!mifare.isConnected()) mifare.connect();

            byte[] bb = new byte[ssid.length()+20];
            byte f = '\0';
            java.util.Arrays.fill(bb, f);
            for(int i=0; i<ssid.length(); i++){
                bb[i] = ssid.getBytes(Charset.forName("US-ASCII"))[i];
            }
            for(int i=0; i<bb.length/4-4; i++){
                byte[] b = {bb[i*4+0], bb[i*4+1], bb[i*4+2], bb[i*4+3]};
                mifare.writePage(i + 12, b);
            }

            java.util.Arrays.fill(bb, f);
            for(int i=0; i<password.length(); i++){
                bb[i] = password.getBytes(Charset.forName("US-ASCII"))[i];
            }
            for(int i=0; i<bb.length/4-4; i++){
                byte[] b = {bb[i*4+0], bb[i*4+1], bb[i*4+2], bb[i*4+3]};
                mifare.writePage(i + 28, b);
            }

            //mifare.writePage(12, "abcd".getBytes(Charset.forName("US-ASCII")));

        } catch (IOException e) {
            Log.d("a", "IOException while closing MifareUltralight...", e);
        } catch (IllegalStateException e) {
            Log.d("c", "IllegalStateException", e);
            toast.setText("连接失败，请将手机贴近设备后重试");
            toast.show();
            finish();
        } finally {
            try {
                nfcLock = false;
                mifare.close();
            } catch (IOException e) {
                Log.e("b", "IOException while closing MifareUltralight...", e);
            }
        }
    }


    public byte[] read4page(int offset) {
        String r="";
        try {
            nfcLock = true;
            if(mifare.isConnected())mifare.close();
            mifare.connect();
            return mifare.readPages(offset);
        } catch (IOException e) {
            Log.e("c", "IOException while writing MifareUltralight message...", e);
        } catch (IllegalStateException e) {
            Log.e("c", "IllegalStateException", e);
            toast.setText("连接失败，请将手机贴近设备后重试");
            toast.show();
            finish();
        } finally {
            if (mifare != null) {
                try {
                    nfcLock = false;
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e("d", "Error closing tag...", e);
                }
            }
        }
        return null;
    }


    public String readTag(int offset) {
        String r="";
        try {
            nfcLock = true;
            if(mifare.isConnected())mifare.close();
            mifare.connect();
            for(int i=0; i<4; i++) {
                byte[] payload = mifare.readPages(offset + 4*i);
                //return new String(payload, Charset.forName("US-ASCII"));
                r += (new String(payload, Charset.forName("UTF-8"))).trim();
            }
            return r;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("c", "IOException while writing MifareUltralight message...", e);
        } catch (IllegalStateException e) {
            Log.e("c", "IllegalStateException", e);
            toast.setText("连接失败，请将手机贴近设备后重试");
            toast.show();
            finish();
        } finally {
            if (mifare != null) {
                try {
                    nfcLock = false;
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e("d", "Error closing tag...", e);
                }
            }
        }
        return null;
    }

    private void checkTag(){
        new Thread(()->{
            while (true) {
                try {
                    while (nfcLock){
                        Thread.sleep(500);
                    }
                    if(mifare.isConnected()) mifare.close();

                    mifare.connect();
                    //mifare.readPages(44);
                    Thread.sleep(250);
                } catch (IOException e) {
                    finish();
                    break;
                } catch (InterruptedException e) {
                    finish();
                    break;
                }finally {
                    try {
                        mifare.close();
                    } catch (Exception e) {
                        finish();
                    }
                }
            }
        }).start();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //Log.d("s", "onStart: ");
        setKeyguardEnable(true);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        //Log.d("s", "onStop: ");
        setKeyguardEnable(false);
        progressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private void setKeyguardEnable(boolean enable) {
        //disable
        if (!enable) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            return;
        }

        //enable
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //Log.d("keyguard", "setKeyguardEnable: ");
    }

    private void getWifiPassword() {
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/wpa_supplicant.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            return;
        } finally {

            if (TextUtils.isEmpty(wifiConf.toString())) {
                Toast.makeText(getApplicationContext(), "请先获取Root权限...", Toast.LENGTH_LONG).show();
            }

            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }


        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
        Matcher networkMatcher = network.matcher(wifiConf.toString());
        WifiInfo wifiInfo;
        while (networkMatcher.find()) {
            String networkBlock = networkMatcher.group();
            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
            Matcher ssidMatcher = ssid.matcher(networkBlock);
            if (ssidMatcher.find()) {
                if(ssidMatcher.group(1).equals(password)){
                    Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                    Matcher pskMatcher = psk.matcher(networkBlock);
                    if (pskMatcher.find()) {
                        password = pskMatcher.group(1);
                        runOnUiThread(()->{
                            nfcPassword.setText(password);
                        });
                    } else {
                    }
                    return;
                }
            }
        }
    }
}
