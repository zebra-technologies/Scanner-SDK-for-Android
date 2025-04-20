package com.zebra.scannercontrol.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.LifecycleCallbacksInSca;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.helpers.UIEnhancer;

public class ConnectionHelpActivity2 extends AppCompatActivity{
    LinearLayout linearHelp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_help2);
        linearHelp = findViewById(R.id.linear_help);
        UIEnhancer.enableEdgeToEdge(linearHelp);
        UIEnhancer.configureOrientation(this);


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
        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }
    public boolean isInBackgroundMode(final Context context) {
        return LifecycleCallbacksInSca.get().isBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadSupportedScanners(View view) {
        Intent intent = new Intent(this, SupportedScanners.class);
        startActivity(intent);
    }

    public void loadPairingHelpAll(View view) {
        Intent intent = new Intent(this, PairingInstructionsAll.class);
        startActivity(intent);
    }

    public void loadPairingHelpRFD8500(View view) {
        Intent intent = new Intent(this, PairingInstructionsRFD8500.class);
        startActivity(intent);
    }
}

