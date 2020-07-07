package com.zebra.scannercontrol.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.DotsProgressBar;
import com.zebra.scannercontrol.app.helpers.SSASymbologyType;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_GET_SLOWEST_DECODE_IMAGE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_RESET_SSA_FOR_SYMBOLOGY_TYPE;
import static com.zebra.scannercontrol.app.activities.BatteryStatistics.attrs;

public class ScanSpeedAnalyticsActivity extends  BaseActivity implements  NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate, ScannerAppEngine.IScannerAppEngineDevEventsDelegate {

    static final int PERMISSIONS_REQUEST_WRITE_EX_STORAGE = 20;
    private static String NO_VALUE_TEXT = "-";
    private static int ACTIVE_LABEL_TEXT_COLOR = 0x000000;
    private static int ACTIVE_VALUE_TEXT_COLOR = 0x000000;
    private static int INACTIVE_LABEL_TEXT_COLOR = 0x000000;
    private static int INACTIVE_VALUE_TEXT_COLOR = 0x000000;
    private static int BARCHART_BAR_COLOR = 0x000000;
    private static int BARCHART_AXIS_COLOR = 0x000000;

    private boolean SSA_DATA_RETRIEVED_FLAG = false;
    private boolean IMAGE_RECIEVED_FLAG = false;
    private boolean IMAGE_SAVED_FLAG = false;

    int dialogResetSsaX = 50;
    int dialogResetSsaY = 170;
    int slowestImageGostImagePadding = 60;
    protected CustomProgressDialog getSlowetDecodeImageProgressDialog;

