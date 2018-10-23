package lazyhand.com.main.view;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.JsonObjectRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import lazyhand.com.main.R;
import lazyhand.com.main.controller.DevicesController;
import lazyhand.com.main.databinding.FragmentDeviceBinding;
import lazyhand.com.main.model.db.DeviceEntity;
import lazyhand.com.main.login.AppSingleton;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link .} interface
 * to handle interaction events.
 * Use the {@link DeviceFragment#} factory method to
 * create an instance of this fragment.
 */
public class DeviceFragment extends Fragment implements View.OnTouchListener{

    int width;
    private View itemView;
    private TextView deviceName;
    DeviceEntity deviceEntity;
    FragmentDeviceBinding binding;


    // private OnFragmentInteractionListener mListener;

    public DeviceFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_device, container, false);

        //View v = inflater.inflate(R.layout.fragment_device, container, false);

        View v = binding.getRoot();

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        width = dm.widthPixels;


        FrameLayout f;
        f = getActivity().findViewById(R.id.right);
        f.setOnTouchListener(this);

        String itemTag =((TextView) (v.findViewById(R.id.item_tag))).getText().toString();


        deviceName = v.findViewById(R.id.device_name);

        Button deviceActivite = v.findViewById(R.id.device_activite);
        deviceActivite.setOnClickListener((View view)->{
            new Thread(()->{
                Log.d("click", "onClick: ");
                try {
                    Socket socket  = new Socket(deviceEntity.ipaddr, 22222);
                    OutputStream out = socket.getOutputStream();
                    out.write(("{\"cmd\":\"activate\", \"hashcode\":\""+deviceEntity.hashcode+"\"}").getBytes());

                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final String st = input.readLine();
                    Log.e("response", "onClick: "+st);

                    if(st != null) {
                        DevicesController devicesController = ViewModelProviders.of((FragmentActivity) getActivity()).get(DevicesController.class);
                        JSONObject jsonObject = new JSONObject(st);
                        deviceEntity.hashcode = jsonObject.getString("hashcode");
                        Log.d("response", "onClick: " + deviceEntity.hashcode);

                        devicesController.addDevice(deviceEntity);
                        binding.setDeviceEntity(deviceEntity);
                        String URL_FOR_AUTHORIZE = "https://iot.espressif.cn/v1/key/authorize/";

                        String token = "{\"token\":\""+deviceEntity.hashcode+"\"}";
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userinfo", Context.MODE_PRIVATE);
                        getIot(URL_FOR_AUTHORIZE, sharedPreferences.getString("USER_KEY", ""), token);
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }).start();
        });

        Button deviceSwitch = v.findViewById(R.id.device_switch);
        deviceSwitch.setOnClickListener((View view)->{
            String URL_FOR_DATAPOINT = "https://iot.espressif.cn/v1/datastreams/switch-status/datapoint/?deliver_to_device=true";
            int myInt = offon ? 1 : 0;
            String datapoint = "{\"datapoint\":{\"x\":"+Integer.toString(myInt)+"}}";
            new Thread(()->{
                getIot(URL_FOR_DATAPOINT, deviceEntity.ownerkey, datapoint);
                offon = !offon;
            }).start();

        });

        return v;
    }
    private boolean offon = false;


    private void getIot(final String urlString, final String token, final String jsonString) {

        try {

            //建立连接
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            //设置参数
            httpConn.setDoOutput(true);     //需要输出
            httpConn.setDoInput(true);      //需要输入
            httpConn.setUseCaches(false);   //不允许缓存
            httpConn.setRequestMethod("POST");      //设置POST方式连接

            //设置请求属性
            //httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            //httpConn.setRequestProperty("Charset", "UTF-8");
            httpConn.setRequestProperty("Authorization", "token " + token);

            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();

            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());


            dos.writeBytes(jsonString);
            dos.flush();
            dos.close();

            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "ASCII"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                System.out.println("aaaaaaaaaaaaaa\n"+sb.toString());
                try {
                    JSONObject jsonObject = new JSONObject(sb.toString());

                    int status = jsonObject.getInt("status");

                    if (status == 200) {
                        JSONObject ownerKey = jsonObject.getJSONObject("key");
                        DevicesController deviceViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(DevicesController.class);
                        deviceEntity.ownerkey = ownerKey.getString("token");
                        Log.d("response", "ownerkey: " + deviceEntity.ownerkey);
                        deviceViewModel.addDevice(deviceEntity);
                        binding.setDeviceEntity(deviceEntity);
                    }
                }catch (JSONException e){

                }


            }
        }catch (IOException e){

        }
    }


    private void getAuthorize2(final String hashcode) {
        String URL_FOR_AUTHORIZE = "https://iot.espressif.cn/v1/key/authorize/";
        String cancel_req_tag = "authorize";

        Map<String, String> params = new HashMap<String, String>();
        //params.put("path", "/v1/key/authorize/");
        //params.put("method", "POST");
        params.put("token", hashcode);


        //params.put("meta", "{\"Authorization\": \"token HERE_IS_THE_DEVICE_KEY\"}");
        JSONObject paramJsonObject = new JSONObject(params);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_FOR_AUTHORIZE, paramJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Register Response: " + response.toString());
                //hideDialog();
                //SharedPreferences.Editor editor = sharedPreferences.edit();


                try {
                    int status = response.getInt("status");

                    if (status == 200) {
                        String ownerKey = response.getString("key");
                        DevicesController deviceViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(DevicesController.class);
                        deviceEntity.ownerkey = ownerKey;
                        Log.d("response", "ownerkey: "+deviceEntity.ownerkey);
                        deviceViewModel.addDevice(deviceEntity);
                        synchronized (deviceViewModel) {
                            deviceViewModel.notifyAll();
                        }

                    } else {
                        String errorMsg = response.getString("key");
                        Toast.makeText(getActivity(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){

            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userinfo", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "token "+sharedPreferences.getString("USER_KEY", ""));
                return params;
            }

        };
        //添加到requestQueue
        AppSingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest,cancel_req_tag);

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
       // mListener = null;
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
                preRawX = nowRawX;
                lastX = nowRawX;
                break;
            case MotionEvent.ACTION_MOVE:
                displacement = nowRawX - preRawX;
                if(v.getTranslationX()>=0 && Math.abs(displacement) > 2) {
                    if((v.getTranslationX() + displacement) < 0) {
                        v.setX(0);
                        itemView.setX(-width);
                    }else {
                        v.setX(v.getX() + displacement);
                        itemView.setX(itemView.getX() + displacement);
                    }

                    preRawX = event.getRawX();

                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP:"+Float.toString(v.getTranslationX()));

                int speed;

                //speed *= 2;
                if(v.getTranslationX() > width/2){
                    //itemView.setX(0);
                    speed = Math.abs((int)(v.getX()-width));

                    //v.setX(width);
                    ObjectAnimator.ofFloat(v, "translationX", v.getX(), width).setDuration(speed).start();

                    ObjectAnimator.ofFloat(itemView, "translationX", itemView.getX(), 0).setDuration(speed).start();

                } else {
                    //itemView.setX(-width);
                    speed = Math.abs((int)(v.getX()));
                    ObjectAnimator.ofFloat(v, "translationX", v.getX(), 0).setDuration(speed).start();

                    ObjectAnimator.ofFloat(itemView, "translationX", itemView.getX(), -width).setDuration(speed).start();

                    //v.setX(0);

                }

                break;

        }
        return true;
    }


    public void sleep(int ms){
        try {
            Thread.currentThread().sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceItem(DeviceEntity entity) {
        Log.d(TAG, "deviceItem: "+entity.name);
        this.deviceEntity = entity;
        deviceName.setText(deviceEntity.name);
        binding.setDeviceEntity(entity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceItemView(View view) {
        this.itemView = view;
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //Log.d("s", "onStart: ");
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        //Log.d("s", "onStop: ");
    }



}
