package lazyhand.com.main.controller;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import lazyhand.com.main.model.db.AppDatabase;
import lazyhand.com.main.model.db.DeviceEntity;
import lazyhand.com.main.udp.UDPSocket;
import lazyhand.com.main.utils.DigitalTrans;

import static lazyhand.com.main.udp.UDPSocket.getBroadcast;


public class DevicesController extends AndroidViewModel {
    private static final String TAG = "DevicesController";
    public static final int CLIENT_PORT = 11111;
    private static boolean receiveing = false;
    private static List<String> devicesid;
    DatagramSocket udpClient;

    private AppDatabase db;

    private LiveData<List<DeviceEntity>> devicesList;
    private LiveData<List<DeviceEntity>> usedDevicesList;

   // private Context appContext;

    public DevicesController(Application application) {
        super(application);
        //appContext = application.getApplicationContext();


        db = AppDatabase.getDatabase(this.getApplication());
        pullUsedDevicesList(true);

        updateLocalIpaddr();
        udpListener();
    }

    public void addDevice(final DeviceEntity deviceEntity) {
        new Thread(()->{
            db.deviceDao().addDevice(deviceEntity);
            pullUsedDevicesList(true);
        }).start();
    }

    public LiveData<DeviceEntity> getDeviceByDeviceid(String id){
        return db.deviceDao().getDeviceByDeviceid(id);
    }

    public void getDevicesid(){
        devicesid = db.deviceDao().getDevicesid();
    }

    public LiveData<List<DeviceEntity>> getUsedDevicesList(){
        return usedDevicesList;
    }


    public void pullUsedDevicesList(boolean used){
        usedDevicesList = db.deviceDao().getDevicesByUsed(used);
        getDevicesid();
    }

    DeviceEntity getDeviceByDeviceidNolive(String id, String bssid){
        return db.deviceDao().getDeviceByDeviceidNolive(id, bssid);
    }

    private void updateLocalIpaddr(){
        new Thread(()-> {
            try {
                InetAddress targetAddress = InetAddress.getByName(getBroadcastAddress());
                DatagramPacket packet = new DatagramPacket("aaa".getBytes(), "aaa".length(), targetAddress, CLIENT_PORT);
                udpClient = new DatagramSocket(CLIENT_PORT);
                while (!receiveing && devicesid!=null) {
                    udpClient.send(packet);
                    Thread.sleep(60000 * 5);
                    // 数据发送事件
                    Log.d(TAG, "数据发送成功:aaa");
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }


    private void udpListener(){
        byte[] receiveByte = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveByte, 1024);
        new Thread(()->{
            while (true) {
                receiveing = false;
                //Log.d(TAG, "udpListener: ");

                try {
                    if(udpClient == null || devicesid==null){
                        Thread.sleep(1000);
                        continue;
                    }
                    udpClient.receive(receivePacket);
                    receiveing = true;

                    Log.d(TAG, "receive packet success...");
                } catch (IOException e) {
                    Log.e(TAG, "UDP数据包接收失败！线程停止");
                    udpClient.close();
                    udpClient = null;
                    e.printStackTrace();
                    return;
                }catch (InterruptedException e){

                }

                if (receivePacket.getLength() == 0) {
                    Log.e(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
                    continue;
                }

                String did = DigitalTrans.byte2hex(receivePacket.getData()).substring(0, 14);

                for (int i = 0; i < devicesid.size(); i++) {
                    if (devicesid.get(i).equals(did)) {


                        WifiManager wifiManager = (WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if(wifiManager != null && connectivityManager != null) {

                            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                            if (networkInfo != null && wifiManager.isWifiEnabled() && networkInfo.isConnected()) {



                                String bssid = wifiManager.getConnectionInfo().getBSSID();
                                DeviceEntity deviceEntity = getDeviceByDeviceidNolive(did, wifiManager.getConnectionInfo().getBSSID());
                                //Log.e(TAG, "bssid:" + bssid);

                                if(deviceEntity != null){
                                    String ipaddress = receivePacket.getAddress().getHostAddress();
                                    if(deviceEntity.ipaddr==null || !deviceEntity.ipaddr.equals(ipaddress)) {
                                        deviceEntity.ipaddr = ipaddress;
                                        addDevice(deviceEntity);
                                        Log.e(TAG, "hahahahahhahahahahahahhahahah");
                                    }
                                }
                            }
                        }
                    }
                }






                Log.e(TAG, "udpListener: "+did );
            }
        }).start();
    }










    private static String getBroadcastAddress() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() != null) {
                        return interfaceAddress.getBroadcast().toString().substring(1);
                    }
                }
            }
        }
        return null;
    }

}
