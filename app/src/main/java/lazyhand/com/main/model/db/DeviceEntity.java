package lazyhand.com.main.model.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Entity(tableName = "device")
public class DeviceEntity {
    @PrimaryKey
    @NonNull
    public String deviceid;
    public String hashcode;
    public String name;

    @Nullable
    public int networkid;
    public String mac_address;
    public String bssid;


    public String ipaddr;
    public boolean used=false;
    public boolean activited=false;
    public String ownerkey;
    public long update_time;
}
