package lazyhand.com.main.controller;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import lazyhand.com.main.model.db.AppDatabase;
import lazyhand.com.main.model.db.WifiEntity;


public class WifiController extends AndroidViewModel {
    private AppDatabase db;


    public WifiController(Application application) {

        super(application);

        db = AppDatabase.getDatabase(this.getApplication());
    }

    public void addWifi(final WifiEntity wifiEntity)
    {
        new Thread(()->{
            db.wifiDao().addWifi(wifiEntity);
        }).start();
    }

    public LiveData<WifiEntity> getWifiByNetworkid(int networkid){
        return db.wifiDao().getWifiByNetworkid(networkid);
    }

    public void deleteWifi(WifiEntity mWifi){
        db.wifiDao().deleteWifi(mWifi);
    }

    public LiveData<List<WifiEntity>> getAllWifi(){
        return db.wifiDao().getAllWifi();
    }

}
