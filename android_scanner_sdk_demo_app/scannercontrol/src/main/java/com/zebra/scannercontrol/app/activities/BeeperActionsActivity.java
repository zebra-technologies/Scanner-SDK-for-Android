package com.zebra.scannercontrol.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;

import static com.zebra.scannercontrol.RMDAttributes.*;

public class BeeperActionsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate, SeekBar.OnSeekBarChangeListener {
    private NumberPicker beeperPicker;
    SeekBar seekBarVolume;
    private ArrayList<Integer> beeperActions;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private int scannerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beeper_actions);

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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Active Scanner");
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

        beeperActions = new ArrayList<>();
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_SHORT_BEEP_1);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_SHORT_BEEP_2);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_SHORT_BEEP_3);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_SHORT_BEEP_4);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_SHORT_BEEP_5);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_SHORT_BEEP_1);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_SHORT_BEEP_2);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_SHORT_BEEP_3);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_SHORT_BEEP_4);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_SHORT_BEEP_5);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LONG_BEEP_1);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LONG_BEEP_2);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LONG_BEEP_3);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LONG_BEEP_4);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LONG_BEEP_5);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_LONG_BEEP_1);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_LONG_BEEP_2);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_LONG_BEEP_3);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_LONG_BEEP_4);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_LONG_BEEP_5);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_FAST_WARBLE_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_SLOW_WARBLE_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LOW_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_HIGH_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_LOW_HIGH_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_LOW_HIGH_LOW_BEEP);
        beeperActions.add(RMD_ATTR_VALUE_ACTION_HIGH_HIGH_LOW_LOW_BEEP);

        seekBarVolume = (SeekBar) findViewById(R.id.seek_beeper_volume);
        if(seekBarVolume !=null) {
            seekBarVolume.setProgress(getBeeperVolume(scannerID));
            drawLines();
            seekBarVolume.setOnSeekBarChangeListener(this);
        }
    }
    private int getBeeperVolume(int scannerID) {
        int beeperVolume = 0;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>140</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);
        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(outXML.toString()));
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
                        if (name.equals("value")) {
                            beeperVolume = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if(beeperVolume == 0){
            return 100;
        }else if(beeperVolume == 1){
            return 50;
        }else{
            return 0;
        }
    }

    private void drawLines() {
        int indent = 20;

        seekBarVolume.setPadding(indent*2,0,indent*2,0);
        //Get the width of the main view.
        Display display = getWindowManager().getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        int width = displaysize.x;
        //set the seekbar maximum (Must be a even number, having a remainder will cause undersirable results)
        //this variable will also determine the number of points on the scale.
        int seekbarmax = 2;

        int seekbarpoints = (width / seekbarmax); //this will determine how many points on the scale there should be on the seekbar


        //Create a new bitmap that is the width of the screen
        Bitmap bitmap = Bitmap.createBitmap(width, 100, Bitmap.Config.ARGB_8888);
        //A new canvas to draw on.
        Canvas canvas = new Canvas(bitmap);


        //a new style of painting - colour and stoke thickness.
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(this, R.color.blue)); //Set the colour to red
        paint.setStyle(Paint.Style.STROKE); //set the style
        paint.setStrokeWidth(10); //Stoke width

        int point = 0; //initiate the point variable
        canvas.drawLine((indent*2), 100, (indent*2), 0, paint);
        point = point + seekbarpoints;
        canvas.drawLine(point, 100, point, 0, paint);
        point = point + seekbarpoints;
        canvas.drawLine(point-(indent*2), 100, point-(indent*2), 0, paint);


        //Create a new Drawable
        Drawable d = new BitmapDrawable(getResources(), bitmap);


        //Set the seekbar widgets background to the above drawable.
        seekBarVolume.setBackground(d);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawLines();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        return true;
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
        beeperPicker = (NumberPicker) findViewById(R.id.beeperPicker);
        beeperPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        beeperPicker.setDisplayedValues(getResources().getStringArray(R.array.beeper_actions));
        beeperPicker.setMaxValue(26);
        beeperPicker.setMinValue(0);
        beeperPicker.setValue(26);
    }

    public void beeperAction(View view) {
        int value = beeperPicker.getValue();
        Integer actionVal = (Integer) beeperActions.get(value);

        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID><cmdArgs><arg-int>" +
                + actionVal +"</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();

        new MyAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION,outXML).execute(new String[]{inXML});
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.CurScannerId = Application.SCANNER_ID_NONE;
            finish();
            intent = new Intent(BeeperActionsActivity.this, HomeActivity.class);
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
                    Application.CurScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(BeeperActionsActivity.this, FindCabledScanner.class);
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
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int mProgress = seekBar.getProgress();
        if(mProgress > 0 & mProgress < 26) {
            seekBar.setProgress(0);
        } else if(mProgress > 25 & mProgress < 76) {
            seekBar.setProgress(50);
        } else seekBar.setProgress(100);

        int i = seekBar.getProgress();
        int beeperVolume = 0;
        if(i == 50){
            beeperVolume = 1;
        }else if(i==0){
            beeperVolume = 2;
        }
        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>140</id><datatype>B</datatype><value>" + beeperVolume + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        new MyAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET,outXML).execute(new String[]{inXML});
    }

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        StringBuilder outXML;

        public MyAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML){
            this.scannerId=scannerId;
            this.opcode=opcode;
            this.outXML = outXML;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(BeeperActionsActivity.this, "Executing beeper action..");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return  executeCommand(opcode,strings[0],outXML,scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!b){
                Toast.makeText(BeeperActionsActivity.this, "Cannot perform beeper action", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
