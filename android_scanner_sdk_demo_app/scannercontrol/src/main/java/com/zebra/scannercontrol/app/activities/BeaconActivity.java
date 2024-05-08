/**
 * ©2023 Zebra Technologies Corp. and/or its affiliates.  All rights reserved.
 */
package com.zebra.scannercontrol.app.activities;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import static com.zebra.scannercontrol.app.helpers.Constants.*;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.adapters.BeaconListAdapter;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.AvailableScanner;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.beacon.BeaconManager;
import com.zebra.scannercontrol.beacon.IDcsBeaconEvents;
import com.zebra.scannercontrol.beacon.entities.ZebraBeacon;
import com.zebra.scannercontrol.beacon.entities.ZebraBeaconFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * This class is responsible for displaying beacons
 */
public class BeaconActivity extends BaseActivity implements IDcsBeaconEvents {

    Dialog dialogFilter;
    public static boolean BEACON_SCAN_ENABLE = false; //use to start scan on activity onStart() and stop on activity onStop()
    Calendar newCalendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private boolean batterySpinnerInit, dateSpinnerInit = true;
    RecyclerView recyclerView;
    BeaconListAdapter adapter;
    public static ArrayList<ZebraBeacon> beacons;
    Spinner spinnerCradle,spinnerMotion, spinnerBatteryStatus, spinnerVirtualTether, spinnerConnected, spinnerBattery, spinnerDOM;
    EditText edtPercentage1, edtPercentage2, edtModelNumber, edtSerialNumber, edtRSSI, edtProdRelName, edtConfigFileName;
    TextView tvDate1, tvDate2;
    Button btnResetFilter, btnApplyFilter , beepButton, ledButton;
    static BeaconConnectAsyncTask cmdExecTask=null;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
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
        try {
            Application.sdkHandler.setiDcsBeaconEvents(this);
        }catch (Exception e){
            e.getMessage();
        }

        //update scannerList before compare the scanner ids onBeaconFound()/ onBeaconUpdate()
        updateScannersList();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(null);

        beacons = new ArrayList<>();

