package com.zebra.scannercontrol.app.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.LifecycleCallbacksInSca;
import com.zebra.scannercontrol.app.helpers.UIEnhancer;

public class SupportedScanners extends AppCompatActivity {

    LinearLayout linearSupportScanners;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supported_scanners);
        linearSupportScanners = findViewById(R.id.linear_support_scanners);
        UIEnhancer.enableEdgeToEdge(linearSupportScanners);
        UIEnhancer.configureOrientation(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }
    public boolean isInBackgroundMode(final Context context) {
        return LifecycleCallbacksInSca.get().isBackground();
    }
}
