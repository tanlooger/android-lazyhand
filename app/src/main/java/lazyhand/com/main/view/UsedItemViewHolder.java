package lazyhand.com.main.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import lazyhand.com.main.R;
import lazyhand.com.main.model.db.DeviceEntity;

public class UsedItemViewHolder extends RecyclerView.ViewHolder implements ViewGroup.OnTouchListener, ViewGroup.OnClickListener{
    private final View mView;
    private DeviceEntity mItem;
    private int width;
    private FrameLayout frameLayout;

    protected UsedItemViewHolder(View view, Context context1) {
        super(view);
        mView = view;

        Resources resources = view.getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        width = dm.widthPixels;

        mView.setOnTouchListener(this);
        mView.setOnClickListener(this);

        frameLayout = ((MainActivity) view.getContext()).findViewById(R.id.right);

        ImageView myTasksButton = (ImageView)view.findViewById(R.id.item_wifi_icon);
        Drawable myTasksDrawable = myTasksButton.getDrawable();
        myTasksDrawable.setColorFilter(Color.parseColor("#FF05F4F4"), PorterDuff.Mode.SRC_ATOP);

        ImageView myTasksButton2 = (ImageView)view.findViewById(R.id.item_cloud_icon);
        Drawable myTasksDrawable2 = myTasksButton2.getDrawable();
        myTasksDrawable2.setColorFilter(Color.parseColor("#FF05F4F4"), PorterDuff.Mode.SRC_ATOP);

        ImageView myTasksButton3 = (ImageView)view.findViewById(R.id.item_sun_icon);
        Drawable myTasksDrawable3 = myTasksButton3.getDrawable();
        myTasksDrawable3.setColorFilter(Color.parseColor("#FF05F4F4"), PorterDuff.Mode.SRC_ATOP);
    }

    public void setItem(DeviceEntity item){
        mItem = item;
    }



    private float lastX;
    private float preRawX;
    private Boolean isDrag = false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float nowRawX = event.getRawX();
        float displacement = 0;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preRawX = nowRawX;
                lastX = nowRawX;
                DeviceFragment deviceFragment = new DeviceFragment();
                FragmentManager fm = ((Activity)mView.getContext()).getFragmentManager();

                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.right, deviceFragment).commit();

                break;
            case MotionEvent.ACTION_MOVE:
                displacement = nowRawX - preRawX;
                Log.d("t", "onTouch: "+Float.toString(v.getTranslationX()));

                if(v.getTranslationX()<=0 && Math.abs(displacement) > 2){
                    if((v.getTranslationX()+displacement)>0){
                        v.setX(0);
                        frameLayout.setX(width);
                    }else {
                        if (isDrag) {
                            v.setX(v.getX() + displacement);
                            frameLayout.setX(frameLayout.getX() + displacement);
                            //preRawX = event.getRawX();
                        } else if (Math.abs(displacement) > 20) {
                            isDrag = true;
                            v.setX(v.getX() + displacement);
                            frameLayout.setX(frameLayout.getX() + displacement);
                            //preRawX = event.getRawX();
                        }
                    }
                    preRawX = event.getRawX();

                }

                break;
            case MotionEvent.ACTION_UP:
                int speed;

                //speed *= 2;
                if(v.getTranslationX() > -width/2){
                    Log.d("old", "onTouch: old fragment");
                    //v.setX(0);
                    //frameLayout.setX(width);
                    speed = Math.abs((int)(v.getX()));

                    ObjectAnimator.ofFloat(v, "translationX", v.getX(), 0).setDuration(speed).start();
                    ObjectAnimator.ofFloat(frameLayout, "translationX", frameLayout.getX(), width).setDuration(speed).start();
                } else {
                    Log.d("new", "onTouch: new fragment");
                    //v.setX(-width);
                    //frameLayout.setX(0);
                    speed = Math.abs((int)(v.getX()+width));
                    ObjectAnimator.ofFloat(v, "translationX", v.getX(), -width).setDuration(speed).start();
                    ObjectAnimator.ofFloat(frameLayout, "translationX", frameLayout.getX(), 0).setDuration(speed).start();

                    EventBus.getDefault().post(mItem);
                    EventBus.getDefault().post(mView);
                }
                if(isDrag){
                    isDrag = false;
                    v.getParent().requestDisallowInterceptTouchEvent(isDrag);
                    return true;
                }

            case MotionEvent.ACTION_CANCEL:
                Log.d("touch", "onTouchEvent: ACTION_CANCEL");
                break;
        }
        v.getParent().requestDisallowInterceptTouchEvent(isDrag);
        return isDrag;
    }


    //onTouch—–>onTouchEvent—>onclick
    private boolean offon = false;
    @Override
    public void onClick(View v) {
        if(mItem.ipaddr == null)return;
        new Thread(()->{
            try {
                //Socket socket  = new Socket(mContentView.getText().toString(), 22222);
                Socket socket = new Socket();
                SocketAddress socAddress = new InetSocketAddress(mItem.ipaddr, 22222);
                socket.connect(socAddress, 5000);

                OutputStream out = socket.getOutputStream();

                if(offon) {
                    out.write(("{\"cmd\":\"offon\",\"active\":\"off\", \"hashcode\":\""+mItem.hashcode+"\"}").getBytes());
                }else {
                    out.write(("{\"cmd\":\"offon\",\"active\":\"on\", \"hashcode\":\""+mItem.hashcode+"\"}").getBytes());
                }
                offon = !offon;
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final String st = input.readLine();
                Log.d("response", "onClick: "+st);
            }catch (IOException e){
                Log.e("eeeee", "onClick: ");
                e.printStackTrace();
            }
        }).start();
    }

}