        beepButton = findViewById(R.id.btn_beep);
        beepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ZebraBeacon beacon : beacons) {
                    waitingForBeaconBeep = true;
                    int index = beacons.indexOf(beacon);

                    //check the last beacon on the list, for progress dismiss.
                    connectingToTheAvailableScanner(beacon, index == beacons.size() - 1);

                }
            }
        });

        ledButton = findViewById(R.id.btn_led);
        ledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ZebraBeacon beacon : beacons) {
                    waitingForBeaconLED = true;
                    int index = beacons.indexOf(beacon);

                    //check the last beacon on the list, for progress dismiss.
                    connectingToTheAvailableScanner(beacon, index == beacons.size() - 1);
                }
            }
        });

        adapter = new BeaconListAdapter (beacons, getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Generate instance of available scanner by getting beacon data and connecting to the scanner
     */
    private void connectingToTheAvailableScanner(ZebraBeacon beacon, boolean isLast){
        AvailableScanner availableScanner = new AvailableScanner(beacon.getScannerID(), beacon.getzModelNumber() + " " + beacon.getzSerialNumber(), beacon.getzSerialNumber(), Objects.equals(beacon.getzConnected(), "YES"), beacon.isAutoCommunicationSessionReestablishment(), beacon.getConnectionType());
        availableScanner.setIsConnectable(true);

        //Make the connection
        cmdExecTask = new BeaconConnectAsyncTask(availableScanner, isLast);
        cmdExecTask.execute();
    }

    private void showProgress(){
        progressDialog = new CustomProgressDialog(BeaconActivity.this, "Indicating Selected Beacon Devices....");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class BeaconConnectAsyncTask extends AsyncTask<Void,AvailableScanner,Boolean> {
        private AvailableScanner scanner;
        private boolean isLastBeacon;

        public BeaconConnectAsyncTask(AvailableScanner scn, boolean isLast ){
            this.scanner=scn;
            this.isLastBeacon = isLast;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog==null) {
                showProgress();
            } else if (!progressDialog.isShowing()) {
                showProgress();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
            try {
                result = connect(scanner.getScannerId());
            }catch (Exception e){
                e.getMessage();
            }
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Boolean isBeaconConnected) {
            super.onPostExecute(isBeaconConnected);

            if (isLastBeacon) {
                if (waitingForBeaconBeep) {
                    waitingForBeaconBeep = false;
                }
                else if (waitingForBeaconLED) {
                    waitingForBeaconLED = false;
                }

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            if(!isBeaconConnected){
                Toast.makeText(getApplicationContext(),"Unable to communicate with scanner : " + scanner.getScannerAddress(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beacon_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_filter:
                openBeaconFilterDialog();
                return true;
            case android.R.id.home:
                Application.barcodeData.clear();
                Application.currentScannerId = Application.SCANNER_ID_NONE;
                finish();
                Intent intent_home = new Intent(BeaconActivity.this, HomeActivity.class);
                startActivity(intent_home);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Pop up dialog to filter beacons
     **/
    private void openBeaconFilterDialog(){
        dialogFilter = new Dialog(BeaconActivity.this);
        dialogFilter.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFilter.setContentView(R.layout.dialog_beacon_filter);
        dialogFilter.setCancelable(true);
        Window window = dialogFilter.getWindow();
        assert window != null;
        window.setLayout(MATCH_PARENT, MATCH_PARENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );
        btnResetFilter = dialogFilter.findViewById(R.id.btnResetFilter);
        btnApplyFilter = dialogFilter.findViewById(R.id.btnApplyFilter);

        spinnerBattery = dialogFilter.findViewById(R.id.spBattery);
        spinnerDOM = dialogFilter.findViewById(R.id.spDOM);
        ArrayAdapter<String> adapterDate = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SPINNER_DATA_DATE);
        ArrayAdapter<String> adapterBattery = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SPINNER_DATA_BATTERY);

        adapterBattery.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBattery.setAdapter(adapterBattery);
        spinnerDOM.setAdapter(adapterDate);

        spinnerCradle = dialogFilter.findViewById(R.id.spCradle);
        spinnerMotion = dialogFilter.findViewById(R.id.spMotion);
        spinnerBatteryStatus = dialogFilter.findViewById(R.id.spBatteryStatus);
        spinnerVirtualTether = dialogFilter.findViewById(R.id.spVirtualTetherAlarm);
        spinnerConnected = dialogFilter.findViewById(R.id.spConnected);

        ArrayAdapter<String> adapterSpinnerYesNo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SPINNER_DATA_YES_NO);
        ArrayAdapter<String> adapterSpinnerOnOff = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SPINNER_DATA_ON_OFF);

        adapterSpinnerYesNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerOnOff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Yes No
        spinnerCradle.setAdapter(adapterSpinnerYesNo);
        spinnerMotion.setAdapter(adapterSpinnerYesNo);
        spinnerConnected.setAdapter(adapterSpinnerYesNo);


        // On Off
        spinnerBatteryStatus.setAdapter(adapterSpinnerOnOff);
        spinnerVirtualTether.setAdapter(adapterSpinnerOnOff);

        // battery %
        edtPercentage1 = dialogFilter.findViewById(R.id.edtPercentage1);
        edtPercentage2 = dialogFilter.findViewById(R.id.edtPercentage2);

        // date
        tvDate1 = dialogFilter.findViewById(R.id.tvDate1);
        tvDate2 = dialogFilter.findViewById(R.id.tvDate2);

        edtModelNumber = (EditText) dialogFilter.findViewById(R.id.edtModelNumber);
        edtSerialNumber = (EditText) dialogFilter.findViewById(R.id.edtSerialNumber);
        edtRSSI = (EditText) dialogFilter.findViewById(R.id.edtRSSI);
        edtProdRelName = (EditText) dialogFilter.findViewById(R.id.edtProdRelName);
        edtConfigFileName = (EditText) dialogFilter.findViewById(R.id.edtConfigFileName);

        batterySpinnerInit = true;
        dateSpinnerInit = true;

        spinnerBattery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //batery selectors visibility
                if(batterySpinnerInit){
                    batterySpinnerInit = false;
                } else {
                    edtPercentage1.setError(null);
                    edtPercentage2.setError(null);
                    edtPercentage1.setText(null);
                    edtPercentage2.setText(null);
                    switch (SPINNER_DATA_BATTERY.get(i)) {
                        case BCON_FILTER_SELECTOR_BETWEEN:
                            edtPercentage1.setEnabled(true);
                            edtPercentage1.setHint("0% >");
                            edtPercentage2.setEnabled(true);
                            edtPercentage2.setHint("< 100%");
                            break;
                        case BCON_FILTER_SELECTOR_ALL:
                            edtPercentage1.setEnabled(false);
                            edtPercentage1.setHint("0% >");
                            edtPercentage2.setEnabled(false);
                            edtPercentage2.setHint("< 100%");
                            break;
                        case BCON_FILTER_SELECTOR_LESS_THAN:
                            edtPercentage1.setEnabled(false);
                            edtPercentage1.setHint("NA");
                            edtPercentage2.setEnabled(true);
                            edtPercentage2.setHint("< 100%");
                            break;
                        case BCON_FILTER_SELECTOR_GRATER_THAN:
                            edtPercentage1.setEnabled(true);
                            edtPercentage1.setHint("> 0%");
                            edtPercentage2.setEnabled(false);
                            edtPercentage2.setHint("NA");
                            break;
                        case BCON_FILTER_SELECTOR_EQUALS:
                            edtPercentage1.setEnabled(true);
                            edtPercentage1.setHint("0-100%");
                            edtPercentage2.setEnabled(false);
                            edtPercentage2.setHint("NA");
                            break;
                        default:
                            edtPercentage1.setEnabled(false);
                            edtPercentage1.setHint("NA");
                            edtPercentage2.setEnabled(false);
                            edtPercentage2.setHint("NA");
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        spinnerDOM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(dateSpinnerInit){
                    dateSpinnerInit = false;
                }else {
                    tvDate1.setError(null);
                    tvDate2.setError(null);
                    tvDate1.setText(null);
                    tvDate2.setText(null);
                    switch (SPINNER_DATA_DATE.get(i)) {
                        case BCON_FILTER_SELECTOR_BETWEEN:
                            tvDate1.setClickable(true);
                            tvDate1.setHint("From");
                            tvDate2.setClickable(true);
                            tvDate2.setHint("To");
                            break;
                        case BCON_FILTER_SELECTOR_TO_DATE:
                            tvDate1.setClickable(false);
                            tvDate1.setHint("NA");
                            tvDate2.setClickable(true);
                            tvDate2.setHint("To");
                            break;
                        case BCON_FILTER_SELECTOR_EQUALS:
                            tvDate1.setClickable(true);
                            tvDate1.setHint("Date");
                            tvDate2.setClickable(false);
                            tvDate2.setHint("NA");
                            break;
                        case BCON_FILTER_SELECTOR_FROM_DATE:
                            tvDate2.setClickable(false);
                            tvDate2.setHint("NA");
                            tvDate1.setClickable(true);
                            tvDate1.setHint("From");
                            break;
                        default:
                            tvDate1.setClickable(false);
                            tvDate1.setHint("NA");
                            tvDate2.setClickable(false);
                            tvDate2.setHint("NA");
                            break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });


        final DatePickerDialog startDatePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            tvDate1.setText(sdf.format(newDate.getTime()));
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final DatePickerDialog  endDatePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            tvDate2.setText(sdf.format(newDate.getTime()));
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        tvDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDate1.setError(null);
                tvDate2.setError(null);
                startDatePickerDialog.show();
            }
        });
        tvDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDate1.setError(null);
                tvDate2.setError(null);
                endDatePickerDialog.show();
            }
        });

        //Set Filter Based on Shared Preferences Values
        setAddedFilters();

        btnResetFilter.setOnClickListener(v -> {
            resetFilters();
        });
        btnApplyFilter.setOnClickListener(v -> {
            if(validateUserInputs()) {
                applyFilters();
                batterySpinnerInit = true;
                dateSpinnerInit = true;
                dialogFilter.dismiss();
            }
        });
        dialogFilter.show();
    }

    /**
     * validate user inputs in beacon filters
     * special character validation, date validation, percentage validation
     * @return boolean
     */
    private boolean validateUserInputs(){
        if(edtModelNumber.getText() != null){
            if (!edtModelNumber.getText().toString().matches("[a-zA-Z0-9.? ]*")){
                edtModelNumber.setError("Invalid Model Number");
                return false;
            }
        }
        if(edtSerialNumber.getText() != null){
            if (!edtSerialNumber.getText().toString().matches("[a-zA-Z0-9.? ]*")){
                edtSerialNumber.setError("Invalid Serial Number");
                return false;
            }
        }
        if(edtRSSI.getText() != null){
            if (!edtRSSI.getText().toString().matches("[a-zA-Z0-9.? ]*")){
                edtRSSI.setError("Invalid RSSI");
                return false;
            }
        }
        if(edtConfigFileName.getText() != null){
            if (!edtConfigFileName.getText().toString().matches("[a-zA-Z0-9.? ]*")){
                edtConfigFileName.setError("Invalid Config file name");
                return false;
            }
        }
        if(edtProdRelName.getText() != null){
            if (!edtProdRelName.getText().toString().matches("[a-zA-Z0-9.? ]*")){
                edtProdRelName.setError("Invalid Product release Name");
                return false;
            }
        }
        if (spinnerBattery.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_BETWEEN)){
            if(edtPercentage1.getText()==null || edtPercentage1.getText().toString().isEmpty()){
                edtPercentage1.setError("Please enter Minimum value");
                return false;
            } else if(Integer.parseInt(edtPercentage1.getText().toString())>100){
                edtPercentage1.setError("Maximum % should be 100");
                return false;
            }
            if(edtPercentage2.getText()==null || edtPercentage2.getText().toString().isEmpty()){
                edtPercentage2.setError("Please enter Maximum value");
                return false;
            }else if(Integer.parseInt(edtPercentage2.getText().toString())>100){
                edtPercentage2.setError("Maximum % should be 100");
                return false;
            }
            if(Integer.parseInt(edtPercentage1.getText().toString())>= Integer.parseInt(edtPercentage2.getText().toString())){
                edtPercentage1.setError("Minimum % should be less than Maximum %");
                edtPercentage2.setError("Minimum % should be less than Maximum %");
                return false;
            }
        }else if(spinnerBattery.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_EQUALS)) {
            if(edtPercentage1.getText()==null || edtPercentage1.getText().toString().isEmpty()){
                edtPercentage1.setError("Please enter % value to compare");
                return false;
            } else if(Integer.parseInt(edtPercentage1.getText().toString())>100){
                edtPercentage1.setError("Maximum % value should be 100");
                return false;
            }
        } else if (spinnerBattery.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_GRATER_THAN)) {
            if(edtPercentage1.getText()==null || edtPercentage1.getText().toString().isEmpty()){
                edtPercentage1.setError("Please enter % value to compare");
                return false;
            } else if(Integer.parseInt(edtPercentage1.getText().toString())>100){
                edtPercentage1.setError("Maximum % value should be 100");
                return false;
            }
        }else if (spinnerBattery.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_LESS_THAN)) {
            if (edtPercentage2.getText() == null || edtPercentage2.getText().toString().isEmpty()) {
                edtPercentage2.setError("Please enter % value to compare");
                return false;
            } else if (Integer.parseInt(edtPercentage2.getText().toString()) > 100) {
                edtPercentage2.setError("Maximum % value should be 100");
                return false;
            }
        }

        if(spinnerDOM.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_BETWEEN)){
            if(tvDate1.getText()==null || tvDate1.getText()==""){
                tvDate1.setError("Please enter Earliest Date");
                return false;
            } else {
                try {
                    if(!Objects.requireNonNull(sdf.parse(tvDate1.getText().toString())).before(Calendar.getInstance().getTime())){
                        tvDate1.setError("Maximum date is today");
                        return false;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if(tvDate2.getText()==null || tvDate2.getText()==""){
                tvDate2.setError("Please enter Earliest Date");
                return false;
            } else {
                try {
                    if(!Objects.requireNonNull(sdf.parse(tvDate2.getText().toString())).before(Calendar.getInstance().getTime())){
                        tvDate2.setError("Maximum date is today");
                        return false;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                if(Objects.requireNonNull(sdf.parse(tvDate1.getText().toString())).after(sdf.parse(tvDate2.getText().toString()))){
                    tvDate2.setError("Maximum date should be after the first date");
                    return false;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if(spinnerDOM.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_EQUALS)) {
            if (tvDate1.getText() == null || tvDate1.getText()=="") {
                tvDate1.setError("Please enter a Date to compare");
                return false;
            }else {
                try {
                    if(!Objects.requireNonNull(sdf.parse(tvDate1.getText().toString())).before(Calendar.getInstance().getTime())){
                        tvDate1.setError("Maximum date is today");
                        return false;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if(spinnerDOM.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_FROM_DATE)) {
            if (tvDate1.getText() == null || tvDate1.getText()=="") {
                tvDate1.setError("Please enter a Date to compare");
                return false;
            } else {
                try {
                    if(!Objects.requireNonNull(sdf.parse(tvDate1.getText().toString())).before(Calendar.getInstance().getTime())){
                        tvDate1.setError("Maximum date is today");
                        return false;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if(spinnerDOM.getSelectedItem().toString().equalsIgnoreCase(BCON_FILTER_SELECTOR_TO_DATE)) {
            if (tvDate2.getText() == null || tvDate2.getText()=="") {
                tvDate2.setError("Please enter a Date to compare");
                return false;
            } else {
                try {
                    if(!Objects.requireNonNull(sdf.parse(tvDate2.getText().toString())).before(Calendar.getInstance().getTime())){
                        tvDate2.setError("Maximum date is today");
                        return false;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    @Override
    protected void onStart() {
        BEACON_SCAN_ENABLE = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        BEACON_SCAN_ENABLE = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        BEACON_SCAN_ENABLE = false;
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setAddedFilters(){
        SharedPreferences spFilters = getSharedPreferences(PREFS_NAME, 0);
        //Model Number
        edtModelNumber.setText(spFilters.getString(PREF_BCON_FILTER_MODEL_NUMBER,""));
        //Serial Number
        edtSerialNumber.setText(spFilters.getString(PREF_BCON_FILTER_SERIAL_NUMBER,""));
        //RSSI Power Reference
        edtRSSI.setText(spFilters.getString(PREF_BCON_FILTER_RSSI_P_REF,""));
        //Product Release Name
        edtProdRelName.setText(spFilters.getString(PREF_BCON_FILTER_PRODUCT_RELEASE_NAME,""));
        //Configuration File Name
        edtConfigFileName.setText(spFilters.getString(PREF_BCON_FILTER_CONFIG_FILE_NAME,""));

        //Battery Percentage From and To
        String jsonBattery = spFilters.getString(PREF_BCON_FILTER_BATTERY_PERCENTAGE,null);
        TypeToken<HashMap<String,Integer>> tokenBattery = new TypeToken<HashMap<String,Integer>>() {};
        HashMap<String,Integer> batteryPercentage =new Gson().fromJson(jsonBattery,tokenBattery.getType());
        if (batteryPercentage!=null) {
            if (batteryPercentage.containsKey(BeaconManager.BEACON_FILTER_EQUALS)) {
                spinnerBattery.setSelection(SPINNER_DATA_BATTERY.indexOf(BCON_FILTER_SELECTOR_EQUALS), true);
                edtPercentage1.setEnabled(true);
                edtPercentage2.setEnabled(false);
                edtPercentage1.setText(String.valueOf(batteryPercentage.get(BeaconManager.BEACON_FILTER_EQUALS)));
            } else if (batteryPercentage.containsKey(BeaconManager.BEACON_FILTER_FROM) && batteryPercentage.containsKey(BeaconManager.BEACON_FILTER_TO)) {
                spinnerBattery.setSelection(SPINNER_DATA_BATTERY.indexOf(BCON_FILTER_SELECTOR_BETWEEN), true);
                edtPercentage1.setEnabled(true);
                edtPercentage2.setEnabled(true);
                edtPercentage1.setText(String.valueOf(batteryPercentage.get(BeaconManager.BEACON_FILTER_FROM)));
                edtPercentage2.setText(String.valueOf(batteryPercentage.get(BeaconManager.BEACON_FILTER_TO)));
            } else if (batteryPercentage.containsKey(BeaconManager.BEACON_FILTER_TO)) {
                spinnerBattery.setSelection(SPINNER_DATA_BATTERY.indexOf(BCON_FILTER_SELECTOR_LESS_THAN), true);
                edtPercentage1.setEnabled(false);
                edtPercentage2.setEnabled(true);
                edtPercentage2.setText(String.valueOf(batteryPercentage.get(BeaconManager.BEACON_FILTER_TO)));
            } else if (batteryPercentage.containsKey(BeaconManager.BEACON_FILTER_FROM)) {
                spinnerBattery.setSelection(SPINNER_DATA_BATTERY.indexOf(BCON_FILTER_SELECTOR_GRATER_THAN), true);
                edtPercentage2.setEnabled(false);
                edtPercentage1.setEnabled(true);
                edtPercentage1.setText(String.valueOf(batteryPercentage.get(BeaconManager.BEACON_FILTER_FROM)));
            }
            else{
                edtPercentage1.setEnabled(false);
                edtPercentage2.setEnabled(false);
                edtPercentage1.setHint("0%>");
                edtPercentage2.setHint("<100%");
            }
        }else{
            edtPercentage1.setEnabled(false);
            edtPercentage2.setEnabled(false);
            edtPercentage1.setHint("0%>");
            edtPercentage2.setHint("<100%");
        }

        //Date of Manufacture
        String jsonDOM = spFilters.getString(PREF_BCON_FILTER_DOM,null);
        TypeToken<HashMap<String,String>> tokenDOM = new TypeToken<HashMap<String,String>>() {};
        HashMap<String,String> dateOfManufacture =new Gson().fromJson(jsonDOM,tokenDOM.getType());
        if(dateOfManufacture!=null){
            if (dateOfManufacture.containsKey(BeaconManager.BEACON_FILTER_EQUALS)) {
                spinnerDOM.setSelection(SPINNER_DATA_DATE.indexOf(BCON_FILTER_SELECTOR_EQUALS), true);
                tvDate1.setClickable(true);
                tvDate2.setClickable(false);
                tvDate1.setText(String.valueOf(dateOfManufacture.get(BeaconManager.BEACON_FILTER_EQUALS)));
            } else if (dateOfManufacture.containsKey(BeaconManager.BEACON_FILTER_FROM) && dateOfManufacture.containsKey(BeaconManager.BEACON_FILTER_TO)) {
                spinnerDOM.setSelection(SPINNER_DATA_DATE.indexOf(BCON_FILTER_SELECTOR_BETWEEN), true);
                tvDate1.setClickable(true);
                tvDate2.setClickable(true);
                tvDate1.setText(String.valueOf(dateOfManufacture.get(BeaconManager.BEACON_FILTER_FROM)));
                tvDate2.setText(String.valueOf(dateOfManufacture.get(BeaconManager.BEACON_FILTER_TO)));
            } else if (dateOfManufacture.containsKey(BeaconManager.BEACON_FILTER_TO)) {
                spinnerDOM.setSelection(SPINNER_DATA_DATE.indexOf(BCON_FILTER_SELECTOR_TO_DATE), true);
                tvDate1.setClickable(false);
                tvDate2.setClickable(true);
                tvDate2.setText(String.valueOf(dateOfManufacture.get(BeaconManager.BEACON_FILTER_TO)));
            } else if (dateOfManufacture.containsKey(BeaconManager.BEACON_FILTER_FROM)) {
                spinnerDOM.setSelection(SPINNER_DATA_DATE.indexOf(BCON_FILTER_SELECTOR_FROM_DATE),true);
                tvDate2.setClickable(false);
                tvDate1.setClickable(true);
                tvDate1.setText(String.valueOf(dateOfManufacture.get(BeaconManager.BEACON_FILTER_FROM)));
            } else{
                tvDate2.setClickable(false);
                tvDate1.setClickable(false);
                tvDate2.setText("From");
                tvDate1.setText("To");
            }
        } else{
            tvDate2.setClickable(false);
            tvDate1.setClickable(false);
            tvDate2.setText("From");
            tvDate1.setText("To");
        }

        //In Motion
        int indexOfMotion = SPINNER_DATA_YES_NO.indexOf(spFilters.getString(PREF_BCON_FILTER_IN_MOTION,""));
        spinnerMotion.setSelection(indexOfMotion,true);
        //In Cradle
        int indexOfCradle = SPINNER_DATA_YES_NO.indexOf(spFilters.getString(PREF_BCON_FILTER_IN_CRADLE,""));
        spinnerCradle.setSelection(indexOfCradle,true);
        //Battery Charge Status
        int indexOfBatteryChargeStatus = SPINNER_DATA_ON_OFF.indexOf(spFilters.getString(PREF_BCON_FILTER_BATTERY_CHARGE_STATUS,""));
        spinnerBatteryStatus.setSelection(indexOfBatteryChargeStatus, true);
        //Virtual Tether Alarm
        int indexOfVirtualTetherAlarm = SPINNER_DATA_ON_OFF.indexOf(spFilters.getString(PREF_BCON_FILTER_VIRTUAL_TETHER,""));
        spinnerVirtualTether.setSelection(indexOfVirtualTetherAlarm, true);
        //Connected
        int indexOfConnected = SPINNER_DATA_YES_NO.indexOf(spFilters.getString(PREF_BCON_FILTER_IS_CONNECTED,""));
        spinnerConnected.setSelection(indexOfConnected, true);

    }
    @SuppressLint("NotifyDataSetChanged")
    private void applyFilters(){
        SharedPreferences.Editor filtersEditor = getSharedPreferences(PREFS_NAME, 0).edit();
        //Model Number
        filtersEditor.putString(PREF_BCON_FILTER_MODEL_NUMBER,edtModelNumber.getText().toString());
        //Serial Number
        filtersEditor.putString(PREF_BCON_FILTER_SERIAL_NUMBER,edtSerialNumber.getText().toString());
        //RSSI Power Reference
        filtersEditor.putString(PREF_BCON_FILTER_RSSI_P_REF,edtRSSI.getText().toString());
        //Product Release Name
        filtersEditor.putString(PREF_BCON_FILTER_PRODUCT_RELEASE_NAME,edtProdRelName.getText().toString());
        //Configuration File Name
        filtersEditor.putString(PREF_BCON_FILTER_CONFIG_FILE_NAME,edtConfigFileName.getText().toString());

        //Battery Percentage From and To
        String batteryCondition = spinnerBattery.getSelectedItem().toString();
        HashMap<String, Integer> beaconBattery = new HashMap<>();
        switch (batteryCondition){
            case BCON_FILTER_SELECTOR_BETWEEN:
                beaconBattery.put(BeaconManager.BEACON_FILTER_FROM,Integer.parseInt(((EditText)dialogFilter.findViewById(R.id.edtPercentage1)).getText().toString()));
                beaconBattery.put(BeaconManager.BEACON_FILTER_TO,Integer.parseInt(edtPercentage2.getText().toString()));
                break;
            case BCON_FILTER_SELECTOR_GRATER_THAN:
                beaconBattery.put(BeaconManager.BEACON_FILTER_FROM,Integer.parseInt(((EditText)dialogFilter.findViewById(R.id.edtPercentage1)).getText().toString()));
                break;
            case BCON_FILTER_SELECTOR_LESS_THAN:
                beaconBattery.put(BeaconManager.BEACON_FILTER_TO,Integer.parseInt(edtPercentage2.getText().toString()));
                break;
            case BCON_FILTER_SELECTOR_EQUALS:
                beaconBattery.put(BeaconManager.BEACON_FILTER_EQUALS,Integer.parseInt(((EditText)dialogFilter.findViewById(R.id.edtPercentage1)).getText().toString()));
                break;
            default:break;
        }
        String jsonStringBattery = new Gson().toJson(beaconBattery);
        filtersEditor.putString(PREF_BCON_FILTER_BATTERY_PERCENTAGE,jsonStringBattery);

        //Date of Manufacture
        String dateOfManufactureCondition = spinnerDOM.getSelectedItem().toString();
        HashMap<String,String> beaconDOM = new HashMap<>();
        switch (dateOfManufactureCondition){
            case BCON_FILTER_SELECTOR_BETWEEN:
                beaconDOM.put(BeaconManager.BEACON_FILTER_FROM,tvDate1.getText().toString());
                beaconDOM.put(BeaconManager.BEACON_FILTER_TO,tvDate2.getText().toString());
                break;
            case BCON_FILTER_SELECTOR_FROM_DATE:
                beaconDOM.put(BeaconManager.BEACON_FILTER_FROM,tvDate1.getText().toString());
                break;
            case BCON_FILTER_SELECTOR_TO_DATE:
                beaconDOM.put(BeaconManager.BEACON_FILTER_TO,tvDate2.getText().toString());
                break;
            case BCON_FILTER_SELECTOR_EQUALS:
                beaconDOM.put(BeaconManager.BEACON_FILTER_EQUALS,tvDate1.getText().toString());
                break;
            default: break;
        }
        String jsonStringDOM = new Gson().toJson(beaconDOM);
        filtersEditor.putString(PREF_BCON_FILTER_DOM,jsonStringDOM);

        //In Motion
        filtersEditor.putString(PREF_BCON_FILTER_IN_MOTION,spinnerMotion.getSelectedItem().toString());
        //In Cradle
        filtersEditor.putString(PREF_BCON_FILTER_IN_CRADLE,spinnerCradle.getSelectedItem().toString());
        //Battery Charge Status
        filtersEditor.putString(PREF_BCON_FILTER_BATTERY_CHARGE_STATUS,spinnerBatteryStatus.getSelectedItem().toString());
        //Virtual Tether Alarm
        filtersEditor.putString(PREF_BCON_FILTER_VIRTUAL_TETHER,spinnerVirtualTether.getSelectedItem().toString());
        //Connected
        filtersEditor.putString(PREF_BCON_FILTER_IS_CONNECTED,spinnerConnected.getSelectedItem().toString());

        filtersEditor.commit();

        //clear the current beacon list on filtering to get the filtered beacons
        beacons.clear();
        adapter.notifyDataSetChanged();
    }

    private void resetFilters(){
        // UI reset
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogFilter!=null) {
                    edtModelNumber.getText().clear();
                    edtSerialNumber.getText().clear();
                    edtRSSI.getText().clear();
                    edtProdRelName.getText().clear();
                    edtConfigFileName.getText().clear();

                    edtModelNumber.setError(null);
                    edtSerialNumber.setError(null);
                    edtRSSI.setError(null);
                    edtProdRelName.setError(null);
                    edtConfigFileName.setError(null);
                    edtPercentage1.setError(null);
                    edtPercentage2.setError(null);
                    tvDate1.setError(null);
                    tvDate2.setError(null);

                    spinnerMotion.setSelection(SPINNER_DATA_YES_NO.indexOf(BCON_FILTER_SELECTOR_ALL),true);
                    spinnerCradle.setSelection(SPINNER_DATA_YES_NO.indexOf(BCON_FILTER_SELECTOR_ALL),true);
                    spinnerConnected.setSelection(SPINNER_DATA_YES_NO.indexOf(BCON_FILTER_SELECTOR_ALL),true);
                    spinnerVirtualTether.setSelection(SPINNER_DATA_ON_OFF.indexOf(BCON_FILTER_SELECTOR_ALL),true);
                    spinnerBatteryStatus.setSelection(SPINNER_DATA_ON_OFF.indexOf(BCON_FILTER_SELECTOR_ALL),true);

                    spinnerDOM.setSelection(SPINNER_DATA_DATE.indexOf(BCON_FILTER_SELECTOR_ALL),true);
                    tvDate2.setClickable(false);
                    tvDate1.setClickable(false);
                    tvDate2.setText("From");
                    tvDate1.setText("To");

                    spinnerBattery.setSelection(SPINNER_DATA_BATTERY.indexOf(BCON_FILTER_SELECTOR_ALL), true);
                    edtPercentage1.setEnabled(false);
                    edtPercentage2.setEnabled(false);
                    edtPercentage1.setHint("0%>");
                    edtPercentage2.setHint("<100%");
                }
            }
        });


        // shared pref reset
        SharedPreferences.Editor filtersEditor = getSharedPreferences(PREFS_NAME, 0).edit();
        filtersEditor.putString(PREF_BCON_FILTER_MODEL_NUMBER,"");
        filtersEditor.putString(PREF_BCON_FILTER_SERIAL_NUMBER,"");
        filtersEditor.putString(PREF_BCON_FILTER_RSSI_P_REF,"");
        filtersEditor.putString(PREF_BCON_FILTER_PRODUCT_RELEASE_NAME,"");
        filtersEditor.putString(PREF_BCON_FILTER_CONFIG_FILE_NAME,"");
        filtersEditor.putString(PREF_BCON_FILTER_BATTERY_PERCENTAGE,"");
        filtersEditor.putString(PREF_BCON_FILTER_DOM,"");
        filtersEditor.putString(PREF_BCON_FILTER_IN_MOTION,BeaconManager.BEACON_FILTER_ALL);
        filtersEditor.putString(PREF_BCON_FILTER_IN_CRADLE,BeaconManager.BEACON_FILTER_ALL);
        filtersEditor.putString(PREF_BCON_FILTER_BATTERY_CHARGE_STATUS,BeaconManager.BEACON_FILTER_ALL);
        filtersEditor.putString(PREF_BCON_FILTER_VIRTUAL_TETHER,BeaconManager.BEACON_FILTER_ALL);
        filtersEditor.putString(PREF_BCON_FILTER_IS_CONNECTED,BeaconManager.BEACON_FILTER_ALL);

        filtersEditor.commit();

    }

    @Override
    public ZebraBeaconFilter getBeaconFilters() {
        ZebraBeaconFilter zebraBeaconFilter = new ZebraBeaconFilter();
        SharedPreferences spFilters = getSharedPreferences(PREFS_NAME, 0);
        zebraBeaconFilter.setzModelNumber(spFilters.getString(PREF_BCON_FILTER_MODEL_NUMBER,null));
        zebraBeaconFilter.setzSerialNumber(spFilters.getString(PREF_BCON_FILTER_SERIAL_NUMBER,null));
        zebraBeaconFilter.setzRSSIPowerReference(spFilters.getString(PREF_BCON_FILTER_RSSI_P_REF,null));
        zebraBeaconFilter.setzConfigFileName(spFilters.getString(PREF_BCON_FILTER_CONFIG_FILE_NAME,null));
        zebraBeaconFilter.setzProductReleaseName(spFilters.getString(PREF_BCON_FILTER_PRODUCT_RELEASE_NAME,null));

        String jsonBattery = spFilters.getString(PREF_BCON_FILTER_BATTERY_PERCENTAGE,null);
        TypeToken<HashMap<String,Integer>> tokenBattery = new TypeToken<HashMap<String,Integer>>() {};
        HashMap<String,Integer> batteryPercentage =new Gson().fromJson(jsonBattery,tokenBattery.getType());
        zebraBeaconFilter.setzBatteryPercentage(batteryPercentage);

        String jsonDOM = spFilters.getString(PREF_BCON_FILTER_DOM,null);
        TypeToken<HashMap<String,String>> tokenDOM = new TypeToken<HashMap<String,String>>() {};
        HashMap<String,String> dateOfManufacture =new Gson().fromJson(jsonDOM,tokenDOM.getType());
        zebraBeaconFilter.setzDateOfManufacture(dateOfManufacture);

        zebraBeaconFilter.setzInMotion(spFilters.getString(PREF_BCON_FILTER_IN_MOTION,BeaconManager.BEACON_FILTER_ALL));
        zebraBeaconFilter.setzInCradle(spFilters.getString(PREF_BCON_FILTER_IN_CRADLE,BeaconManager.BEACON_FILTER_ALL));
        zebraBeaconFilter.setzBatteryChargeStatus(spFilters.getString(PREF_BCON_FILTER_BATTERY_CHARGE_STATUS,BeaconManager.BEACON_FILTER_ALL));
        zebraBeaconFilter.setzVirtualTetherAlarm(spFilters.getString(PREF_BCON_FILTER_VIRTUAL_TETHER,BeaconManager.BEACON_FILTER_ALL));
        zebraBeaconFilter.setzConnected(spFilters.getString(PREF_BCON_FILTER_IS_CONNECTED,BeaconManager.BEACON_FILTER_ALL));

        return zebraBeaconFilter;
    }


    @Override
    public void onFoundBeacon(ArrayList<ZebraBeacon> beaconsList, ZebraBeacon beacon) {
        //This method will be called when a beacon is found
        if(!beacons.contains(beacon)) {
            beacons.add(beacon);
            int index = beacons.indexOf(beacon);

            //compare the beacon hardware serial numbers with available scanner list scanners' serial numbers
            for(DCSScannerInfo scanner: getActualScannersList()){
                if(scanner.getScannerName().equalsIgnoreCase(beacon.getzModelNumber() +" "+ beacon.getzSerialNumber())){
                    beacon.setScannerID(scanner.getScannerID());
                    beacon.setAutoCommunicationSessionReestablishment(scanner.isAutoCommunicationSessionReestablishment());
                    beacon.setConnectionType(scanner.getConnectionType());
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemInserted(index);
                }
            });
        }
    }

    @Override
    public boolean getBeaconScanEnable() {
        // should be check on app settinges whether the beacon enable or not
        return BEACON_SCAN_ENABLE;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBeaconUpdated(ArrayList<ZebraBeacon> beaconsList, ZebraBeacon beacon) {
        //This method will be called when a beacon is updated
        int index = beacons.indexOf(beacon);
        //compare the beacon hardware serial numbers with available scanner list scanners' serial numbers
        for(DCSScannerInfo scanner: getActualScannersList()){
            if(scanner.getScannerName().equalsIgnoreCase(beacon.getzModelNumber() +" "+ beacon.getzSerialNumber())){
                beacon.setScannerID(scanner.getScannerID());
                beacon.setAutoCommunicationSessionReestablishment(scanner.isAutoCommunicationSessionReestablishment());
                beacon.setConnectionType(scanner.getConnectionType());
            }
        }

        //if some beacons can be remove from the beacon list on SCA.
        //But those can be still beaconing. here we adding those beacons to the list again
        if(index<0){
            beacons.add(beacon);
        }else {
            beacons.set(index, beacon);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(index);
            }
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBeaconRemoved(ArrayList<ZebraBeacon> beaconsList, ZebraBeacon beacon) {
        //This method will be called when a beacon is removed
        int index = beacons.indexOf(beacon);
        beacons.remove(beacon);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemRemoved(index);
            }
        });
    }
}
