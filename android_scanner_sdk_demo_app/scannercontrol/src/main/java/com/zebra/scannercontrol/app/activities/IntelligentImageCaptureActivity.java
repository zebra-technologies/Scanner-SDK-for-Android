package com.zebra.scannercontrol.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDCConfig;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.barcode.BarcodeTypes;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_STORE;

public class IntelligentImageCaptureActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate,ScannerAppEngine.IScannerAppEngineDevEventsDelegate {

    int idcModeAttribute = 594;
    int idcModeValue = 2;
    String binaryDataType = "B";
    public static Bitmap retrievedSlowestDecodeImage = null;
    private ImageView SnapiImageView;
    TextView barcodeIdc;
    TextView barcodeTypeIdc;
    TextView isoIdc;
    CheckBox IOSFile;
    SeekBar seekBarVolume;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private int scannerID;
    String disconnect_current_scanner = "This will disconnect your current scanner";
    String continueTxt = "Continue";
    String barcode_type = "Barcode Type : ";
    String barcode_data = "Barcode Data:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idc);

        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp< Application.minScreenWidth){
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
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.title_activity_idc);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setUI();
    }


    public void onCheckboxISOData(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        IDCConfig idcConfig = new IDCConfig();
        barcodeIdc = (TextView) findViewById(R.id.barcodeDataIdc);
        barcodeTypeIdc = (TextView) findViewById(R.id.barcodeTypeIdc);
        isoIdc = (TextView) findViewById(R.id.txt_iso_data);

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkBoxISO:
                if (checked) {
                    idcConfig.setSendIDCDataAsBinaryEventEnabledFlag(true);
                    Application.sdkHandler.dcssdkSetIDCConfig(idcConfig);
                }
            else {
                    idcConfig.setSendIDCDataAsBinaryEventEnabledFlag(false);
                    Application.sdkHandler.dcssdkSetIDCConfig(idcConfig);

                }
                barcodeIdc.setText("");
                barcodeTypeIdc.setText("");
                SnapiImageView.setImageDrawable(null);
                isoIdc.setText("");
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
        removeDevEventsDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);

    }

    private void setUI()
    {
        SnapiImageView = (ImageView) findViewById(R.id.imgViewSnapiImage);
        IOSFile = (CheckBox)findViewById(R.id.checkBoxISO);
        barcodeIdc = (TextView) findViewById(R.id.barcodeDataIdc);
        barcodeTypeIdc = (TextView) findViewById(R.id.barcodeTypeIdc);
        isoIdc = (TextView) findViewById(R.id.txt_iso_data);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        addDevConnectionsDelegate(this);
        addDevEventsDelegate(this);
    }

    private void Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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
            intent = new Intent(IntelligentImageCaptureActivity.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        }else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new  AlertDialog.Builder(this);
            dlg.setTitle(disconnect_current_scanner);
            dlg.setPositiveButton(continueTxt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(IntelligentImageCaptureActivity.this, FindCabledScanner.class);
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


    public void idcMode(View view) {

        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>"+idcModeAttribute+"</id><datatype>binaryDataType</datatype><value>"+idcModeValue+"</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder sb = new StringBuilder();
        
        Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_RSM_ATTR_STORE,inXML,sb,scannerID);
    }

    // IScannerAppEngineDevConnectionsDelegate Events
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
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        this.finish();
        Application.currentScannerId =Application.SCANNER_ID_NONE;
        Intent intent = new Intent(IntelligentImageCaptureActivity.this,HomeActivity.class);
        startActivity(intent);
        return true;
    }


    // IScannerAppEngineDevEventsDelegate Events
    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {

        barcodeIdc = (TextView) findViewById(R.id.barcodeDataIdc);
        barcodeIdc.setText(barcode_data+(new String(barcodeData)));
        barcodeTypeIdc = (TextView) findViewById(R.id.barcodeTypeIdc);
        barcodeTypeIdc.setText(barcode_type + BarcodeTypes.getBarcodeTypeName(barcodeType));
    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
    }

    @Override
    public void  scannerImageEvent(byte[] imageData) {

        barcodeIdc = (TextView) findViewById(R.id.barcodeDataIdc);
        barcodeTypeIdc = (TextView) findViewById(R.id.barcodeTypeIdc);
        isoIdc = (TextView) findViewById(R.id.txt_iso_data);
        barcodeIdc.setText("");
        barcodeTypeIdc.setText("");
        isoIdc.setText("");
        final byte[] tempImgData = imageData;

        SnapiImageView.setPadding(0, 0, 0, 0);
        retrievedSlowestDecodeImage = BitmapFactory.decodeByteArray(tempImgData, 0, tempImgData.length);
        SnapiImageView.setImageBitmap(retrievedSlowestDecodeImage);


    }

    @Override
    public void  scannerVideoEvent(byte[] videoData) {

    }


}
