package lazyhand.com.main.view;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


//import cn.bingoogolapple.badgeview.annotation.BGABadge;
import lazyhand.com.main.R;
import lazyhand.com.main.controller.DevicesController;
import lazyhand.com.main.udp.UDPSocket;

/*
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
public class MainActivity extends AppCompatActivity
                        implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnTouchListener

{

    private TextView mTextMessage;
    private Toolbar toolbar;
    private FrameLayout mainbody;
    private BottomNavigationView navigation;
    private UsedFragment usedFragment;
    static private Fragment nowFragment;
    private DeviceFragment deviceFragment;
    private MeFragment meFragment;


    int width;

    DevicesController devicesController;

    public static final String TAG = "UdpSendThread";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextMessage = (TextView) findViewById(R.id.message);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        mainbody = findViewById(R.id.mainbody);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        usedFragment = new UsedFragment();
        meFragment = new MeFragment();
        loadFragment(usedFragment);


        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimary);
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        */


        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        width = dm.widthPixels;
        int height = dm.heightPixels;

        FrameLayout frameLayout = findViewById(R.id.right);

        //frameLayout.setOnTouchListener(this);


        frameLayout.setX(width);



        devicesController = ViewModelProviders.of(this).get(DevicesController.class);


    }






    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.used:
                loadFragment(usedFragment);
                return true;
            case R.id.devices:
               // mTextMessage.setText(R.string.title_dashboard);
                return true;
            case R.id.fee:
                //mTextMessage.setText(R.string.title_notifications);
                return true;
            case R.id.me:
                //mTextMessage.setText(R.string.title_notifications);
                loadFragment(meFragment);
                return true;
        }

        return false;
    }

    private void loadFragment(Fragment toFragment) {
        if (nowFragment == toFragment) return;


        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //ft.replace(R.id.mainbody, toFragment).commit();

        if(nowFragment == null)
            ft.add(R.id.mainbody, toFragment).commit();
        else
        if (toFragment.isAdded())
            ft.hide(nowFragment).show(toFragment).commit();
        else
            ft.hide(nowFragment).add(R.id.mainbody, toFragment).commit();

        nowFragment = toFragment;

    }




    float lastX;
    float preRawX;
    float nowRawX;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        nowRawX = event.getRawX();
        float displacement = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("main ACTION_DOWN", "onTouch: "+Float.toString(v.getTranslationX()));

                preRawX = nowRawX;
                lastX = nowRawX;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("main ACTION_MOVE", "onTouch: "+Float.toString(v.getTranslationX()));
                if(v.getTranslationX()>=0 && v.getRight()<width && Math.abs(nowRawX-preRawX) > 20) {
                    displacement = nowRawX - preRawX;
                    if(v.getTranslationX()==0 && displacement<0){
                    }else {
                        v.setX(v.getX() + (nowRawX - preRawX));
                        preRawX = event.getRawX();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("main ACTION_UP", "onTouch: "+Float.toString(v.getTranslationX()));

                if(v.getTranslationX() > width/2){
                    v.setX(-width);
                } else {
                    v.setX(0);
                }
                break;

        }
        return true;
    }


    public void onMeWifiClick(View view) {

        meFragment.onMeWifiClick(view);

    }

    public void onMeLoginClick(View view) {
        meFragment.onMeLoginClick(view);
    }

    public void onMeUserinfoClick(View view) {meFragment.onMeUserinfoClick(view);}


    @SuppressLint("MissingSuperCall")
    @Override  //fringe
    protected void onSaveInstanceState(Bundle outState) { }


}
