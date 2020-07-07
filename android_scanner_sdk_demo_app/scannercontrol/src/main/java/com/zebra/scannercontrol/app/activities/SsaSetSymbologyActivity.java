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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.adapters.HintAdapter;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.SSASymbologyType;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import java.util.List;

import static com.zebra.scannercontrol.RMDAttributes.NUM_STATUS_UNUSED_HEX_LI;

public class SsaSetSymbologyActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate{

    // local variables
    List<Integer> supportedSSI = null;
    List<SSASymbologyType> ssaSupportedSymbologyList = null;
    private CustomProgressDialog progressDialog;
    private int scannerID;

    // static variables
    public static boolean SSA_SYMBOLOGY_ENABLED_FLAG = false;
    public static SSASymbologyType SSA_ENABLED_SYMBOLOGY_OBJECT = null;

    // UI element variables
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;

    Spinner spinnerSymbologyType = null;
    Button btnEnable = null;
    TextView textSelectScanner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssa_set_symbology);

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

        // get scanner ID and Scan speed analytics supported symbology ID list from intent
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        supportedSSI = getIntent().getIntegerArrayListExtra(Constants.SYMBOLOGY_SSA);

        // bind with Enable button
        btnEnable = (Button) findViewById(R.id.btn_ssa_enable_symbology);

        textSelectScanner = (TextView) findViewById(R.id.textView3);

        // bind with spinner control to display SSA supported symbology list
        spinnerSymbologyType = (Spinner) findViewById(R.id.spinnerSymbology);
        spinnerSymbologyType.setOnItemSelectedListener(this);

        // get SSA supported SSASymbologyType list from supported symbology ID list
        ssaSupportedSymbologyList = SSASymbologyType.getSSASymbologyList(supportedSSI);
        // add spinner hint as last item
        ssaSupportedSymbologyList.add(0,new SSASymbologyType("Select a Symbology"));

        // set values to spinner control
        HintAdapter adapter1 = new HintAdapter(this, ssaSupportedSymbologyList, android.R.layout.simple_spinner_item){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    //tv.setVisibility(View.GONE);
                    tv.setTextColor(ContextCompat.getColor(SsaSetSymbologyActivity.this, R.color.inactive_text));
                }
                else {
                    tv.setTextColor(ContextCompat.getColor(SsaSetSymbologyActivity.this, R.color.font_color));
                }
                return view;
            }
        };
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymbologyType.setAdapter(adapter1);
        // show hint (added  at the end of the list)
        spinnerSymbologyType.setSelection(0);


    }

    /**
     * Enable button press event
     * @param view
     */
    public void enableSSASymbology(View view) {
        btnEnable = (Button) findViewById(R.id.btn_ssa_enable_symbology);
        if(btnEnable != null && btnEnable.isEnabled()){
            SSA_SYMBOLOGY_ENABLED_FLAG = true;
            SSA_ENABLED_SYMBOLOGY_OBJECT = (SSASymbologyType) spinnerSymbologyType.getSelectedItem();
            resetStatistics();
            setSymbologyConfiguration();
            setThresholddValue();
            displaySSAView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);

        int ssaStatus = getIntent().getIntExtra(Constants.SSA_STATUS,0);
        TextView textView = (TextView)findViewById(R.id.txt_no_ssa);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout_Scan_Speed_Analitics);
        if(ssaStatus == 1 ){
            textView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.GONE);
        }else{
            linearLayout.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.GONE);
        }
    }
    /**
     * Reset Scan Speed Analytics information for selected symbology type
     */
    private void resetStatistics(){
        SSASymbologyType selectedSymbology = (SSASymbologyType) spinnerSymbologyType.getSelectedItem();
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" +
                5006 + "</id><datatype>W</datatype><value>" + selectedSymbology.getAttrIDDecodeCount() + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();

        new EnableButtonAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET,outXML).execute(new String[]{inXML});
    }

    /**
     * Set Slowest Decode Image settings
     */
    private void setSymbologyConfiguration() {

        SSASymbologyType selectedSymbology = (SSASymbologyType) spinnerSymbologyType.getSelectedItem();
        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute>" +
                "<id>" + 1755 + "</id><datatype>A</datatype><value>" + selectedSymbology.getAttrIDDecodeCountHexValue()+" "+ NUM_STATUS_UNUSED_HEX_LI + " "+ NUM_STATUS_UNUSED_HEX_LI +" "+ NUM_STATUS_UNUSED_HEX_LI + "</value>" +
                "</attribute></attrib_list></arg-xml></cmdArgs></inArgs>";

        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        StringBuilder outXML = new StringBuilder();

        new EnableButtonAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_STORE,outXML).execute(new String[]{inXML});
    }

    /**
     * Set Threshold value
     */
    private void setThresholddValue(){
        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>1756</id><datatype>W</datatype><value>" + 0 + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        new EnableButtonAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET,outXML).execute(new String[]{inXML});
    }

    /**
     * Async task for Enable button pressed process
     */
    private class EnableButtonAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        View view;
        StringBuilder outXML;
        String scannerName;

        public EnableButtonAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        public EnableButtonAsyncTask(int scannerId, String scannerName, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, View _view) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.view = _view;
            this.scannerName = scannerName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = false;
            try {
                result = executeCommand(opcode,strings[0],outXML,scannerId);
            } catch (Exception e) {
                //dismissDialog();
            }
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET) {
                if (result) {
                    try {
                        Log.i(TAG, outXML.toString());
                     } catch (Exception e) {
                        Log.e(TAG, e.toString());
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

            if(!b){
                if(opcode==DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET){
                    Toast.makeText(SsaSetSymbologyActivity.this, "Cannot perform the action", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // set scan speed analytics enable flag ON
            SSA_SYMBOLOGY_ENABLED_FLAG = true;
        }
    }

    /**
     * Navigate to Scan Speed Analytic(details) view
     */
    public void displaySSAView()
    {
        Intent intent = new Intent(this, ScanSpeedAnalyticsActivity.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SYMBOLOGY_SSA_ENABLED, SSA_ENABLED_SYMBOLOGY_OBJECT);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Enable symbology enable button
        long gg = spinnerSymbologyType.getSelectedItemId();
        if (spinnerSymbologyType.getCount() == 0 || spinnerSymbologyType.getSelectedItemId() == 0) {
            btnEnable.setEnabled(false);
            textSelectScanner.setText("");

            //String item = (String) parent.getItemAtPosition(position);
            //((TextView) parent.getChildAt(0)).setTextColor(0x00000000);
        } else {
            btnEnable.setEnabled(true);
            textSelectScanner.setText("Select a Symbology");

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Disable symbology enable button
        btnEnable.setEnabled(false);
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
            intent = new Intent(SsaSetSymbologyActivity.this, HomeActivity.class);
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
                    Intent intent = new Intent(SsaSetSymbologyActivity.this, FindCabledScanner.class);
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

    /**
     * set reset scan speed analytics flag to OFF
     */
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
        Intent intent = new Intent(SsaSetSymbologyActivity.this,HomeActivity.class);
        startActivity(intent);
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }
    public static void resetScanSpeedAnalyticSettings(){
        // set scan speed analytics enable flag OFF
        SSA_SYMBOLOGY_ENABLED_FLAG = false;
    }
}