    //public static Bitmap bmp2;
    List<Integer> histogramValues =  Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0);

    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    //private TextView symbologyNameTextView;
    private TextView slowestDecodeImageLabelTextView;
    private TextView slowestDecodeTimeLabelTextView;
    private TextView slowestDecodeTimeValueTextView;
    private TextView slowestDecodeDataLabelTextView;
    private TextView slowestDecodeDataValueTextView;
    private TextView scanSpeedHistogramLabelTextView;
    private TextView totalScansLabelTextView;
    private TextView totalScansValueTextView;
    private TextView noSlowestImageTextView;
    private TextView barchartXAxisTextView;
    private TextView barchartYAxisTextView;
    private ImageView slowestDecodeImageImageView;
    private ImageButton saveImageButton;
    public static DotsProgressBar slowestDecodeImageLoadingDotsProgressBar;
    public static Dialog dialogSsaResetting;
    DotsProgressBar dotProgressBar;
    public static Bitmap retrievedSlowestDecodeImage = null;



    protected String slowestDecodeTimeValue = "-";
    protected String slowestDecodeDataValue = "-";
    protected String totalScansValue = "0";

    protected static boolean FLAG_NO_SSA_DATA = true;



    private int ssaStatus;
    private int scannerID;
    private String scannerName;
    private int barcodeType;
    private String barcodeTypeStr;
    private SSASymbologyType selectedSSASymbology;

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_speed_analytics);

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

        // link with UI
        //symbologyNameTextView = (TextView) findViewById(R.id.txt_ssa_symbology_name);
        slowestDecodeImageLabelTextView = (TextView) findViewById(R.id.txtSlowestDecodeImageLabel);
        slowestDecodeTimeLabelTextView = (TextView) findViewById(R.id.txtSlowestDecodeTimeLabel);
        slowestDecodeTimeValueTextView = (TextView) findViewById(R.id.txtSlowestDecodeTime);
        slowestDecodeDataLabelTextView = (TextView) findViewById(R.id.txtSlowestDecodeDataLabel);
        slowestDecodeDataValueTextView = (TextView) findViewById(R.id.txtSlowestDecodeData);
        scanSpeedHistogramLabelTextView = (TextView) findViewById(R.id.txtScanSpeedHistogramLabel);
        slowestDecodeImageLoadingDotsProgressBar = (DotsProgressBar) findViewById(R.id.dotProgressbar_slowestDecodeImage);
        slowestDecodeImageLoadingDotsProgressBar.setDotsCount(6);
        totalScansLabelTextView = (TextView) findViewById(R.id.txtTotalScanCountLabel);
        totalScansValueTextView = (TextView) findViewById(R.id.txtTotalScanCount);
        noSlowestImageTextView = (TextView) findViewById(R.id.txt_startScanHelpMessage);
        slowestDecodeImageImageView = (ImageView) findViewById(R.id.imgViewSlowestDecodeImage);
        saveImageButton = (ImageButton) findViewById(R.id.imgbtn_saveImage);
        barchartXAxisTextView = (TextView) findViewById(R.id.txtBarchartXAxisName);
        barchartYAxisTextView = (TextView) findViewById(R.id.txtBarchartYAxisName);

        slowestDecodeImageLoadingDotsProgressBar.setVisibility(View.GONE);
        saveImageButton.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.title_activity_scan_speed_analytics);
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

        // get values passed via Intent
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        selectedSSASymbology = (SSASymbologyType) getIntent().getSerializableExtra(Constants.SYMBOLOGY_SSA_ENABLED);
        scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);

        barcodeType = selectedSSASymbology.getAttrIDScanSpeedHistogram();
        barcodeTypeStr = selectedSSASymbology.getSymbologyName();

        // set Symbology Name
        subActionBar.setTitle("Scan Speed Analytics (" + barcodeTypeStr + ")");

        ACTIVE_LABEL_TEXT_COLOR = getResources().getColor(R.color.colorPrimary);
        INACTIVE_LABEL_TEXT_COLOR = getResources().getColor(R.color.light_gray);
        ACTIVE_VALUE_TEXT_COLOR = getResources().getColor(R.color.inactive_text);
        INACTIVE_VALUE_TEXT_COLOR = getResources().getColor(R.color.light_gray);
        BARCHART_BAR_COLOR = getResources().getColor(R.color.colorAccent);
        BARCHART_AXIS_COLOR = getResources().getColor(R.color.medium_gray);

        // set SSA data retrieved flag OFF
        SSA_DATA_RETRIEVED_FLAG = false;
        retrievedSlowestDecodeImage = null;

        // set image saved flag OFF
        IMAGE_SAVED_FLAG = false;

        // set image received flag OFF
        IMAGE_RECIEVED_FLAG = false;

        getSlowetDecodeImageProgressDialog = null;

        // update UI
        updateUI();
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
        ssaStatus = getIntent().getIntExtra(Constants.SSA_STATUS,0);

        // retrieve SSA data if not already retrieved
        if (!SSA_DATA_RETRIEVED_FLAG) {
            fetchSSA();
        } else {
            addDevEventsDelegate(this);
        }
    }

    /**
     * get Scan Speed Analytics information
     */
    private void fetchSSA() {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID != -1) {

            String in_xml = "<inArgs><scannerID>" + scannerID + " </scannerID><cmdArgs><arg-xml><attrib_list>";
            in_xml+=selectedSSASymbology.getAttrIDScanSpeedHistogram() + ",";
            in_xml+=selectedSSASymbology.getAttrIDMaxDecodeTime() + ",";
            in_xml+=selectedSSASymbology.getAttrIDSlowestDecodeData() + ",";
            in_xml+=selectedSSASymbology.getAttrIDDecodeCount();
            in_xml+= "</attrib_list></arg-xml></cmdArgs></inArgs>";

            new GetSSAAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Scan Speed Analytics Async Task class
     */
    private class GetSSAAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public GetSSAAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = new CustomProgressDialog(ScanSpeedAnalyticsActivity.this, "Please wait...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                if (result) {
                    try {
                        Log.i(TAG, sb.toString());
                        int i = 0;
                        int attrId = -1;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    Log.i(TAG, "Name of the end tag: " + name);
                                    if (name.equals("id")) {
                                        if (text != null) {
                                            attrId = Integer.parseInt(text.trim());
                                        }
                                        Log.i(TAG, "ID tag found: ID: " + attrId);
                                    } else if (name.equals("value")) {
                                        if(text!=null) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);

                                            if (selectedSSASymbology.getAttrIDMaxDecodeTime() == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        slowestDecodeTimeValue = attrVal;
                                                    }
                                                });

                                            } else if (selectedSSASymbology.getAttrIDSlowestDecodeData() == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        slowestDecodeDataValue = hexStringToString(attrVal);
                                                    }
                                                });

                                            } else if (selectedSSASymbology.getAttrIDDecodeCount() == attrId) {

                                                try {
                                                    if (Integer.parseInt(attrVal) > 0) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                FLAG_NO_SSA_DATA = false;
                                                                totalScansValue = attrVal;

                                                            }
                                                        });
                                                    } else {
                                                        FLAG_NO_SSA_DATA = true;
                                                    }
                                                } catch (Exception ex) {
                                                    FLAG_NO_SSA_DATA = true;
                                                }


                                            } else if (selectedSSASymbology.getAttrIDScanSpeedHistogram() == attrId) {

                                                histogramValues = getHistogramDataArray(attrVal);

                                            }
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            } else if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL) {
                if (result) {
                    try {
                        int i = 0;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    if (name.equals("attribute")) {
                                        if(text!=null) {
                                            attrs.add(text.trim());
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            } else if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET) {
                if (!result) {
                    return false;
                }
            }

            return result;
        }


        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // set SSA data flag ON
                    SSA_DATA_RETRIEVED_FLAG = true;

                    if (!FLAG_NO_SSA_DATA) {
                        addSelfToDevEventsDelegate();
                        getSlowestDecodeImage();
                    }
                    updateUI();
                }
            });
        }
    }

    /**
     * Update UI Elements
     */
    private void updateUI() {
        if (FLAG_NO_SSA_DATA) {
            slowestDecodeTimeValueTextView.setText(NO_VALUE_TEXT);
            slowestDecodeDataValueTextView.setText(NO_VALUE_TEXT);
            totalScansValueTextView.setText("0");
            noSlowestImageTextView.setVisibility(View.VISIBLE);
            saveImageButton.setVisibility(View.GONE);

            slowestDecodeImageLabelTextView.setTextColor(INACTIVE_LABEL_TEXT_COLOR);
            slowestDecodeTimeLabelTextView.setTextColor(INACTIVE_LABEL_TEXT_COLOR);
            slowestDecodeDataLabelTextView.setTextColor(INACTIVE_LABEL_TEXT_COLOR);
            scanSpeedHistogramLabelTextView.setTextColor(INACTIVE_LABEL_TEXT_COLOR);
            totalScansLabelTextView.setTextColor(INACTIVE_LABEL_TEXT_COLOR);

            slowestDecodeTimeValueTextView.setTextColor(INACTIVE_VALUE_TEXT_COLOR);
            slowestDecodeDataValueTextView.setTextColor(INACTIVE_VALUE_TEXT_COLOR);
            totalScansValueTextView.setTextColor(INACTIVE_VALUE_TEXT_COLOR);
            barchartXAxisTextView.setTextColor(INACTIVE_VALUE_TEXT_COLOR);
            barchartYAxisTextView.setTextColor(INACTIVE_VALUE_TEXT_COLOR);


        } else {
            slowestDecodeTimeValueTextView.setText(slowestDecodeTimeValue);
            slowestDecodeDataValueTextView.setText(slowestDecodeDataValue);
            totalScansValueTextView.setText(totalScansValue);
            //noSlowestImageTextView.setVisibility(View.GONE);
            //saveImageButton.setVisibility(View.VISIBLE);

            slowestDecodeImageLabelTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            slowestDecodeTimeLabelTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            slowestDecodeDataLabelTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            scanSpeedHistogramLabelTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            totalScansLabelTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);

            slowestDecodeTimeValueTextView.setTextColor(ACTIVE_VALUE_TEXT_COLOR);
            slowestDecodeDataValueTextView.setTextColor(ACTIVE_VALUE_TEXT_COLOR);
            totalScansValueTextView.setTextColor(ACTIVE_VALUE_TEXT_COLOR);
            barchartXAxisTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            barchartYAxisTextView.setTextColor(ACTIVE_LABEL_TEXT_COLOR);

        }
        if (histogramValues != null && histogramValues.size() > 0) {
            // Todo: set flag here
            //histogramValues = new ArrayList<Integer>();
            drawGraph(histogramValues);
        }
    }

    /**
     * get Slowest Decode Image Data
     */
    private void getSlowestDecodeImage(){
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute>" +
                "<id>" + RMD_ATTR_VALUE_GET_SLOWEST_DECODE_IMAGE + "</id><datatype>W</datatype><value>" + selectedSSASymbology.getAttrIDDecodeCount() + "</value>" +
                "</attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        new GetSlowetDecodeImageAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET).execute(new String[]{inXML});
    }

    /**
     * Async Task to get Slowest Decode Image Data
     */
    private class GetSlowetDecodeImageAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public GetSlowetDecodeImageAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    slowestDecodeImageLoadingDotsProgressBar.setVisibility(View.VISIBLE);
                    noSlowestImageTextView.setVisibility(View.GONE);
