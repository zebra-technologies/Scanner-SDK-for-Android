package com.zebra.scannercontrol.app.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.barcode.BarCodeView;
import com.zebra.scannercontrol.app.barcode.GenerateBarcode128B;
import com.zebra.scannercontrol.app.helpers.UIEnhancer;

import java.util.Objects;

public class PairingInstructionsAll extends AppCompatActivity {
    private FrameLayout llBarcode;
    LinearLayout linearPairAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_instructions_all);
        linearPairAll = findViewById(R.id.linear_pair_all);
        UIEnhancer.enableEdgeToEdge(linearPairAll);
        UIEnhancer.configureOrientation(this);

        reloadBarcode();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadBarcode();

    }

    private void reloadBarcode() {
        llBarcode = (FrameLayout) findViewById(R.id.set_factory_defaults_barcode);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        String data2encode = String.valueOf((char) 3) +
                "92";
        GenerateBarcode128B barcode = new GenerateBarcode128B(data2encode);
        BarCodeView barCodeView = new BarCodeView(this, barcode);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int x = width * 6 / 10;
        int y = x / 3;
        barCodeView.setSize(x,y);
        llBarcode.addView(barCodeView, layoutParams);
    }

}
