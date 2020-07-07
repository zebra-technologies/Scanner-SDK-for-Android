package com.zebra.scannercontrol.app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DISABLE_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ENABLE_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RESET_SCALE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ZERO_SCALE;
import static com.zebra.scannercontrol.app.helpers.Constants.*;

public class ScaleActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {


    public static Bitmap retrievedDecodeImage = null;
    private ImageView SnapiImageView;
    private Switch scaleEnableSwitch, liveWeightEnableSwitch;

    private Button readWeightButton, zeroScaleButton, resetScaleButton;

    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private int scannerID;
    private boolean liveWeightEnable;

    private int STATUS_SCALE_NOT_ENABLED = 0;
    private int STATUS_SCALE_NOT_READY = 1;
    private int STATUS_STABLE_WEIGHT_OVER_LIMIT = 2;
    private int STATUS_STABLE_WEIGHT_UNDER_ZERO = 3;
    private int STATUS_NON_STABLE_WEIGHT = 4;
    private int STATUS_STABLE_ZERO_WEIGHT = 5;
    private int STATUS_STABLE_NON_ZERO_WEIGHT = 6;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);

        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (configuration.smallestScreenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (configuration.screenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Toolbar subActionBar = (Toolbar) findViewById(R.id.sub_actionbar);
        setSupportActionBar(subActionBar);
        readWeightButton = findViewById(R.id.read_weight_button);
        zeroScaleButton = findViewById(R.id.zero_scale_button);
        resetScaleButton = findViewById(R.id.reset_scale_button);
        liveWeightEnableSwitch = findViewById(R.id.live_weight_enable_switch);
        scaleEnableSwitch = findViewById(R.id.scale_enable_switch);
        scaleEnableSwitch.setChecked(true);

        liveWeightEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (liveWeightEnableSwitch.isChecked()) {
                    readWeightButton.setEnabled(false);
                    liveWeightEnable = true;
                    liveWeight();
                } else {
                    readWeightButton.setEnabled(true);
                    liveWeightEnable = false;
                }
            }

        });


        scaleEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (scaleEnableSwitch.isChecked()) {
                    enableScale();
                    readWeightButton.setEnabled(true);
                    zeroScaleButton.setEnabled(true);
                    resetScaleButton.setEnabled(true);
                    liveWeightEnableSwitch.setEnabled(true);

                } else {
                    disableScale();
                    liveWeightEnableSwitch.setEnabled(false);
                    readWeightButton.setEnabled(false);
                    zeroScaleButton.setEnabled(false);
                    resetScaleButton.setEnabled(false);
                }
            }

        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.scale);
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
    public void onBackPressed() {
        super.onBackPressed();
        liveWeightEnable = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
        liveWeightEnable = false;
        //removeDevEventsDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
        boolean isScaleAvailable = getIntent().getBooleanExtra(Constants.SCALE_STATUS, false);
        TextView textViewNoScale = (TextView) findViewById(R.id.txt_no_scale);
        LinearLayout linearLayoutScale = (LinearLayout) findViewById(R.id.layout_scale);
        liveWeightEnable = false;
        if (isScaleAvailable) {
            linearLayoutScale.setVisibility(View.VISIBLE);
            textViewNoScale.setVisibility(View.INVISIBLE);
            textViewNoScale.setVisibility(View.GONE);


        } else {
            textViewNoScale.setVisibility(View.VISIBLE);
            linearLayoutScale.setVisibility(View.INVISIBLE);
            linearLayoutScale.setVisibility(View.GONE);
        }

    }

    public void liveWeight() {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID + getIntent().getIntExtra(Constants.SCANNER_ID, 0) +XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;

            new ExecuteRSMAsyncLiveWeight(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    public void readWeight(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID  + getIntent().getIntExtra(Constants.SCANNER_ID, 0) +XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;

            new ExecuteRSMAsync(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }

    }

    public void zeroScale(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID  + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_ZERO_SCALE, in_xml, sb, scannerID);
        }

    }

    public void resetScale(View view) {

        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID  + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_RESET_SCALE, in_xml, sb, scannerID);
        }

    }

    public void enableScale() {

        clearText();
        ((TextView) findViewById(R.id.txtWeightMeasured)).setText("");
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID  + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_ENABLE_SCALE, in_xml, sb, scannerID);
        }
    }

    public void disableScale() {

        clearText();
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        StringBuilder sb = new StringBuilder();
        if (scannerID > 0) {

            String in_xml = XMLTAG_ARGXML+XMLTAG_SCANNER_ID  + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + XMLTAG_SCANNER_ID+ XMLTAG_ARGXML;
            Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_DISABLE_SCALE, in_xml, sb, scannerID);
        }
    }

    public void clearText() {
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
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            finish();
            navigateActivity = new Intent(ScaleActivity.this, HomeActivity.class);
            startActivity(navigateActivity);

        } else if (id == R.id.nav_devices) {
            navigateActivity = new Intent(this, ScannersActivity.class);

            startActivity(navigateActivity);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
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
        } else if (id == R.id.nav_connection_help) {
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
        Application.currentScannerId = Application.SCANNER_ID_NONE;
        Intent intent = new Intent(ScaleActivity.this, HomeActivity.class);
        startActivity(intent);
        return true;
    }

    private class ExecuteRSMAsync extends AsyncTask<String, Integer, Boolean> {
        private int scannerId = 0;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public ExecuteRSMAsync(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
            this.scannerId = scannerId;
            this.opcode = opcode;
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
            StringBuilder sbOutXml = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sbOutXml, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT && result) {

                try {
                    Log.i(TAG, sbOutXml.toString());

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
                                Log.i(TAG, "Name of the end tag: " + outXmlElement);
                                if (outXmlAttribute != null) {

                                    if (outXmlElement.equals(WEIGHT_XML_ELEMENT)) {
                                        final String weight = outXmlAttribute.trim();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.txtWeightMeasured)).setText(weight);
                                            }
                                        });
                                        Log.i(TAG, "Weight : " + weight);
                                    } else if (outXmlElement.equals(WEIGHT_MODE_XML_ELEMENT)) {
                                        final String weightMode = outXmlAttribute.trim();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.txtWeightUnit)).setText(weightMode);
                                            }
                                        });
                                        Log.i(TAG, "Weight Mode : " + weightMode);
                                    } else if (outXmlElement.equals(WEIGHT_STATUS_XML_ELEMENT)) {
                                        int status = Integer.parseInt(outXmlAttribute.trim());
                                        String scaleStatus = null;
                                        if (status == STATUS_SCALE_NOT_ENABLED) {
                                            scaleStatus = SCALE_STATUS_SCALE_NOT_ENABLED;
                                        } else if (status == STATUS_SCALE_NOT_READY) {
                                            scaleStatus = SCALE_STATUS_SCALE_NOT_READY ;
                                        } else if (status == STATUS_STABLE_WEIGHT_OVER_LIMIT) {
                                            scaleStatus = SCALE_STATUS_STABLE_WEIGHT_OVER_LIMIT ;
                                        } else if (status == STATUS_STABLE_WEIGHT_UNDER_ZERO) {
                                            scaleStatus = SCALE_STATUS_STABLE_WEIGHT_UNDER_ZERO;
                                        } else if (status == STATUS_NON_STABLE_WEIGHT) {
                                            scaleStatus = SCALE_STATUS_NON_STABLE_WEIGHT;
                                        } else if (status == STATUS_STABLE_ZERO_WEIGHT) {
                                            scaleStatus = SCALE_STATUS_STABLE_ZERO_WEIGHT;
                                        } else if (status == STATUS_STABLE_NON_ZERO_WEIGHT) {
                                            scaleStatus = SCALE_STATUS_STABLE_NON_ZERO_WEIGHT;
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
                    Log.e(TAG, e.toString());
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

    private class ExecuteRSMAsyncLiveWeight extends AsyncTask<String, Integer, Boolean> {
        private int scannerId = 0;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public ExecuteRSMAsyncLiveWeight(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
            this.scannerId = scannerId;
            this.opcode = opcode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean result;
            do {
                StringBuilder sbOutXml = new StringBuilder();
                result = executeCommand(opcode, strings[0], sbOutXml, scannerId);
                if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT && result) {

                    try {
                        Log.i(TAG, sbOutXml.toString());

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
                                    Log.i(TAG, "Name of the end tag: " + outXmlElement);
                                    if (outXmlAttribute != null) {

                                        if (outXmlElement.equals(WEIGHT_XML_ELEMENT)) {
                                            final String weight = outXmlAttribute.trim();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((TextView) findViewById(R.id.txtWeightMeasured)).setText(weight);
                                                }
                                            });
                                            Log.i(TAG, "Weight : " + weight);
                                        } else if (outXmlElement.equals(WEIGHT_MODE_XML_ELEMENT)) {
                                            final String weightMode = outXmlAttribute.trim();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((TextView) findViewById(R.id.txtWeightUnit)).setText(weightMode);
                                                }
                                            });
                                            Log.i(TAG, "Weight Mode : " + weightMode);
                                        } else if (outXmlElement.equals("status")) {
                                            int status = Integer.parseInt(outXmlAttribute.trim());
                                            String scaleStatus = null;
                                            if (status == STATUS_SCALE_NOT_ENABLED) {
                                                scaleStatus = SCALE_STATUS_SCALE_NOT_ENABLED;
                                            } else if (status == STATUS_SCALE_NOT_READY) {
                                                scaleStatus = SCALE_STATUS_SCALE_NOT_READY ;
                                            } else if (status == STATUS_STABLE_WEIGHT_OVER_LIMIT) {
                                                scaleStatus = SCALE_STATUS_STABLE_WEIGHT_OVER_LIMIT ;
                                            } else if (status == STATUS_STABLE_WEIGHT_UNDER_ZERO) {
                                                scaleStatus = SCALE_STATUS_STABLE_WEIGHT_UNDER_ZERO;
                                            } else if (status == STATUS_NON_STABLE_WEIGHT) {
                                                scaleStatus = SCALE_STATUS_NON_STABLE_WEIGHT;
                                            } else if (status == STATUS_STABLE_ZERO_WEIGHT) {
                                                scaleStatus = SCALE_STATUS_STABLE_ZERO_WEIGHT;
                                            } else if (status == STATUS_STABLE_NON_ZERO_WEIGHT) {
                                                scaleStatus = SCALE_STATUS_STABLE_NON_ZERO_WEIGHT;
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
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }


            } while (liveWeightEnable);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

        }


    }

}

