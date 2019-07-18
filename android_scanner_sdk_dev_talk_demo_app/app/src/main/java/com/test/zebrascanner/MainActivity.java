package com.test.zebrascanner;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IDcsSdkApiDelegate  {
    SDKHandler sdkHandler;
    ArrayList<DCSScannerInfo> mScannerInfoList=new ArrayList<DCSScannerInfo>();
    static Dialog dialogPairNewScanner;
    BarCodeView barCodeView = null;
    FrameLayout llBarcode = null;
    static int connectedScannerID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//-------------------------------------------------------------------------------------------------------------------
        sdkHandler = new SDKHandler(this);
        sdkHandler.dcssdkSetDelegate(this);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        int notifications_mask = 0;
// We would like to subscribe to all scanner available/not-available events
        notifications_mask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
// We would like to subscribe to all scanner connection events
        notifications_mask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
// We would like to subscribe to all barcode events
        notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;
// subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList);
        sdkHandler.dcssdkGetActiveScannersList(mScannerInfoList);
//-------------------------------------------------------------------------------------------------------------------

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showPairNewScannerDialog();

            }
        });
        setUINoScannerConnected();
    }



    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType,int fromScannerID) {
//-------------------------------------------------------------------------------------------------------------------
        Barcode barcode = new Barcode(barcodeData,barcodeType,fromScannerID);
        dataHandler.obtainMessage(Constants.BARCODE_RECIEVED,barcode).sendToTarget();
//-------------------------------------------------------------------------------------------------------------------
    }

    private final Handler dataHandler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.BARCODE_RECIEVED:
                    TextView textViewScanData = (TextView) findViewById(R.id.txt_scan_data);
                    Barcode barcode = (Barcode) msg.obj;
                    textViewScanData.append(new String(barcode.getBarcodeData()));
                    textViewScanData.append("\n");
                    break;
            }
            scrollToBottom();
        }
    };

    public void disableScanner(View view) {
//-------------------------------------------------------------------------------------------------------------------
        String in_xml = "<inArgs><scannerID>" + connectedScannerID + "</scannerID></inArgs>";
        new MyAsyncTask(
                connectedScannerID,DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE).
                execute(new String[]{in_xml});

//-------------------------------------------------------------------------------------------------------------------
    }

    public void beepScanner(View view) {
//-------------------------------------------------------------------------------------------------------------------
// set beeper to perform a HIGH pitch SHORT duration tone
        int value = 2;
        String inXML = "<inArgs><scannerID>"+connectedScannerID+"</scannerID><cmdArgs><arg-int>" +
                Integer.toString(value) +"</arg-int></cmdArgs></inArgs>";
// Exceute in an AsyncTask to remove UI blocking
        new MyAsyncTask(connectedScannerID,DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION).execute(
                new String[]{inXML});

//-------------------------------------------------------------------------------------------------------------------
    }

    public void enableScanner(View view) {
//-------------------------------------------------------------------------------------------------------------------
        String in_xml = "<inArgs><scannerID>" + connectedScannerID + "</scannerID></inArgs>";
        new MyAsyncTask(
                connectedScannerID,DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE).
                execute(new String[]{in_xml});

//-------------------------------------------------------------------------------------------------------------------
    }

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], null, scannerId);
        }
        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sdkHandler !=null){
            sdkHandler.dcssdkClose(this);
        }
    }

    private void showPairNewScannerDialog() {
        if(!isFinishing()) {
            dialogPairNewScanner = new Dialog(MainActivity.this);
            dialogPairNewScanner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogPairNewScanner.setContentView(R.layout.dialog_new_scanner);
            TextView cancelButton = (TextView) dialogPairNewScanner.findViewById(R.id.btn_cancel);
            // if decline button is clicked, close the custom dialog
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialogPairNewScanner.dismiss();
                    dialogPairNewScanner = null;
                }
            });
            llBarcode = (FrameLayout) dialogPairNewScanner.findViewById(R.id.scan_to_connect_barcode);
            barCodeView = null;

            barCodeView = sdkHandler.dcssdkGetPairingBarcode(DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST,  DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT);


            if (barCodeView != null && llBarcode != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 8 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                llBarcode.addView(barCodeView, layoutParams);
            }
            dialogPairNewScanner.setCancelable(false);
            dialogPairNewScanner.setCanceledOnTouchOutside(false);
            dialogPairNewScanner.show();
            Window window = dialogPairNewScanner.getWindow();
            if (window != null) {
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, getY());
            }
        }else{
        }
    }

    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (220 * scale + 0.5f);
        return y;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {

    }

    @Override
    public void dcssdkEventScannerDisappeared(int i) {

    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {
        if(dialogPairNewScanner !=null){
            dialogPairNewScanner.dismiss();
        }
        setUIScannerConnected(dcsScannerInfo);
        connectedScannerID = dcsScannerInfo.getScannerID();
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int i) {
        setUINoScannerConnected();
        connectedScannerID = -1;
    }




    private void scrollToBottom()
    {
        final ScrollView mScrollView = (ScrollView) findViewById(R.id.scroll_view_barcodes);
        final TextView textViewScanData = (TextView) findViewById(R.id.txt_scan_data);

        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.smoothScrollTo(0, textViewScanData.getBottom());
            }
        });
    }


    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {

    }

    private void setUIScannerConnected(DCSScannerInfo dcsScannerInfo) {
        final DCSScannerInfo connectedScanner = dcsScannerInfo;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textViewNoScanners = (TextView)findViewById(R.id.txt_no_scanners);
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout_scanner_info);
                TextView textViewScanData = (TextView)findViewById(R.id.txt_scan_data_title);
                ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view_barcodes);
                linearLayout.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.VISIBLE);
                textViewScanData.setVisibility(View.VISIBLE);
                textViewNoScanners.setVisibility(View.INVISIBLE);
                textViewNoScanners.setVisibility(View.GONE);


                TextView textModel = (TextView)findViewById(R.id.txtModel);
                textModel.setText(connectedScanner.getScannerModel());

                TextView textSerial = (TextView)findViewById(R.id.txtSerial);
                textSerial.setText(connectedScanner.getScannerName());
            }
        });
    }
    private void setUINoScannerConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textViewNoScanners = (TextView)findViewById(R.id.txt_no_scanners);
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout_scanner_info);
                TextView textViewScanData = (TextView)findViewById(R.id.txt_scan_data_title);
                ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view_barcodes);
                linearLayout.setVisibility(View.INVISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
                textViewScanData.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                textViewScanData.setVisibility(View.GONE);

                textViewNoScanners.setVisibility(View.VISIBLE);
            }
        });
    }





    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        if (sdkHandler != null)
        {
            if(outXML == null){
                outXML = new StringBuilder();
            }
            DCSSDKDefs.DCSSDK_RESULT result=sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode,inXML,outXML,scannerID);
            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
                return true;
            else if(result==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
        }
        return false;
    }
}
