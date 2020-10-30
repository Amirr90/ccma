package com.ccma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import static com.ccma.Utility.Util.IS_LOGIN;
import static com.ccma.Utility.Util.MyPREFERENCES;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                checkLoginStatus();
            }
        }.start();
    }

    private void checkLoginStatus() {

        if (sharedpreferences.contains(IS_LOGIN)) {
            boolean isLogin = sharedpreferences.getBoolean(IS_LOGIN, false);
            if (isLogin) {
                startActivity(new Intent(this, HomeScreen.class));
            } else {
                startActivity(new Intent(this, LoginScreen.class));
            }
            finish();
        } else {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        }
    }
}
