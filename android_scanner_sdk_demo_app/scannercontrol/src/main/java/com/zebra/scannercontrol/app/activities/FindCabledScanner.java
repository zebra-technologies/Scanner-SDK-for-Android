package com.zebra.scannercontrol.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;

import java.util.ArrayList;

public class FindCabledScanner extends BaseActivity implements ScannerAppEngine.IScannerAppEngineDevListDelegate {

    private FrameLayout llBarcode;
    static MyAsyncTask cmdExecTask=null;
    private static CustomProgressDialog progressDialog;
    private static ArrayList<DCSScannerInfo> mSNAPIList=new ArrayList<DCSScannerInfo>();
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_cabled_scanner);

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

        mSNAPIList.clear();
        updateScannersList();
        for(DCSScannerInfo device:getActualScannersList()){
            if(device.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI){
                mSNAPIList.add(device);
            }
        }

        llBarcode = (FrameLayout) findViewById(R.id.snapi_barcode);
        if(mSNAPIList.size() == 0){
            // No SNAPI Scanners
            getSnapiBarcode();
        }else if(mSNAPIList.size() >1){
            // Multiple SNAPI scanners. Show the dialog and navigate to available scanner list.

            dialog = new Dialog(FindCabledScanner.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_multiple_snapi_scanners);
            TextView continueButton = (TextView) dialog.findViewById(R.id.btn_continue);
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                    dialog = null;
                    Intent intent = new Intent(getBaseContext(), ScannersActivity.class);
                    intent.putExtra(Constants.LAUNCH_FROM_FCS, true);
                    startActivity(intent);
                }
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        }else {
            // Only one SNAPI scanner available
            if(mSNAPIList.get(0).isActive()){
                // Available scanner is active. Navigate to active scanner
                finish();
            }else{
                // Try to connect available scanner
                cmdExecTask=new MyAsyncTask(mSNAPIList.get(0));
                cmdExecTask.execute();
            }
        }
    }

    private void getSnapiBarcode() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        BarCodeView barCodeView = Application.sdkHandler.dcssdkGetUSBSNAPIWithImagingBarcode();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int orientation =this.getResources().getConfiguration().orientation;
        int x = width * 9 / 10;
        int y = x / 3;
        if(getDeviceScreenSize()>6){ // TODO: Check 6 is ok or not
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                x =  width /2;
                y = x/3;
            }else {
                x =  width *2/3;
                y = x/3;
            }
        }
        barCodeView.setSize(x, y);
        llBarcode.addView(barCodeView, layoutParams);
    }

    private double getDeviceScreenSize() {
        double screenInches = 0;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int mWidthPixels;
        int mHeightPixels;

        try {
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            mWidthPixels = realSize.x;
            mHeightPixels = realSize.y;
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(mWidthPixels/dm.xdpi,2);
            double y = Math.pow(mHeightPixels/dm.ydpi,2);
            screenInches = Math.sqrt(x+y);
        } catch (Exception ignored) {
        }
        return screenInches;
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeDevListDelegate(this);
        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevListDelegate(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getSnapiBarcode();

    }


    private class MyAsyncTask extends AsyncTask<Void,DCSScannerInfo,Boolean> {
        private DCSScannerInfo  scanner;
        public MyAsyncTask(DCSScannerInfo scn){
            this.scanner=scn;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!isFinishing()) {
                progressDialog = new CustomProgressDialog(FindCabledScanner.this, "Connecting To scanner. Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result =connect(scanner.getScannerID());
            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS){
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(!isFinishing()) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
            Intent returnIntent = new Intent();
            if (!b) {
                setResult(RESULT_CANCELED, returnIntent);
                Toast.makeText(getApplicationContext(), "Unable to communicate with scanner", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
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
            Intent intent = new Intent(FindCabledScanner.this, ActiveScannerActivity.class);
            intent.putExtra(Constants.SCANNER_NAME, Application.currentConnectedScanner.getScannerName());
            intent.putExtra(Constants.SCANNER_ADDRESS, Application.currentConnectedScanner.getScannerHWSerialNumber());
            intent.putExtra(Constants.SCANNER_ID, Application.currentConnectedScanner.getScannerID());
            intent.putExtra(Constants.AUTO_RECONNECTION, Application.currentConnectedScanner.isAutoCommunicationSessionReestablishment());
            intent.putExtra(Constants.CONNECTED, true);
            startActivity(intent);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean scannersListHasBeenUpdated() {
        mSNAPIList.clear();
        updateScannersList();
        for (DCSScannerInfo device : getActualScannersList()) {
            if (device.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
                mSNAPIList.add(device);
            }
        }
        if (mSNAPIList.size() > 1) {
            // Multiple SNAPI scanners. Navigate to available scanner list
            Intent intent = new Intent(this, ScannersActivity.class);
            startActivity(intent);
        } else if (mSNAPIList.size() == 1) {
            // Only one SNAPI scanner available
            if (mSNAPIList.get(0).isActive()) {
                // Available scanner is active. Navigate to active scanner
                finish();
            } else {
                // Try to connect available scanner
                cmdExecTask = new MyAsyncTask(mSNAPIList.get(0));
                cmdExecTask.execute();
            }
        }
        return true;
    }
}
