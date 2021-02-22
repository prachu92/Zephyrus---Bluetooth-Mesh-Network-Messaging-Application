package com.example.zephyrus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.zephyrus.client.ClientActivity;
import com.example.zephyrus.databinding.ActivityMainBinding;
import com.example.zephyrus.server.ServerActivity;
import com.example.zephyrus.login.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button server_btn = findViewById(R.id.svr_btn);
//        Button client_btn = findViewById(R.id.clnt_btn);


        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.svrBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                ServerActivity.class)));
        binding.clntBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                ClientActivity.class)));


        //request permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }


        //get uuid
        Context context = getApplication();
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        
       //fetches the user's device id
       //getDeviceid() -- deprecated
       // String uuid = tManager.getDeviceId();
        
        //randomly generated UUID
        String uuid = UUID.randomUUID().toString();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, uuid, duration);
        toast.show();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }
}


//public class MainActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        TextView msg_txt = findViewById(R.id.txt_msg);
//        Button login_btn = findViewById(R.id.login_btn);
//
//        BiometricManager biometricManager = BiometricManager.from(this);
//        switch (biometricManager.canAuthenticate()) {
//            case BiometricManager.BIOMETRIC_SUCCESS: //user can use biometric sensor
////                msg_txt.setText("You can use the fingerprint sensor to login");
//            //msg_txt.setTextColor(Color.parseColor(colorString: "#Fafafa"));
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE: //Biometric sensor not supported in the device
//                msg_txt.setText("The device doesn't have fingerprint sensor");
//                login_btn.setVisibility(View.GONE);
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE: //Biometric sensor currently not available
//                msg_txt.setText("The biometric sensor is currently unavailable");
//                login_btn.setVisibility(View.GONE);
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED://No saved biometrics in the device
//                msg_txt.setText("Device doesn't have a saved fingerprint, please check security settings");
//                login_btn.setVisibility(View.GONE);
//                break;
//        }
//        //Biometric dialog box
//        Executor executor = ContextCompat.getMainExecutor(  this);
//
//        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override // On error while authentication
//            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//            }
//
//            @Override //On successful authentication
//            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//               // Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
//                Intent intent=new Intent(MainActivity.this,HomeActivity.class);
//                startActivity(intent);
//            }
//
//            @Override // On authentication failure
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//            }
//        });
//
//        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Login")
//                .setDescription("Use your fingerprint to login")
//                .setNegativeButtonText("cancel")
//                .build();
//        // call the dialog on click of login
//        login_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            biometricPrompt.authenticate(promptInfo);
//            }
//        });
//
//    }
//}
