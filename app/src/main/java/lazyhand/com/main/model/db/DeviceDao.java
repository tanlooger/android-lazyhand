package lazyhand.com.main.model.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface DeviceDao {
    @Query("SELECT deviceid from device")
    List<String> getDevicesid();

    @Query("SELECT * from device WHERE used=:id  ORDER BY update_time ASC")
    LiveData<List<DeviceEntity>> getDevicesByUsed(boolean id);

    @Query("SELECT * from device WHERE deviceid=:id")
    LiveData<DeviceEntity> getDeviceByDeviceid(String id);

    @Query("SELECT * from device WHERE deviceid=:id and bssid=:bssid")
    DeviceEntity getDeviceByDeviceidNolive(String id, String bssid);

    @Insert(onConflict = REPLACE)
    void addDevice(DeviceEntity mDevice);
}
