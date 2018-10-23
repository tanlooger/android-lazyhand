package lazyhand.com.main.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import lazyhand.com.main.R;

public class UserinfoActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);



        ((TextView)findViewById(R.id.userinfo_name)).setText(sharedPreferences.getString("USER_NAME", ""));
        ((TextView)findViewById(R.id.userinfo_nickname)).setText(sharedPreferences.getString("USER_NICKNAME", ""));
        ((TextView)findViewById(R.id.userinfo_tweet)).setText(sharedPreferences.getString("USER_TWEET", ""));
        ((TextView)findViewById(R.id.userinfo_email)).setText(sharedPreferences.getString("USER_EMAIL", ""));

    }
}
