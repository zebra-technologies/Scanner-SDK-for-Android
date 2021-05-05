package com.zebra.scannercontrol.app.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.adapters.AvailableScannerListAdapter;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.AvailableScanner;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.receivers.NotificationsReceiver;

import java.util.ArrayList;
import java.util.Collections;

public class ScannersActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevListDelegate {

    AvailableScannerListAdapter lastConnectedScannerListAdapter;
    ListView lastConnectedScanner;
    ArrayList<AvailableScanner> lastConnectedScannerList;

    AvailableScannerListAdapter availableScannerListAdapter;
    ListView otherScanners;
    ArrayList<AvailableScanner> scannersList;

    // Member fields
    private CustomProgressDialog progressDialog;

    static  AvailableScanner curAvailableScanner=null;
    int scannerId;

    static MyAsyncTask cmdExecTask=null;
    private int REFRESH = 1;
    private int EVENT = 2;
    private int CREATE = 3;

    static boolean launchFromFCS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_scanners);
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
        addDevListDelegate(this);
        configureNotificationAvailable(true);

        otherScanners = (ListView) findViewById(R.id.other_scanners);
        scannersList = new ArrayList<AvailableScanner>();
        availableScannerListAdapter = new AvailableScannerListAdapter(this,scannersList);
        otherScanners.setAdapter(availableScannerListAdapter);
        otherScanners.setOnItemClickListener(mDeviceClickListener);
        otherScanners.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lastConnectedScanner = (ListView) findViewById(R.id.last_connected_scanner);
        lastConnectedScannerList = new ArrayList<AvailableScanner>();
        lastConnectedScannerListAdapter = new AvailableScannerListAdapter(this,lastConnectedScannerList);
        lastConnectedScanner.setAdapter(lastConnectedScannerListAdapter);
        lastConnectedScanner.setOnItemClickListener(mDeviceClickListenerLastConnected);
        lastConnectedScanner.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        UpdateScannerListView(CREATE);
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        if (nMgr != null) {
            nMgr.cancel(NotificationsReceiver.DEFAULT_NOTIFICATION_ID);
        }

        launchFromFCS = getIntent().getBooleanExtra(Constants.LAUNCH_FROM_FCS, false);
     }


    private final Handler UpdateScannersHandler = new Handler() {

        public void handleMessage(Message msg) {
            updateScannersList();
            scannersList.clear();
            lastConnectedScannerList.clear();
            boolean enableLastScannerConnection = false;
            if(Application.lastConnectedScanner !=null) {
                DCSScannerInfo device = Application.lastConnectedScanner;
                addToLastConnectedScannerList(new AvailableScanner(device.getScannerID(), device.getScannerName(), device.getScannerHWSerialNumber(), false, device.isAutoCommunicationSessionReestablishment(), device.getConnectionType()));
            }
            for(DCSScannerInfo device:getActualScannersList()){
                if(device.isActive()){
                    AvailableScanner availableScanner = new AvailableScanner(device.getScannerID(),device.getScannerName(), device.getScannerHWSerialNumber(),true,device.isAutoCommunicationSessionReestablishment(),device.getConnectionType());
                    Application.currentConnectedScanner = device;
                    Application.lastConnectedScanner = Application.currentConnectedScanner;
                    availableScanner.setIsConnectable(true);
                    addToLastConnectedScannerList(availableScanner);
                    enableLastScannerConnection = true;
                }else
                {
                    AvailableScanner availableScanner = new AvailableScanner(device.getScannerID(),device.getScannerName(), device.getScannerHWSerialNumber(),false,device.isAutoCommunicationSessionReestablishment(),device.getConnectionType());
                    availableScanner.setIsConnectable(true);
                    addToAvailableScannerList(availableScanner);
                }
            }
            //-- CDC readers -- TODO: Available CDC device list

            //-- CDC readers --

            Collections.sort(scannersList);

            AvailableScanner currentConnectedScannerX = null;

            if(Application.lastConnectedScanner !=null) {
                AvailableScanner lastConnectedScanner = null;
                for (AvailableScanner scanner : scannersList) {
                    if (scanner.getScannerId() == Application.lastConnectedScanner.getScannerID()) {
                        lastConnectedScanner = scanner;
                        scanner.setIsConnectable(true);
                        addToLastConnectedScannerList(scanner);
                        enableLastScannerConnection = true;
                        break;
                    }

                    if(Application.currentConnectedScanner != null){
                        if(null != Application.currentConnectedScanner.getScannerHWSerialNumber() && Application.currentConnectedScanner.getScannerHWSerialNumber().equals(scanner.getScannerAddress())){
                            currentConnectedScannerX = scanner;
                        }
                    }
                }
                if (lastConnectedScanner != null) {
                    scannersList.remove(lastConnectedScanner);
                }
            }

            if(currentConnectedScannerX != null){
                scannersList.remove(currentConnectedScannerX);
            }

            ListView lvLastConnectedScanner= (ListView)findViewById(R.id.last_connected_scanner);
            if (lvLastConnectedScanner != null) {
                lvLastConnectedScanner.setEnabled(enableLastScannerConnection);
            }



            if (availableScannerListAdapter!=null) {
                availableScannerListAdapter.notifyDataSetChanged();
            }
            if (lastConnectedScannerListAdapter!=null) {
                lastConnectedScannerListAdapter.notifyDataSetChanged();
            }
            ListView lv= (ListView)findViewById(R.id.other_scanners);
            View ll= (View)findViewById(R.id.noScannersMessage);
            TableRow tblROwLastScannerConnected = (TableRow)findViewById(R.id.tbl_row_last_connected_scanner);

            if (availableScannerListAdapter.getCount()==0 && lastConnectedScannerListAdapter.getCount() == 0)
            {

                lv.setVisibility(View.GONE);
                ll.setVisibility(View.VISIBLE);

            }
            else
            {
                lv.setVisibility(View.VISIBLE);
                ll.setVisibility(View.GONE);
            }

            if(lastConnectedScannerListAdapter.getCount() == 0){
                tblROwLastScannerConnected.setVisibility(View.GONE);
            }else{
                tblROwLastScannerConnected.setVisibility(View.VISIBLE);
            }
            TextView lastConnectedScannerTxt = (TextView) findViewById(R.id.txt_last_connected_scanner);
            if (lastConnectedScannerTxt != null) {
                lastConnectedScannerTxt.setText("Last Connected Scanner");
            }

            if(lastConnectedScannerList.size()>0){
                if(lastConnectedScannerList.get(0).isConnected()){
                    if(msg.what ==EVENT) {
                        if (cmdExecTask == null || AsyncTask.Status.RUNNING != cmdExecTask.getStatus()) {
                            Application.currentScannerName = lastConnectedScannerList.get(0).getScannerName();
                            Application.currentScannerAddress = lastConnectedScannerList.get(0).getScannerAddress();
                            Application.currentScannerId = lastConnectedScannerList.get(0).getScannerId();
                            Application.currentAutoReconnectionState = lastConnectedScannerList.get(0).isAutoReconnection();
                            Intent intent = new Intent(ScannersActivity.this, ActiveScannerActivity.class);
                            intent.putExtra(Constants.SCANNER_NAME, Application.currentScannerName);
                            intent.putExtra(Constants.SCANNER_ADDRESS, Application.currentScannerAddress);
                            intent.putExtra(Constants.SCANNER_ID, Application.currentScannerId);
                            intent.putExtra(Constants.AUTO_RECONNECTION, Application.currentAutoReconnectionState);
                            intent.putExtra(Constants.CONNECTED, true);
                            intent.putExtra(Constants.SHOW_BARCODE_VIEW, false);
                            startActivity(intent);
                        }
                    }
                    if (lastConnectedScannerTxt != null) {
                        lastConnectedScannerTxt.setText("Currently Connected Scanner");
                    }
                }
            }
        }
    };

    private void addToAvailableScannerList(AvailableScanner availableScanner) {
        if(!scannersList.contains(availableScanner)) {
            scannersList.add(availableScanner);
        }
    }

    private void addToLastConnectedScannerList(AvailableScanner availableScanner) {
        lastConnectedScannerList.clear();
        lastConnectedScannerList.add(availableScanner);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        if (nMgr != null) {
            nMgr.cancel(NotificationsReceiver.DEFAULT_NOTIFICATION_ID);
        }
        removeDevListDelegate(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevListDelegate(this);
/*
		* RHBJ36 03.03.2016
		* Device discovery is resource heavy operation.
		* Make sure to cancel the discovery when not needed.
		*/
        Application.sdkHandler.dcssdkStopScanningDevices();

        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        if (nMgr != null) {
            nMgr.cancel(NotificationsReceiver.DEFAULT_NOTIFICATION_ID);
        }
        addDevListDelegate(this);
    }

    public void OnConnHelp(View v) {

        Intent intent = new Intent(this, ConnectionHelpActivity2.class);
        startActivity(intent);

    }

    private void UpdateScannerListView(int what) {

        Message msg =  new Message();
        msg.what= what;
        UpdateScannersHandler.sendMessage(msg);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.scanners, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_add: {
                /*
                 * Show only paired bluetooth devices.
                 */
                updateScannersList();
				return true;
            }
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(Application.isAnyScannerConnected){
            Intent intent = new Intent(ScannersActivity.this, ActiveScannerActivity.class);
            intent.putExtra(Constants.SCANNER_NAME, curAvailableScanner.getScannerName());
            intent.putExtra(Constants.SCANNER_ADDRESS, curAvailableScanner.getScannerAddress());
            intent.putExtra(Constants.SCANNER_ID, curAvailableScanner.getScannerId());
            intent.putExtra(Constants.AUTO_RECONNECTION, curAvailableScanner.isAutoReconnection());
            intent.putExtra(Constants.CONNECTED, true);
            startActivity(intent);
        }else {
            if(launchFromFCS){
                Intent mainIntent = new Intent(this, HomeActivity.class);
                startActivity(mainIntent);
            }else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            // Get the device MAC address, which is the last 17 chars in the View
            AvailableScanner availableScanner = scannersList.get(position);

            ConnectToScanner(availableScanner);
        }
    };

    private AdapterView.OnItemClickListener mDeviceClickListenerLastConnected = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            // Get the device MAC address, which is the last 17 chars in the View
            AvailableScanner availableScanner = lastConnectedScannerList.get(position);

            ConnectToScanner(availableScanner);
        }
    };

    private void ConnectToScanner(AvailableScanner availableScanner) {
        for(DCSScannerInfo device:getActualScannersList()) {
            if(device.getScannerID() == availableScanner.getScannerId()){
                availableScanner.setIsAutoReconnection(device.isAutoCommunicationSessionReestablishment());
            }
        }
        if (availableScanner != null)
        {
            if (!availableScanner.isConnected()) {

                if ((curAvailableScanner!=null) &&(!availableScanner.getScannerAddress().equals(curAvailableScanner.getScannerAddress())))                 {
                    if (curAvailableScanner.isConnected())
                        disconnect(curAvailableScanner.getScannerId());
                }
                cmdExecTask=new MyAsyncTask(availableScanner);
                cmdExecTask.execute();

            } else {
                curAvailableScanner = availableScanner;
                if (curAvailableScanner.isConnected()) {

                    Application.currentScannerName = availableScanner.getScannerName();
                    Application.currentScannerAddress = availableScanner.getScannerAddress();
                    Application.currentAutoReconnectionState = availableScanner.isAutoReconnection();
                    Application.currentScannerId = availableScanner.getScannerId();
                    Intent intent = new Intent(ScannersActivity.this, ActiveScannerActivity.class);
                    intent.putExtra(Constants.SCANNER_NAME, availableScanner.getScannerName());
                    intent.putExtra(Constants.SCANNER_ADDRESS, availableScanner.getScannerAddress());
                    intent.putExtra(Constants.SCANNER_ID, availableScanner.getScannerId());
                    intent.putExtra(Constants.AUTO_RECONNECTION, availableScanner.isAutoReconnection());
                    intent.putExtra(Constants.CONNECTED, true);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean scannersListHasBeenUpdated() {

        UpdateScannerListView(EVENT);

        return true;
    }

    private class MyAsyncTask extends AsyncTask<Void,AvailableScanner,Boolean> {
        private AvailableScanner  scanner;
        public MyAsyncTask(AvailableScanner scn){
            this.scanner=scn;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ScannersActivity.this, "Connecting To scanner. Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {

                    if (cmdExecTask != null) {
                        cmdExecTask.cancel(true);
                    }
                }
            });

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result =connect(scanner.getScannerId());
            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS){
                curAvailableScanner = scanner;
                curAvailableScanner.setConnected(true);
                return true;
            }
            else {
                curAvailableScanner=null;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            Intent returnIntent = new Intent(getApplicationContext(),HomeActivity.class);
            if(!b){
                returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(returnIntent);
                setResult(RESULT_CANCELED, returnIntent);
                Toast.makeText(getApplicationContext(),"Unable to communicate with scanner",Toast.LENGTH_SHORT).show();
                scannersListHasBeenUpdated();
            }

        }
    }
}
