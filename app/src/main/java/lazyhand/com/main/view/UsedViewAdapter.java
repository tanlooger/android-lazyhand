package lazyhand.com.main.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


//import cn.bingoogolapple.badgeview.annotation.BGABadge;
import lazyhand.com.main.R;
import lazyhand.com.main.databinding.FragmentUsedItemBinding;
import lazyhand.com.main.model.db.DeviceEntity;
import lazyhand.com.main.udp.UDPSocket;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DeviceEntity} and makes a call to the
 *
 * TODO: Replace the implementation with code for your data type.

@BGABadge({
        View.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeView，不想用这个类的话就删了这一行
        ImageView.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeImageView，不想用这个类的话就删了这一行
        TextView.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFloatingTextView，不想用这个类的话就删了这一行
        RadioButton.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeRadioButton，不想用这个类的话就删了这一行
        LinearLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeLinearLayout，不想用这个类的话就删了这一行
        FrameLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFrameLayout，不想用这个类的话就删了这一行
        RelativeLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeRelativeLayout，不想用这个类的话就删了这一行
        FloatingActionButton.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFloatingActionButton，不想用这个类的话就删了这一行

})
 */
public class UsedViewAdapter extends RecyclerView.Adapter<UsedItemViewHolder> {

    private final List<DeviceEntity> mValues;

    private int layoutId;
    //private final OnListFragmentInteractionListener mListener;

    private Context context;

    private FragmentUsedItemBinding binding;

    private int networkId;

    UsedViewAdapter(Context deviceFragmentContext, List<DeviceEntity> items, int layoutId) {
        context = deviceFragmentContext;
        mValues = items;
        this.layoutId = layoutId;

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(wifiManager != null && connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && wifiManager.isWifiEnabled() && networkInfo.isConnected()) {
                networkId = wifiManager.getConnectionInfo().getNetworkId();
            }
        }
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    @NonNull
    public UsedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_used_item, parent, false);


        return new UsedItemViewHolder(binding.getRoot(), context);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsedItemViewHolder holder, int position) {
        DeviceEntity deviceEntity = mValues.get(position);
        holder.setItem(deviceEntity);
        binding.setDeviceEntity(deviceEntity);
    }





}



