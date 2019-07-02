package com.zebra.scannercontrol.app.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;

public class AvailableScannerActivity extends BaseActivity {

    private String address;
    int scannerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_scanner);

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

        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        address = getIntent().getStringExtra(Constants.SCANNER_ADDRESS);
        TextView scannerNameView = (TextView) findViewById(R.id.availableScannerName);
        scannerNameView.setText(scannerName);
        scannerId=getIntent().getIntExtra(Constants.SCANNER_ID,-1);
        if(scannerId!=-1){
            TextView availableScannerID=(TextView)findViewById(R.id.availableScannerID);
            availableScannerID.setText(scannerId+"");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
    }

    /**
     * Method to connect to a scanner
     * @param view - Button clicked
     */
    public void connectToScanner(View view){
       new MyAsyncTask(scannerId).execute();
    }
    private class MyAsyncTask extends AsyncTask<Void,Integer,Boolean> {
        private int scannerId;
        private CustomProgressDialog progressDialog;
        public MyAsyncTask(int scannerId){
            this.scannerId=scannerId;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(AvailableScannerActivity.this, "Connect To scanner...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
         DCSSDKDefs.DCSSDK_RESULT result =connect(scannerId);
            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS){
                return true;
            }
            else if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            Intent returnIntent = new Intent();
            if(b){
                setResult(RESULT_OK, returnIntent);
                returnIntent.putExtra(Constants.SCANNER_ID, scannerId);
            }
            else{
                setResult(RESULT_CANCELED, returnIntent);
            }
            AvailableScannerActivity.this.finish();
        }
    }
}
