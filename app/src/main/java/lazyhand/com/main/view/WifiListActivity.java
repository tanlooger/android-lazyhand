package lazyhand.com.main.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

import lazyhand.com.main.R;
import lazyhand.com.main.SuperRecyclerAdapter.SuperRecyclerAdapter;
import lazyhand.com.main.SuperRecyclerAdapter.SuperRecyclerHolder;
import lazyhand.com.main.controller.WifiController;
import lazyhand.com.main.model.db.WifiEntity;
import lazyhand.com.main.utils.SwipeView;

public class WifiListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);



        WifiController wifiController = ViewModelProviders.of(this).get(WifiController.class);


        RecyclerView recyclerView = findViewById(R.id.wifi_list);



        wifiController.getAllWifi().observe(this, (List<WifiEntity> wifiEntityList)->{
            Log.e("a", "convert: ");



                SuperRecyclerAdapter mAdapter = new SuperRecyclerAdapter<WifiEntity>(this, wifiEntityList) {



                    @Override
                    public void convert(SuperRecyclerHolder holder, final WifiEntity s, int layoutType, final int position) {


                        //只有一种布局，不使用layoutType来区分type了
                        holder//
                            .setText(R.id.wifi_item_detail_ssid, s.ssid)
                            .setText(R.id.wifi_item_detail_password, s.password)
                            .setText(R.id.wifi_item_detail_bssid, s.bssid)
                            //.setText(R.id.wifi_item_detail_ip, s.ipaddr)
                            .setText(R.id.wifi_item_detail_mac, s.mac_address)
                            .setText(R.id.wifi_item_detail_netid, Integer.toString(s.networkid))
                                .setTag(R.id.wifi_item_delete, wifiEntityList.get(position))

                            .setOnItemClickListenner(new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    //Toast.makeText(WifiListActivity.this, "item点击:" + s, Toast.LENGTH_SHORT).show();
                                }
                            });



                    }

                    @Override public int getLayoutAsViewType(WifiEntity s, int position) {
                        return R.layout.wifi_item;
                    }
                };
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(mAdapter);

        });
    }

    public void onMeWifiAddClick(View view) {
        Intent intent = new Intent(this, WifiAddActivity.class);
        startActivity(intent);
    }

    public void onMeWifiDetailClick(View view) {
        View detail = ((View)view.getParent().getParent()).findViewById(R.id.wifi_item_detail1);
        runOnUiThread(()->{
            if(detail.getVisibility() == View.GONE)
                detail.setVisibility(View.VISIBLE);
            else
                detail.setVisibility(View.GONE);
        });
    }

    public void onWifiDeleteClick(View view) {
        SwipeView.closeMenu(view);

        WifiController wifiController = ViewModelProviders.of(this).get(WifiController.class);

        WifiEntity wifiEntity = (WifiEntity)view.getTag();
        Log.e("delete", "onWifiDeleteClick: "+wifiEntity.ssid);
        wifiController.deleteWifi(wifiEntity);


        /*
        Toast.makeText(WifiListActivity.this, "delete:", Toast.LENGTH_SHORT).show();
        View detail = ((View)view.getParent()).findViewById(R.id.wifi_item_detail1);
        runOnUiThread(()->{
            if(detail.getVisibility() == View.GONE)
                detail.setVisibility(View.VISIBLE);
            else
                detail.setVisibility(View.GONE);
        });
        */
    }
}
