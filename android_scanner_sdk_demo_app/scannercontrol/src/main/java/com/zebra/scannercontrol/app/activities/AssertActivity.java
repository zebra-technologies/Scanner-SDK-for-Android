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
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_MODEL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_SERIAL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_FW_VERSION;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_CONFIG_NAME;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_DOM;


/**
 * Created by pndv47 on 1/30/2015.
 */
public class AssertActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    int scannerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assertinfo);

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
        if(actionBar!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Asset Information");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);

        fetchAssertInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        ((TextView) findViewById(R.id.txt_scanner_name)).setText(scannerName.trim());
        fetchAssertInfo();
    }

    private void fetchAssertInfo() {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID != -1) {

            String in_xml = "<inArgs><scannerID>" + scannerID + " </scannerID><cmdArgs><arg-xml><attrib_list>";
            in_xml+=RMD_ATTR_MODEL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_SERIAL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_FW_VERSION;
            in_xml+=",";
            in_xml+=RMD_ATTR_CONFIG_NAME;
            in_xml+=",";
            in_xml+=RMD_ATTR_DOM;
            in_xml += "</attrib_list></arg-xml></cmdArgs></inArgs>";

            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
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
            intent = new Intent(AssertActivity.this, HomeActivity.class);
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
                    Intent intent = new Intent(AssertActivity.this, FindCabledScanner.class);
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

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean>{
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public MyAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(AssertActivity.this, "Execute Command...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder() ;
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                if (result) {
                    try {
                        Log.i(TAG,sb.toString());
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
                                    Log.i(TAG,"Name of the end tag: "+name);
                                    if(text!=null) {
                                        if (name.equals("id")) {
                                            attrId = Integer.parseInt(text.trim());
                                            Log.i(TAG, "ID tag found: ID: " + attrId);
                                        } else if (name.equals("value")) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);
                                            if (RMD_ATTR_MODEL_NUMBER == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) findViewById(R.id.txtModel)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_SERIAL_NUMBER == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) findViewById(R.id.txtSerial)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_FW_VERSION == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) findViewById(R.id.txtFW)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_DOM == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) findViewById(R.id.txtDOM)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_CONFIG_NAME == attrId) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) findViewById(R.id.txtConfigName)).setText(attrVal);
                                                    }
                                                });

                                            }
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG,e.toString());
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

        }


    }
}
