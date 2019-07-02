package com.zebra.scannercontrol.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DISABLE_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ENABLE_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RESET_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ZERO_SCALE;

public class ScaleActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {



    public static Bitmap retrievedDecodeImage = null;
    private ImageView SnapiImageView;

    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private int scannerID;

    private int STATUS_SCALE_NOT_ENABLED = 0;
    private int STATUS_SCALE_NOT_READY = 1;
    private int STATUS_STABLE_WEIGHT_OVER_LIMIT = 2;
    private int STATUS_STABLE_WEIGHT_UNDER_ZERO = 3;
    private int STATUS_NON_STABLE_WEIGHT = 4;
    private int STATUS_STABLE_ZERO_WEIGHT = 5;
    private int STATUS_STABLE_NON_ZERO_WEIGHT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);

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
            actionBar.setTitle("Active Scanner");
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
        addDevConnectionsDelegate(this);
        //addDevEventsDelegate(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
        //removeDevEventsDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
        boolean isScaleAvailable = getIntent().getBooleanExtra(Constants.SCALE_STATUS, false);
        TextView textViewNoScale = (TextView)findViewById(R.id.txt_no_scale);
        LinearLayout linearLayoutScale = (LinearLayout)findViewById(R.id.layout_scale);
        if(isScaleAvailable){
            linearLayoutScale.setVisibility(View.VISIBLE);
            textViewNoScale.setVisibility(View.INVISIBLE);
            textViewNoScale.setVisibility(View.GONE);
        }else{
            textViewNoScale.setVisibility(View.VISIBLE);
            linearLayoutScale.setVisibility(View.INVISIBLE);
            linearLayoutScale.setVisibility(View.GONE);
        }

    }

    public void readWeight(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID > 0) {

            String in_xml = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";

            new ExecuteRSMAsync(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }

    }

    public void zeroScale(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_ZERO_SCALE,in_xml,sb,scannerID);
        }

    }

    public void resetScale(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_RESET_SCALE,in_xml,sb,scannerID);
        }

    }

    public void enableScale(View view) {

        clearText();
        ((TextView) findViewById(R.id.txtWeightMeasured)).setText("");
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_ENABLE_SCALE,in_xml,sb,scannerID);
        }
    }

    public void disableScale(View view) {

        clearText();
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_DISABLE_SCALE,in_xml,sb,scannerID);
        }
    }

    public void clearText(){
        ((TextView) findViewById(R.id.txtWeightMeasured)).setText("");
        ((TextView) findViewById(R.id.txtWeightUnit)).setText("");
        ((TextView) findViewById(R.id.txtWeightStatus)).setText("");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent navigateActivity = null;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.CurScannerId = Application.SCANNER_ID_NONE;
            finish();
            navigateActivity = new Intent(ScaleActivity.this, HomeActivity.class);
            startActivity(navigateActivity);

        } else if (id == R.id.nav_devices) {
            navigateActivity = new Intent(this, ScannersActivity.class);

            startActivity(navigateActivity);
        }else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder alertDialog = new  AlertDialog.Builder(this);
            alertDialog.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.CurScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(ScaleActivity.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                }
            });
            alertDialog.show();
        }else if (id == R.id.nav_connection_help) {
            navigateActivity = new Intent(this, ConnectionHelpActivity2.class);
            startActivity(navigateActivity);
        } else if (id == R.id.nav_settings) {
            navigateActivity = new Intent(this, SettingsActivity.class);
            startActivity(navigateActivity);
        } else if (id == R.id.nav_about) {
            navigateActivity = new Intent(this, AboutActivity.class);
            startActivity(navigateActivity);
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        drawerLayout.setSelected(true);
        return true;
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
        //pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        //pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        this.finish();
        Application.CurScannerId=Application.SCANNER_ID_NONE;
        Intent intent = new Intent(ScaleActivity.this,HomeActivity.class);
        startActivity(intent);
        return true;
    }
    private class ExecuteRSMAsync extends AsyncTask<String,Integer,Boolean>{
        private int scannerId = 0;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public ExecuteRSMAsync(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ScaleActivity.this, "Execute Command...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sbOutXml = new StringBuilder() ;
            boolean result = executeCommand(opcode, strings[0], sbOutXml, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT && result) {

                    try {
                        Log.i(TAG,sbOutXml.toString());

                        XmlPullParser parserForOutXml = Xml.newPullParser();

                        parserForOutXml.setInput(new StringReader(sbOutXml.toString()));
                        int event = parserForOutXml.getEventType();
                        String outXmlAttribute = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String outXmlElement = parserForOutXml.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    outXmlAttribute = parserForOutXml.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    Log.i(TAG,"Name of the end tag: "+outXmlElement);
                                    if(outXmlAttribute!=null) {

                                        if (outXmlElement.equals("weight")) {
                                            final String weight = outXmlAttribute.trim();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((TextView) findViewById(R.id.txtWeightMeasured)).setText(weight);
                                                }
                                            });
                                            Log.i(TAG, "Weight : " + weight);
                                        } else if (outXmlElement.equals("weight_mode")) {
                                            final String weightMode = outXmlAttribute.trim();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((TextView) findViewById(R.id.txtWeightUnit)).setText(weightMode);
                                                }
                                            });
                                            Log.i(TAG, "Weight Mode : " + weightMode);
                                        }else if (outXmlElement.equals("status")) {
                                            int status = Integer.parseInt(outXmlAttribute.trim());
                                            String scaleStatus = null;
                                            if(status == STATUS_SCALE_NOT_ENABLED){
                                                scaleStatus = "Scale Not Enabled";
                                            }else if(status == STATUS_SCALE_NOT_READY){
                                                scaleStatus = "Scale Not Ready";
                                            }else if(status == STATUS_STABLE_WEIGHT_OVER_LIMIT){
                                                scaleStatus = "Stable Weight OverLimit";
                                            }else if(status == STATUS_STABLE_WEIGHT_UNDER_ZERO){
                                                scaleStatus = "Stable Weight Under Zero";
                                            }else if(status == STATUS_NON_STABLE_WEIGHT){
                                                scaleStatus = "Non Stable Weight";
                                            }else if(status == STATUS_STABLE_ZERO_WEIGHT){
                                                scaleStatus = "Stable Zero Weight";
                                            }else if(status == STATUS_STABLE_NON_ZERO_WEIGHT){
                                                scaleStatus = "Stable NonZero Weight";
                                            }

                                            final String scale = scaleStatus;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((TextView) findViewById(R.id.txtWeightStatus)).setText(scale);
                                                }
                                            });
                                            Log.i(TAG, "status : " + status);
                                        }

                                    }
                                    break;
                            }
                            event = parserForOutXml.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG,e.toString());
                    }

            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

        }


    }


}

