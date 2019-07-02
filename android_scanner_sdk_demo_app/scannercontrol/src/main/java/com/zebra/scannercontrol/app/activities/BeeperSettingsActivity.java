package com.zebra.scannercontrol.app.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

import static com.zebra.scannercontrol.RMDAttributes.*;

public class BeeperSettingsActivity extends BaseActivity implements View.OnClickListener{
    private static final String MSG_FETCH = "Fetching settings";
    private boolean settingsFetched;
	private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beeper_settings);

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

        findViewById(R.id.lowVolume).setOnClickListener(this);
        findViewById(R.id.mediumVolume).setOnClickListener(this);
        findViewById(R.id.highVolume).setOnClickListener(this);
        findViewById(R.id.lowFrequency).setOnClickListener(this);
        findViewById(R.id.mediumFrequency).setOnClickListener(this);
        findViewById(R.id.highFrequency).setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBeeperSettings();
    }

    private void fetchBeeperSettings() {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);

        if (scannerID != -1) {

            String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>" + RMD_ATTR_BEEPER_VOLUME
                    + "," + RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY + "</attrib_list></arg-xml></cmdArgs></inArgs>";

            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{inXML});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    private void performBeeperSettings(String inXML) {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        if (scannerID != -1) {
            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET).execute(new String[]{inXML});
        }
    }

    private void prepareInXML(int attribID, int attribValue){
        if(settingsFetched) {
            String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, -1) + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" +
                    attribID + "</id><datatype>B</datatype><value>" + attribValue + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
            performBeeperSettings(inXML);
        }else{
            Toast.makeText(this, "Beeper settings have not been retrieved. Action will not be performed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(((CheckedTextView)view).isChecked()){
            Toast.makeText(this, "Already Active!!", Toast.LENGTH_SHORT).show();
        }else{
            int attribId = -1;
            int attribValue = -1;

            if(view == findViewById(R.id.lowVolume)){
                ((CheckedTextView)findViewById(R.id.mediumVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.highVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.lowVolume)).setChecked(true);
                attribId = RMD_ATTR_BEEPER_VOLUME;
                attribValue = RMD_ATTR_VALUE_BEEPER_VOLUME_LOW;
            }else if(view == findViewById(R.id.mediumVolume)){
                ((CheckedTextView)findViewById(R.id.lowVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.highVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.mediumVolume)).setChecked(true);
                attribId = RMD_ATTR_BEEPER_VOLUME;
                attribValue = RMD_ATTR_VALUE_BEEPER_VOLUME_MEDIUM;
            }else if(view == findViewById(R.id.highVolume)){
                ((CheckedTextView)findViewById(R.id.mediumVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.lowVolume)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.highVolume)).setChecked(true);
                attribId = RMD_ATTR_BEEPER_VOLUME;
                attribValue = RMD_ATTR_VALUE_BEEPER_VOLUME_HIGH;
            }else if(view == findViewById(R.id.lowFrequency)){
                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(true);
                attribId = RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY;
                attribValue = RMD_ATTR_VALUE_BEEPER_FREQ_LOW;
            }else if(view == findViewById(R.id.mediumFrequency)){
                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(true);
                attribId = RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY;
                attribValue = RMD_ATTR_VALUE_BEEPER_FREQ_MEDIUM;
            }else if(view == findViewById(R.id.highFrequency)){
                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(false);
                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(true);
                attribId = RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY;
                attribValue = RMD_ATTR_VALUE_BEEPER_FREQ_HIGH;
            }

            if(attribId != -1)
                prepareInXML(attribId, attribValue);
        }
    }

	
	/*RHBJ36:Issue reported from IOS.SSDK-3471.
	 *Change for fix for crashing when scanning barcode while fetching Beeper settings.
	 *Dialog is dismisses after Activity finished. Added check to confirm if the activity is not in Finishing state
	 *before cancelling the dialog.
	*/
	private void dismissDailog(){
        if (progressDialog != null && progressDialog.isShowing()&&!this.isFinishing())
            progressDialog.cancel();
    }

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean>{
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        
        public MyAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(BeeperSettingsActivity.this, "Execute Command...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder() ;
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                 if (result) {
                    try {
                        Log.i(TAG, sb.toString());
                        int i = 0;
                        int attrId = -1;
                        int attrVal;
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
                                    if (name.equals("id")) {
                                        attrId = Integer.parseInt(text != null ? text.trim() : null);
                                        Log.i(TAG,"ID tag found: ID: "+attrId);
                                    } else if (name.equals("value")) {
                                        settingsFetched = true;
                                        attrVal = Integer.parseInt(text != null ? text.trim() : null);
                                        Log.i(TAG,"Value tag found: Value: "+attrVal);
                                        if (RMD_ATTR_BEEPER_VOLUME == attrId) {
                                            if (RMD_ATTR_VALUE_BEEPER_VOLUME_LOW == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_VOLUME = RMD_ATTR_VALUE_BEEPER_VOLUME_LOW");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((CheckedTextView) findViewById(R.id.lowVolume)).setChecked(true);
                                                        ((CheckedTextView) findViewById(R.id.mediumVolume)).setChecked(false);
                                                        ((CheckedTextView) findViewById(R.id.highVolume)).setChecked(false);
                                                    }
                                                });
                                                }else if (RMD_ATTR_VALUE_BEEPER_VOLUME_MEDIUM == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_VOLUME = RMD_ATTR_VALUE_BEEPER_VOLUME_MEDIUM");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((CheckedTextView) findViewById(R.id.mediumVolume)).setChecked(true);
                                                        ((CheckedTextView) findViewById(R.id.lowVolume)).setChecked(false);
                                                        ((CheckedTextView) findViewById(R.id.highVolume)).setChecked(false);
                                                    }
                                                });
                                                } else if (RMD_ATTR_VALUE_BEEPER_VOLUME_HIGH == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_VOLUME = RMD_ATTR_VALUE_BEEPER_VOLUME_HIGH");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                ((CheckedTextView) findViewById(R.id.highVolume)).setChecked(true);
                                                ((CheckedTextView) findViewById(R.id.lowVolume)).setChecked(false);
                                                ((CheckedTextView) findViewById(R.id.mediumVolume)).setChecked(false);
                                                    }
                                                });
                                            }
                                        } else if (RMD_ATTR_BEEPER_FREQUENCY == attrId) {
                                            if (RMD_ATTR_VALUE_BEEPER_FREQ_LOW == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_FREQUENCY = RMD_ATTR_VALUE_BEEPER_FREQ_LOW");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(true);
                                                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(false);
                                                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(false);
                                                    }
                                                });
                                            } else if (RMD_ATTR_VALUE_BEEPER_FREQ_MEDIUM == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_FREQUENCY = RMD_ATTR_VALUE_BEEPER_FREQ_MEDIUM");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(true);
                                                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(false);
                                                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(false);
                                                    }
                                                });
                                            } else if (RMD_ATTR_VALUE_BEEPER_FREQ_HIGH == attrVal) {
                                                Log.i(TAG,"RMD_ATTR_BEEPER_FREQUENCY = RMD_ATTR_VALUE_BEEPER_FREQ_HIGH");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                ((CheckedTextView)findViewById(R.id.highFrequency)).setChecked(true);
                                                ((CheckedTextView)findViewById(R.id.mediumFrequency)).setChecked(false);
                                                ((CheckedTextView)findViewById(R.id.lowFrequency)).setChecked(false);
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
            dismissDailog();
            if(!b){
                if(opcode==DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET)
                  Toast.makeText(BeeperSettingsActivity.this, "Unable to fetch beeper settings", Toast.LENGTH_SHORT).show();
                else if(opcode==DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET){
                    Toast.makeText(BeeperSettingsActivity.this, "Cannot perform the action", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
