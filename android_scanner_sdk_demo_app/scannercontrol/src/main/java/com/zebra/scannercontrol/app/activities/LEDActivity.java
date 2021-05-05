package com.zebra.scannercontrol.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
import android.widget.TableRow;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.R;

import static com.zebra.scannercontrol.app.helpers.Constants.SCANNER_MODEL_CS4070;

public class LEDActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate  {

    String scannerName;
    private int scannerID;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

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
            actionBar.setTitle(R.string.title_activity_led);
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

        scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if(scannerName.startsWith("RFD")){
            TableRow amberLED=(TableRow) findViewById(R.id.tbl_row_amber_led);
            TableRow redLED =(TableRow) findViewById(R.id.tbl_row_red_led);
            if(amberLED!=null) amberLED.setEnabled(false);
            if(redLED!=null) redLED.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
    }

    public void greenLEDOnClicked(View view) {
        prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_GREEN_ON);
    }

    public void greenLEDOffClicked(View view) {
        prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_GREEN_OFF);
    }

    public void otherLEDOnClicked(View view) {
        if(scannerName.startsWith(SCANNER_MODEL_CS4070)){
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_RED_ON);
        }else {
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_OTHER_ON);
        }
    }

    public void otherLEDOffClicked(View view) {
        if(scannerName.startsWith(SCANNER_MODEL_CS4070)){
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_RED_OFF);
        }else {
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_OTHER_OFF);
        }
    }

    public void redLEDOnClicked(View view) {
        if(scannerName.startsWith(SCANNER_MODEL_CS4070)){
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_OTHER_ON);
        }else{
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_RED_ON);

        }

    }

    public void redLEDOffClicked(View view) {
        if(scannerName.startsWith(SCANNER_MODEL_CS4070)){
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_OTHER_OFF);
        }else {
            prepareInXML(RMDAttributes.RMD_ATTR_VALUE_ACTION_LED_RED_OFF);
        }
    }

    private void performLedAction(String inXML) {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        if (scannerID != -1) {
            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION).execute(new String[]{inXML});
        } else {
            Toast.makeText(this, "Invalid scanner ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareInXML(int value){
        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, -1) + "</scannerID><cmdArgs><arg-int>" +
                value + "</arg-int></cmdArgs></inArgs>";
        performLedAction(inXML);
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
            intent = new Intent(LEDActivity.this, HomeActivity.class);
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
                    Intent intent = new Intent(LEDActivity.this, FindCabledScanner.class);
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
    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean> {
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
            progressDialog = new CustomProgressDialog(LEDActivity.this, "Execute Command...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            if(scannerName.startsWith(SCANNER_MODEL_CS4070)){
                return  executeSSICommand(opcode,strings[0],null,scannerId);
            }else {
                return  executeCommand(opcode,strings[0],null,scannerId);
            }

        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!b){
                Toast.makeText(LEDActivity.this, "Cannot perform LED Action", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
