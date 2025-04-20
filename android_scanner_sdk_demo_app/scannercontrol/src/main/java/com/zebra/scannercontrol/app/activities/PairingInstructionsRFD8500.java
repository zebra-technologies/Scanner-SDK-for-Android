package com.zebra.scannercontrol.app.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.UIEnhancer;

public class PairingInstructionsRFD8500 extends AppCompatActivity {
    LinearLayout linearPairInstr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_instructions_rfd8500);
        linearPairInstr = findViewById(R.id.linear_pair_instructions);
        UIEnhancer.enableEdgeToEdge(linearPairInstr);
        UIEnhancer.configureOrientation(this);
    }

}
