package lazyhand.com.main.model.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Entity(tableName = "wifi")
public class WifiEntity {
    @PrimaryKey
    @NonNull
    public int networkid;
    public String mac_address;

    @NonNull
    public String bssid;
    public String ssid;
    public int ipaddr;

    @Nullable
    public String password;
}
