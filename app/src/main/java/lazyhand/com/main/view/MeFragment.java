package lazyhand.com.main.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import lazyhand.com.main.R;
import lazyhand.com.main.login.LoginActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link } interface
 * to handle interaction events.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences sharedPreferences=null;

    TextView meLoginView;
    TextView meNickView;
    TextView meTweetView;



    public MeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sharedPreferences = getActivity().getSharedPreferences("userinfo", Context.MODE_PRIVATE);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_me, container, false);

        meNickView = view.findViewById(R.id.platform_user_nick);
        meTweetView = view.findViewById(R.id.platform_user_tweet);
        meLoginView = view.findViewById(R.id.me_login);

        meNickView.setText(sharedPreferences.getString("USER_NICKNAME", "未登录"));
        meTweetView.setText(sharedPreferences.getString("USER_TWEET", "未登录，无法远程控制设备"));
        return view;
    }

    public void onMeLoginClick(View view) {
        if(sharedPreferences.contains("USER_PASSWORD")){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            getActivity().runOnUiThread(()->{
                meNickView.setText(sharedPreferences.getString("USER_NICKNAME", "未登录"));
                meTweetView.setText(sharedPreferences.getString("USER_TWEET", "未登录，无法远程控制设备"));
                if(sharedPreferences.contains("USER_PASSWORD")){
                    meLoginView.setText("退出");
                }else {
                    meLoginView.setText("登录/注册");
                }
            });
        }else {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    public void onMeWifiClick(View view) {
        Intent intent = new Intent(getContext(), WifiListActivity.class);
        startActivity(intent);
    }

    public void onMeUserinfoClick(View view) {
        Intent intent = new Intent(getContext(), UserinfoActivity.class);
        startActivity(intent);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onResume() {//和activity的onResume绑定，Fragment初始化的时候必调用，但切换fragment的hide和visible的时候可能不会调用！
        super.onResume();
        getActivity().runOnUiThread(()->{
            Log.d("tag", "onResume: ");
            meNickView.setText(sharedPreferences.getString("USER_NICKNAME", "未登录"));
            meTweetView.setText(sharedPreferences.getString("USER_TWEET", "未登录，无法远程控制设备"));
            if(sharedPreferences.contains("USER_PASSWORD")){
                meLoginView.setText("退出");
            }else {
                meLoginView.setText("登录/注册");
            }
        });


    }


        @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
