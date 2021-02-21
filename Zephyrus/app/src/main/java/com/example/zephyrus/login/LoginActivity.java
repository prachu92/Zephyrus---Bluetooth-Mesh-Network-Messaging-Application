package com.example.zephyrus.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import com.example.zephyrus.R;
import android.telephony.TelephonyManager;


public class LoginActivity extends AppCompatActivity{
    Context context = getApplication();
    TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    String uuid = tManager.getDeviceId();
    int duration = Toast.LENGTH_SHORT;

    Toast toast = Toast.makeText(context, uuid, duration);
  //  toast.show();

    //Button btn_login;

@Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
   // btn_login = findViewById(R.id.bttn_login);
/*
    btn_login.setOnClickListener(new View.OnClickListener(){



    });
 */
    }

}
