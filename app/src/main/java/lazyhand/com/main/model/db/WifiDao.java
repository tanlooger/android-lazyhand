package lazyhand.com.main.model.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface WifiDao {
    @Query("SELECT * from wifi WHERE networkid=:id")
    LiveData<WifiEntity> getWifiByNetworkid(int id);

    @Query("SELECT * from wifi")
    public LiveData<List<WifiEntity>> getAllWifi();

    @Insert(onConflict = REPLACE)
    void addWifi(WifiEntity mWifi);

    @Delete
    void deleteWifi(WifiEntity mWifi);
}
