package com.zebra.scannercontrol.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

public class SampleBarcodes extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {
    private int scannerID;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_barcodes);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Toolbar subActionBar = (Toolbar) findViewById(R.id.sub_actionbar);
        setSupportActionBar(subActionBar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.title_activity_sample_barcodes);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        RadioButton upc = (RadioButton)findViewById(R.id.radio_upc);
        if(upc!=null){
            upc.setChecked(true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            finish();
            intent = new Intent(SampleBarcodes.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        }else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new  AlertDialog.Builder(this);
            dlg.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            dlg.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(SampleBarcodes.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                }
            });
            dlg.show();
        }else if (id == R.id.nav_connection_help) {
            intent = new Intent(this, ConnectionHelpActivity2.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        this.finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }

    public void onRadioButtonClicked(View view) {

            switch (view.getId()) {
                case R.id.radio_upc:
                    checkUPCA();
                    break;
                case R.id.radio_code128:
                    checkCode128();
                    break;
                case R.id.radio_data_matrix:
                    checkDM();
                    break;
            }
    }

    private void checkUPCA() {
        ImageView barcode = (ImageView)findViewById(R.id.img_barcode);
        RadioButton upc = (RadioButton)findViewById(R.id.radio_upc);
        RadioButton code128 = (RadioButton)findViewById(R.id.radio_code128);
        RadioButton dataMatrix = (RadioButton)findViewById(R.id.radio_data_matrix);

        if (barcode != null) {
            barcode.setImageResource(R.drawable.upc);
        }
        if (code128 != null) {
            code128.setChecked(false);
        }
        if (dataMatrix != null) {
            dataMatrix.setChecked(false);
        }

    }

    private void checkCode128() {
        ImageView barcode = (ImageView)findViewById(R.id.img_barcode);
        RadioButton upc = (RadioButton)findViewById(R.id.radio_upc);
        RadioButton code128 = (RadioButton)findViewById(R.id.radio_code128);
        RadioButton dataMatrix = (RadioButton)findViewById(R.id.radio_data_matrix);

        if (barcode != null) {
            barcode.setImageResource(R.drawable.code_128);
        }
        if (upc != null) {
            upc.setChecked(false);
        }
        if (dataMatrix != null) {
            dataMatrix.setChecked(false);
        }

    }

    private void checkDM() {
        ImageView barcode = (ImageView)findViewById(R.id.img_barcode);
        RadioButton upc = (RadioButton)findViewById(R.id.radio_upc);
        RadioButton code128 = (RadioButton)findViewById(R.id.radio_code128);
        RadioButton dataMatrix = (RadioButton)findViewById(R.id.radio_data_matrix);

        if (barcode != null) {
            barcode.setImageResource(R.drawable.data_matrix);
        }
        if (upc != null) {
            upc.setChecked(false);
        }
        if (code128 != null) {
            code128.setChecked(false);
        }

    }
}
