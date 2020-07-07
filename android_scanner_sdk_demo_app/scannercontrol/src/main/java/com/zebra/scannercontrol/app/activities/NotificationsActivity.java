package com.zebra.scannercontrol.app.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Switch;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedSettings();
    }

    //Load the settings from application
    private void loadSavedSettings() {
        Switch availableScanner = (Switch) findViewById(R.id.notAvailableScanner);
        availableScanner.setChecked(Application.MOT_SETTING_NOTIFICATION_AVAILABLE);
        Switch activeScanner = (Switch) findViewById(R.id.notActiveScanner);
        activeScanner.setChecked(Application.MOT_SETTING_NOTIFICATION_ACTIVE);
        Switch barcodeEvent = (Switch) findViewById(R.id.notBarcodeEvent);
        barcodeEvent.setChecked(Application.MOT_SETTING_NOTIFICATION_BARCODE);
        Switch imageEvent = (Switch) findViewById(R.id.notImageEvent);
        imageEvent.setChecked(Application.MOT_SETTING_NOTIFICATION_IMAGE);
        Switch videoEvent = (Switch) findViewById(R.id.notVideoEvent);
        videoEvent.setChecked(Application.MOT_SETTING_NOTIFICATION_VIDEO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save preferences
        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        settingsEditor.putBoolean(Constants.PREF_NOTIFY_AVAILABLE, ((Switch) findViewById(R.id.notAvailableScanner)).isChecked());
        settingsEditor.putBoolean(Constants.PREF_NOTIFY_ACTIVE, ((Switch) findViewById(R.id.notActiveScanner)).isChecked());
        settingsEditor.putBoolean(Constants.PREF_NOTIFY_BARCODE, ((Switch) findViewById(R.id.notBarcodeEvent)).isChecked());
        settingsEditor.putBoolean(Constants.PREF_NOTIFY_IMAGE, ((Switch) findViewById(R.id.notImageEvent)).isChecked());
        settingsEditor.putBoolean(Constants.PREF_NOTIFY_VIDEO, ((Switch) findViewById(R.id.notVideoEvent)).isChecked()).apply();
    }
}
