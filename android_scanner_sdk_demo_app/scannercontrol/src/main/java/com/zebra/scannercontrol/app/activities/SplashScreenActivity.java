package com.zebra.scannercontrol.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class SplashScreenActivity extends AppCompatActivity {

    ArrayList<String> permissionsList;
    int permissionsCount = 0;
    AlertDialog alertDialog;
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_13_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private final static int REQUEST_ENABLE_BT=1;

    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String,Boolean> result) {
                            if (result.size() != 0) {
                                ArrayList<Boolean> list = new ArrayList<>(result.values());
                                permissionsList = new ArrayList<>();
                                permissionsCount = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (shouldShowRequestPermissionRationale(ANDROID_13_BLE_PERMISSIONS[i])) {
                                            permissionsList.add(ANDROID_13_BLE_PERMISSIONS[i]);
                                        } else if (!hasPermission(SplashScreenActivity.this, ANDROID_13_BLE_PERMISSIONS[i])) {
                                            permissionsCount++;
                                        }
                                    }
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (shouldShowRequestPermissionRationale(ANDROID_12_BLE_PERMISSIONS[i])) {
                                            permissionsList.add(ANDROID_12_BLE_PERMISSIONS[i]);
                                        } else if (!hasPermission(SplashScreenActivity.this, ANDROID_12_BLE_PERMISSIONS[i])) {
                                            permissionsCount++;
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (shouldShowRequestPermissionRationale(BLE_PERMISSIONS[i])) {
                                            permissionsList.add(BLE_PERMISSIONS[i]);
                                        } else if (!hasPermission(SplashScreenActivity.this, BLE_PERMISSIONS[i])) {
                                            permissionsCount++;
                                        }
                                    }
                                }
                                if (permissionsList.size() > 0) {
                                    //Some permissions are denied and can be asked again.
                                    askForPermissions(permissionsList);
                                } else if (permissionsCount > 0) {
                                    //Show alert dialog
                                    showPermissionDialog();
                                } else {
                                    //All permissions granted
                                    initSplash();
                                }
                            }
                        }

                    });

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);
        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(configuration.screenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.addAll(Arrays.asList(ANDROID_13_BLE_PERMISSIONS));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionsList.addAll(Arrays.asList(ANDROID_12_BLE_PERMISSIONS));
        } else {
            permissionsList.addAll(Arrays.asList(BLE_PERMISSIONS));
        }
        askForPermissions(permissionsList);


    }

    public void initSplash(){
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        /* Duration of wait */
        int SPLASH_DISPLAY_LENGTH = 2000;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startActivity(mainIntent);
                SplashScreenActivity.this.finish();;

            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    /**
     * Method to request required permissions with the permission launcher
     * @param permissionsList list of permissions
     */
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            showPermissionDialog();
        }


    }


    /**
     * Method to redirect to application settings if user denies the permissions
     */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setCancelable(false)
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    //askForPermissions(permissionsList);
                    openAppSettings();
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    /**
     * Method to open application settings
     */
    public void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",SplashScreenActivity.this.getPackageName(), null);
        intent.setData(uri);
        SplashScreenActivity.this.startActivity(intent);
    }

    /**
     * Method to check permissions granted status
     * @param context
     * @param permissionStr
     * @return true if permission granted otherwise false
     */
    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }
}