//                    noSlowestImageTextView.setText("Retrieving Image...");
//                    getSlowetDecodeImageProgressDialog = new CustomProgressDialog(ScanSpeedAnalyticsActivity.this, "Retrieving Image...");
//                    getSlowetDecodeImageProgressDialog.setCancelable(false);
//                    getSlowetDecodeImageProgressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result  = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                if (result) {
                    try {
                        Log.i(TAG, sb.toString());
                        int i = 0;
                        int attrId = -1;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    Log.i(TAG, "Name of the end tag: " + name);
                                    if(text!=null) {
                                        if (name.equals("id")) {
                                            attrId = Integer.parseInt(text.trim());
                                            Log.i(TAG, "ID tag found: ID: " + attrId);
                                        } else if (name.equals("value")) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);

                                            if (selectedSSASymbology.getAttrIDMaxDecodeTime() == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        slowestDecodeTimeValue = attrVal;
                                                    }
                                                });

                                            } else if (selectedSSASymbology.getAttrIDSlowestDecodeData() == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        slowestDecodeDataValue = hexStringToString(attrVal);
                                                    }
                                                });

                                            } else if (selectedSSASymbology.getAttrIDDecodeCount() == attrId) {

                                                try {
                                                    if (Integer.parseInt(attrVal) > 0) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                FLAG_NO_SSA_DATA = false;
                                                                totalScansValue = attrVal;

                                                            }
                                                        });
                                                    } else {
                                                        FLAG_NO_SSA_DATA = true;
                                                    }
                                                } catch (Exception ex) {
                                                    FLAG_NO_SSA_DATA = true;
                                                }


                                            } else if (selectedSSASymbology.getAttrIDScanSpeedHistogram() == attrId) {

                                                histogramValues = getHistogramDataArray(attrVal);

                                            }
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            } else if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL) {
                if (result) {
                    try {
                        int i = 0;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    if (name.equals("attribute")) {
                                        if(text!=null) {
                                            attrs.add(text.trim());
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            } else if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET) {
                if (!result) {//TODO :  check result and if error stop spinner
                    if (getSlowetDecodeImageProgressDialog != null && getSlowetDecodeImageProgressDialog.isShowing()) {
                        getSlowetDecodeImageProgressDialog.dismiss();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
        }
    }

    /**
     * Returns the histogram data as a byte array.
     * @param hexString
     * @return
     */
    public List<Integer> getHistogramDataArray(String hexString)
    {
        try
        {
            String[] split_values;
            byte[] HistVal = new byte[4];
            int counter = 0;
            List<byte[]>  HitogramValues = new ArrayList<byte[]>();

            if (hexString != null && !hexString.isEmpty() && (hexString.contains(" ")))
            {
                split_values = hexString.split(" ");
            }
            else
            {
                return null;
            }

            if (split_values.length % 4 != 0) return null; // missing data in input string


            for (String item : split_values)
            {
                HistVal[counter] = (byte) ((Character.digit(item.charAt(2), 16) << 4)  + Character.digit(item.charAt(3), 16));
                counter++;
                if (counter == 4)
                {
                    counter = 0;
                    HitogramValues.add(HistVal.clone());
                }

            }

            List<Integer> deciVals = new ArrayList<>();
            for (byte[] item : HitogramValues)
            {
                deciVals.add(byteArrayToInt(item));
            }
            return deciVals;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Change hex string to char string
     * @param s hex string to convert
     * @return
     */
    public static String hexStringToString(String s) {

        StringBuilder data = new StringBuilder();

        try {

            String[] split_list = s.split(" ");
            int len = split_list.length;
            if (len == 0) {
                return data.toString();
            }
            String dataLenStr = split_list[len - 1];
            char[] valPart = dataLenStr.substring(2).toCharArray();
            byte dataLen = (byte) ((Character.digit(valPart[0], 16) << 4)
                    + Character.digit(valPart[1], 16));

            for (int i = 0; i < dataLen; i ++) {
                char tempByte;
                valPart = split_list[i].substring(2).toCharArray();
                tempByte = (char) ((Character.digit(valPart[0], 16) << 4)
                        + Character.digit(valPart[1], 16));

                data.append(GetEscapeCode(tempByte));

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return data.toString();
    }

    /**
     * convert non printable characters to printable
     * @param character
     * @return
     */
    public static String GetEscapeCode(char character)
    {
        int asciiCode;
        asciiCode = (int)character;


        switch (asciiCode)
        {
            case 0: return "<NUL>";
            case 1: return "<SOH>";
            case 2: return "<STX>";
            case 3: return "<ETX>";
            case 4: return "<EOT>";
            case 5: return "<ENQ>";
            case 6: return "<ACK>";
            case 7: return "<BEL>";
            case 8: return "<BS>";
            case 10: return "<LF>";
            case 11: return "<VT>";
            case 12: return "<IF>";
            case 13: return "<Enter>";
            case 14: return "<SO>";
            case 15: return "<SI>";
            case 16: return "<DLE>";
            case 17: return "<DC1>";
            case 18: return "<DC2>";
            case 19: return "<DC3>";
            case 20: return "<DC4>";
            case 21: return "<NAK>";
            case 22: return "<SYN>";
            case 23: return "<ETB>";
            case 24: return "<CAN>";
            case 25: return "<EM>";
            case 26: return "<SUB>";
            case 27: return "<ESC>";
            case 28: return "<FS>";
            case 29: return "<GS>";
            case 30: return "<RS>";
            case 31: return "<US>";
            default: return String.valueOf(character);
        }
    }


    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }


    /**
     * Draw Histogram
     * @param histogramData histogram data
     */
    public void drawGraph(List<Integer> histogramData)
    {
        barChart = (BarChart) findViewById(R.id.barchart);

        //barChart.setDrawBarShadow(false);
        //barChart.setDrawValueAboveBar(true);
        //barChart.setMaxVisibleValueCount(100);

        //barChart.setDrawGridBackground(true);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setStartAtZero(true);
        barChart.getAxisRight().setStartAtZero(true);

        // stop touch events of the chart
        barChart.setTouchEnabled(false);
        // stop highlighting
        barChart.setHighlightPerDragEnabled(false);
        barChart.setHighlightPerTapEnabled(false);

        // disable zooming
        barChart.setPinchZoom(false);

        barChart.setNoDataText("");



        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        // Disable Legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        //String[] decodeTimeInts = new String[]{"", "\u226475", "\u2264110", "\u2264170", "\u2264300", "\u2264600", "\u22641000", "\u22641500",">1500","",};
        String[] decodeTimeInts = new String[]{"\u226475", "\u2264110", "\u2264170", "\u2264300", "\u2264600", "\u22641000", "\u22641500",">1500"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(decodeTimeInts));
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(8);
        xAxis.setAxisLineWidth(1.5f);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int iterator = 0;
        for (int element : histogramData) {
            float temp = (float) element;
            barEntries.add(new BarEntry(iterator, temp));
            iterator++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"");
        barDataSet.setValueFormatter(new IntValueFormatter());

        barDataSet.setColors(BARCHART_BAR_COLOR);         // set bar color
        BarData data = new BarData(barDataSet);

        if (FLAG_NO_SSA_DATA){
            xAxis.setAxisLineColor(INACTIVE_LABEL_TEXT_COLOR);
            xAxis.setTextColor(INACTIVE_LABEL_TEXT_COLOR);
            barDataSet.setValueTextSize(0f); // set bar data value
            slowestDecodeImageImageView.setEnabled(false);
            slowestDecodeImageImageView.setPadding(slowestImageGostImagePadding,slowestImageGostImagePadding,slowestImageGostImagePadding,slowestImageGostImagePadding);

        } else {
            xAxis.setAxisLineColor(BARCHART_AXIS_COLOR);
            xAxis.setTextColor(ACTIVE_LABEL_TEXT_COLOR);
            barDataSet.setValueTextSize(12f); // set bar data value
            slowestDecodeImageImageView.setPadding(slowestImageGostImagePadding,slowestImageGostImagePadding,slowestImageGostImagePadding,slowestImageGostImagePadding);
            slowestDecodeImageImageView.setEnabled(true);
            //slowestDecodeImageImageView.setScaleType(ImageView.ScaleType.CENTER);
        }

        data.setBarWidth(0.85f); // set bar width

        barChart.setData(data);

        barChart.setFitBars(true); // make the x-axis fit exactly all bars

        // re-draw chart
        barChart.invalidate();
    }

    public class MyXAxisValueFormatter extends ValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values){
            this.mValues = values;
        }
        @Override
        public String getFormattedValue(float value) {
            return mValues[(int) value];
        }
    }

    public class IntValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }

    /**
     * Save Image to Download folder
     * @param finalBitmap save bitmap image
     * @return
     */
    private boolean saveToInternalStorage(Bitmap finalBitmap) {

        boolean retVal =  false;
        if (finalBitmap != null && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            final String relativeLocation = Environment.DIRECTORY_PICTURES;

            final ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, getImageFileName());
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

            final ContentResolver resolver = this.getContentResolver();

            OutputStream stream = null;
            Uri uri = null;

            try {
                final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                uri = resolver.insert(contentUri, contentValues);

                if (uri == null) {
                    throw new IOException("Failed to create new MediaStore record.");
                }

                stream = resolver.openOutputStream(uri);

                if (stream == null) {
                    throw new IOException("Failed to get output stream.");
                }

                if (finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream) == false) {
                    throw new IOException("Failed to save bitmap.");
                }

                retVal = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                FileOutputStream outStream = null;
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(directory, fileName);
                outStream = new FileOutputStream(outFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);
                retVal = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return retVal;
    }

    /**
     * create and return slowest decode image file name to save
     * @return
     */
    private String getImageFileName() {
        //Slowest_Image_UPC_DS8178-DL0F007ZMWW_16145010503319_2017.12.15_05-54-39
        StringBuilder retFileName = new StringBuilder("Slowest_Image_");
        String symTypeName = this.barcodeTypeStr;
        if (symTypeName.equals("EAN/JAN")) {
            symTypeName = "EAN-JAN";
        }
        retFileName.append(symTypeName).append("_");
        // get values passed via Intent
        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        retFileName.append(scannerName).append("_");
        SimpleDateFormat s = new SimpleDateFormat("yyyy.MM.dd_hh-mm-ss", Locale.US);
        String currDateTime = s.format(new Date());
        retFileName.append(currDateTime).append(".jpg");
        int invalidCharIndex = retFileName.indexOf("/");
        return retFileName.toString();
    }

    /**
     * reset scan speed analytics data
     * @param view`
     */
    public void resetSSAStatisticsAction(View view){
         AlertDialog.Builder resetAlertBuilder = new AlertDialog.Builder(this);
         resetAlertBuilder.setTitle("Resetting Analytics");
         resetAlertBuilder.setMessage("Remove all Scan Speed Analytics from scanner.");
         resetAlertBuilder.setCancelable(true);

         resetAlertBuilder.setPositiveButton(
                "RESET",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        // TODO : the current symbology is re-setted. if needed do it for all supported symbology types
                        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" +
                                RMD_ATTR_VALUE_RESET_SSA_FOR_SYMBOLOGY_TYPE + "</id><datatype>W</datatype><value>" + selectedSSASymbology.getAttrIDDecodeCount() + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
                        StringBuilder outXML = new StringBuilder();

                        new ResetSSAStatisticsAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET,outXML).execute(new String[]{inXML});
                    }
                });

         resetAlertBuilder.setNegativeButton(
                "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        AlertDialog resetAlert = resetAlertBuilder.create();
         resetAlert.show();
    }

    /**
     * Async Task to Reset Slowest Decode Information
     */
    private class ResetSSAStatisticsAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        StringBuilder outXML;

        public ResetSSAStatisticsAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML){
            this.scannerId=scannerId;
            this.opcode=opcode;
            this.outXML = outXML;
        }
        @Override
        protected void onPreExecute() {
            // show resetting dot progress dialog
            showResetting();


        }

        @Override
        protected Boolean doInBackground(String... strings) {
            // TODO: delete later
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return  executeCommand(opcode,strings[0],outXML,scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(dialogSsaResetting!=null){
                dialogSsaResetting.dismiss();
                dialogSsaResetting =null;
            }
            if(!b){
                Toast.makeText(ScanSpeedAnalyticsActivity.this, "Resetting Analytics action failed", Toast.LENGTH_SHORT).show();
            } else {
                //success
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FLAG_NO_SSA_DATA = true;
                        removeSelfFromDevEventsDelegate();
                        //updateUI();
                        finish();
                        startActivity(getIntent());
                    }
                });

            }
        }
    }

    /**
     * add DevEventsDelegate entry
     */
    private void addSelfToDevEventsDelegate() {
        addDevEventsDelegate(this);
    }

    /**
     * remove DevEventsDelegate entry
     */
    private void removeSelfFromDevEventsDelegate() {
        removeDevEventsDelegate(this);
    }

    private void showResetting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ScannerControl","Show Resetting dialog");
                if(!isFinishing()) {
                    dialogSsaResetting = new Dialog(ScanSpeedAnalyticsActivity.this);
                    dialogSsaResetting.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogSsaResetting.setContentView(R.layout.dialog_ssa_resetting);
//                    TextView cancelButton = (TextView) dialogSsaResetting.findViewById(R.id.btn_cancel);
//                    // if decline button is clicked, close the custom dialog
//                    cancelButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            // Close dialog
//                            dialogSsaResetting.dismiss();
//                            dialogSsaResetting = null;
//                            //finish();
//                        }
//                    });

                    dotProgressBar = (DotsProgressBar) dialogSsaResetting.findViewById(R.id.progressBar);
                    dotProgressBar.setDotsCount(6);
                    Window window = dialogSsaResetting.getWindow();
                    if(window!=null) {
                        window.setLayout(getX(), getY());
                    }
                    dialogSsaResetting.setCancelable(false);
                    dialogSsaResetting.setCanceledOnTouchOutside(false);
                    dialogSsaResetting.show();
                }else{
                    finish();
                }
            }
        });
    }

    private int getX() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogResetSsaX * scale + 0.5f);
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }

    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogResetSsaY * scale + 0.5f);
        return y;
    }

    public void slowestImageSaveEvent(View view){
        if (!IMAGE_SAVED_FLAG && retrievedSlowestDecodeImage != null) {

            //------------------------------

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EX_STORAGE);
            } else {
                if (saveToInternalStorage(retrievedSlowestDecodeImage)) {
                    Toast.makeText(this, "Image saved to \'Pictures\' folder successfully!" ,Toast.LENGTH_SHORT).show();
                    saveImageButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "Image Saved Failed!" ,Toast.LENGTH_SHORT).show();
                }
            }
            //----------------------------------


        } else {
            // hide save button
            saveImageButton.setVisibility(View.GONE);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EX_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                    if (saveToInternalStorage(retrievedSlowestDecodeImage)) {
                        Toast.makeText(this, "Image saved to \'Download\' folder successfully!" ,Toast.LENGTH_SHORT).show();
                        saveImageButton.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "Image Saved Failed!" ,Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(dialogSsaResetting!=null) {
            Window window= dialogSsaResetting.getWindow();
            if(window!=null) {
                window.setLayout(getX(), getY());
            }
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
            intent = new Intent(ScanSpeedAnalyticsActivity.this, HomeActivity.class);
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
                    Intent intent = new Intent(ScanSpeedAnalyticsActivity.this, FindCabledScanner.class);
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
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        this.finish();
        Application.currentScannerId =Application.SCANNER_ID_NONE;
        Intent intent = new Intent(ScanSpeedAnalyticsActivity.this,HomeActivity.class);
        startActivity(intent);
        return true;
    }


    // IScannerAppEngineDevEventsDelegate Events
    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {
    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
    }

    @Override
    public void  scannerImageEvent(byte[] imageData) {

        if (!IMAGE_RECIEVED_FLAG) {

            final byte[] tempImgData = imageData;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getSlowetDecodeImageProgressDialog != null && getSlowetDecodeImageProgressDialog.isShowing()) {
                        getSlowetDecodeImageProgressDialog.dismiss();
                    }

                    slowestDecodeImageImageView.setPadding(0, 0, 0, 0);
                    saveImageButton.setVisibility(View.VISIBLE);
                    noSlowestImageTextView.setVisibility(View.GONE);
                    slowestDecodeImageLoadingDotsProgressBar.setVisibility(View.GONE);
                    //slowestDecodeImageImageView.setScaleType(ImageView.ScaleType.CENTER);
                    retrievedSlowestDecodeImage = BitmapFactory.decodeByteArray(tempImgData, 0, tempImgData.length);

                    //slowestDecodeImageImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp2, 200, 150, false));
                    slowestDecodeImageImageView.setImageBitmap(retrievedSlowestDecodeImage);
                    IMAGE_RECIEVED_FLAG = true;

                    slowestDecodeImageImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ScanSpeedAnalyticsActivity.this, DecodeImageActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });
        }

    }
    @Override
    public void  scannerVideoEvent(byte[] videoData) {
    }
}
