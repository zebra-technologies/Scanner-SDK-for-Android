package com.zebra.scannercontrol.app.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends BaseActivity implements View.OnClickListener, OnCheckedChangeListener {

    Button btnResetDefaults = null;
    //Spinner barcodeType;
    Spinner comProtocol;
    List<String> spinnerArrayHost;
    private  List<String> protocolList;
    ArrayAdapter<String> adapterHost;
    private  ArrayAdapter<String>protocolAdapter;
    private static int BARCODE_TYPE_SCANTOCONNECT = 0; // 0 : ScanToConnect Suite
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        btnResetDefaults = (Button) findViewById(R.id.btnResetAppDefaults);

        SwitchCompat optAvailableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        if (optAvailableScanner != null) {
            optAvailableScanner.setOnCheckedChangeListener(this);
        }

        SwitchCompat optActiveScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        if (optActiveScanner != null) {
            optActiveScanner.setOnCheckedChangeListener(this);
        }

        SwitchCompat optBarcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        if (optBarcodeEvent != null) {
            optBarcodeEvent.setOnCheckedChangeListener(this);
        }


        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        if (autoDetection != null) {
            autoDetection.setOnCheckedChangeListener(this);
        }

        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        if (scannerDiscovery != null) {
            scannerDiscovery.setOnCheckedChangeListener(this);
        }

        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        if (setDefaults != null) {
            setDefaults.setOnCheckedChangeListener(this);
        }


        comProtocol = (Spinner) findViewById(R.id.spinner_com_protocol);
        protocolList = new ArrayList<String>();

        // Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        // save barcode type value to 0(ScanToConnect Suite)
        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        settingsEditor.putInt(Constants.PREF_PAIRING_BARCODE_TYPE, BARCODE_TYPE_SCANTOCONNECT).commit();// Commit is required here. So suppressing warning.

        protocolAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, protocolList);
        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setDisplayListProtocolType(0);
        if (SettingsChangedFromDefaults()) {
            btnResetDefaults.setEnabled(true);
        } else {
            btnResetDefaults.setEnabled(false);
        }

        /* Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        barcodeType = (Spinner)findViewById(R.id.spinner_type);
        spinnerArrayHost =  new ArrayList<String>();

        spinnerArrayHost.add("ScanToConnect Suite");
        spinnerArrayHost.add("Legacy Pairing");

        adapterHost = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArrayHost);
        adapterHost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        barcodeType.setAdapter(adapterHost);
        barcodeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedTypeIndex = 0;
                Spinner spinnerType = ((Spinner) findViewById(R.id.spinner_type));
                if (spinnerType != null) {
                    selectedTypeIndex = spinnerType.getSelectedItemPosition();
                }
                SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                settingsEditor.putInt(Constants.PREF_PAIRING_BARCODE_TYPE,selectedTypeIndex).commit();
                SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
                TextView setDefaultText = (TextView)findViewById(R.id.set_factory_defaults_txt);
                Spinner spinnerComProtocol = (Spinner)findViewById(R.id.spinner_com_protocol);
                TextView textViewComProtocol = (TextView)findViewById(R.id.spinner_com_protocol_txt);
                if(selectedTypeIndex == 1){
                    if (setDefaults != null) {
                        setDefaults.setChecked(false);
                        setDefaults.setEnabled(false);
                    }
                    if (setDefaultText != null) {
                        setDefaultText.setEnabled(false);
                    }
                    if (spinnerComProtocol != null) {
                        spinnerComProtocol.setEnabled(false);
                    }
                    if (textViewComProtocol != null) {
                        textViewComProtocol.setEnabled(false);
                    }
                }else{
                    if (setDefaults != null) {
                        setDefaults.setEnabled(true);
                    }
                    if (setDefaultText != null) {
                        setDefaultText.setEnabled(true);
                    }
                    if (spinnerComProtocol != null) {
                        spinnerComProtocol.setEnabled(true);
                    }
                    if (textViewComProtocol != null) {
                        textViewComProtocol.setEnabled(true);
                    }
                }
                setDisplayListProtocolType(selectedTypeIndex);
                if (SettingsChangedFromDefaults()) {
                    btnResetDefaults.setEnabled(true);
                } else {
                    btnResetDefaults.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */



        comProtocol.setAdapter(protocolAdapter);
        AdapterView.OnItemSelectedListener listner= new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedProtocolIndex = 0;
                Spinner spinnerComProtocol = (Spinner) findViewById(R.id.spinner_com_protocol);
                if (spinnerComProtocol != null) {
                    selectedProtocolIndex = spinnerComProtocol.getSelectedItemPosition();
                }
                SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                settingsEditor.putInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE,selectedProtocolIndex).commit();// Commit is required here. So suppressing warning.

                setDisplayListProtocolType(selectedProtocolIndex);
                if (SettingsChangedFromDefaults()) {
                    btnResetDefaults.setEnabled(true);
                } else {
                    btnResetDefaults.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        comProtocol.setOnItemSelectedListener(listner);

        if (SettingsChangedFromDefaults()) {
            btnResetDefaults.setEnabled(true);
        } else {
            btnResetDefaults.setEnabled(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

        switch (buttonView.getId()) {
            case R.id.autoDetection: {
                TextView textView = (TextView) findViewById(R.id.txt_auto_detection);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_SCANNER_DETECTION, ((SwitchCompat) findViewById(R.id.autoDetection)).isChecked()).apply();
                break;
            }
            case R.id.bluetoothScannerDetection: {
                TextView textView = (TextView) findViewById(R.id.txt_discover_bluetooth_scanners);
                boolean enable = false;
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                        enable = true;
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                        enable = false;
                    }
                }
                enableBluetoothScannerDiscovery(enable);
                settingsEditor.putBoolean(Constants.PREF_SCANNER_DISCOVERY, ((SwitchCompat) findViewById(R.id.bluetoothScannerDetection)).isChecked()).apply();
                break;
            }
            case R.id.availableScanner:
            {
                TextView textView = (TextView) findViewById(R.id.txt_available_scanner);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_NOTIFY_AVAILABLE, ((SwitchCompat) findViewById(R.id.availableScanner)).isChecked() ).commit();
                break;
            }
            case R.id.activeScanner:
            {
                TextView textView = (TextView) findViewById(R.id.txt_active_scanner);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_NOTIFY_ACTIVE, ((SwitchCompat) findViewById(R.id.activeScanner)).isChecked()).commit();
                break;
            }
            case R.id.barcodeEvent:
            {
                TextView textView = (TextView) findViewById(R.id.txt_barcode_event);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_NOTIFY_BARCODE,((SwitchCompat) findViewById(R.id.barcodeEvent)).isChecked() ).commit();
                break;
            }
            case R.id.set_factory_defaults:
            {
                TextView textView = (TextView) findViewById(R.id.set_factory_defaults_txt);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_PAIRING_BARCODE_CONFIG, ((SwitchCompat) findViewById(R.id.set_factory_defaults)).isChecked()).commit();
                break;
            }

        }

        if (SettingsChangedFromDefaults()) {
            btnResetDefaults.setEnabled(true);
        } else {
            btnResetDefaults.setEnabled(false);
        }
        initializeDcsSdkWithAppSettings();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnResetAppDefaults: {
                ResetDefaultSettings();
                break;
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
    protected void onResume() {
        super.onResume();
        loadSavedSettings();
    }

    private void ResetDefaultSettings() {
        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        if (autoDetection != null) {
            autoDetection.setChecked(true);
        }

        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        if (scannerDiscovery != null) {
            scannerDiscovery.setChecked(true);
        }

        SwitchCompat availableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        if (availableScanner != null) {
            availableScanner.setChecked(false);
        }

        SwitchCompat activeScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        if (activeScanner != null) {
            activeScanner.setChecked(false);
        }

        SwitchCompat barcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        if (barcodeEvent != null) {
            barcodeEvent.setChecked(false);
        }

        /* Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        Spinner spinnerType = (Spinner)findViewById(R.id.spinner_type);
        if (spinnerType != null) {
            spinnerType.setSelection(0);
        }
        */

        Spinner spinnerComProtocol =(Spinner)findViewById(R.id.spinner_com_protocol);

        if (spinnerComProtocol != null) {
            spinnerComProtocol.setSelection(0);
        }

        SwitchCompat setFactoryDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        if (setFactoryDefaults != null) {
            setFactoryDefaults.setChecked(true);
        }

        btnResetDefaults.setEnabled(false);

    }

    private boolean SettingsChangedFromDefaults() {
        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        SwitchCompat availableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        SwitchCompat activeScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        SwitchCompat barcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        /* Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        Spinner barcodeType = (Spinner)findViewById(R.id.spinner_type);
        */
        Spinner comPrrotocol = (Spinner) findViewById(R.id.spinner_com_protocol);

        if (autoDetection != null && availableScanner!=null && activeScanner!=null && barcodeEvent!=null && setDefaults!=null /* SSDK_6172: && barcodeType!=null*/) {
            if ((autoDetection.isChecked())
                    && (!availableScanner.isChecked())
                    && (!activeScanner.isChecked())
                    && (!barcodeEvent.isChecked())
                    && (setDefaults.isChecked())
                    /* Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
                    && (barcodeType.getSelectedItemPosition() ==0)
                    */
                    && (comPrrotocol.getSelectedItemPosition() == 0)) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }
    }

    //Load the settings from application
    private void loadSavedSettings() {

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);

        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        if (autoDetection != null) {
            autoDetection.setChecked(settings.getBoolean(Constants.PREF_SCANNER_DETECTION, true));
            TextView textViewAutoDetection = (TextView) findViewById(R.id.txt_auto_detection);
            if(textViewAutoDetection !=null) {
                if (autoDetection.isChecked()) {
                    textViewAutoDetection.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewAutoDetection.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }

        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        if (scannerDiscovery != null) {
            scannerDiscovery.setChecked(settings.getBoolean(Constants.PREF_SCANNER_DISCOVERY, true));
            TextView textViewAutoDetection = (TextView) findViewById(R.id.txt_discover_bluetooth_scanners);
            if(textViewAutoDetection !=null) {
                if (scannerDiscovery.isChecked()) {
                    textViewAutoDetection.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewAutoDetection.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }

        SwitchCompat availableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        if (availableScanner != null) {
            availableScanner.setChecked(settings.getBoolean(Constants.PREF_NOTIFY_AVAILABLE, false));
            TextView textViewAvailableScanner = (TextView) findViewById(R.id.txt_available_scanner);
            if(textViewAvailableScanner!=null) {
                if (availableScanner.isChecked()) {
                    textViewAvailableScanner.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewAvailableScanner.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }

        SwitchCompat activeScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        if (activeScanner != null) {
            activeScanner.setChecked(settings.getBoolean(Constants.PREF_NOTIFY_ACTIVE, false));
            TextView textViewActiveScanner = (TextView) findViewById(R.id.txt_active_scanner);
            if(textViewActiveScanner !=null) {
                if (activeScanner.isChecked()) {
                    textViewActiveScanner.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewActiveScanner.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }



        SwitchCompat barcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        if (barcodeEvent != null) {
            barcodeEvent.setChecked(settings.getBoolean(Constants.PREF_NOTIFY_BARCODE, false));
            TextView textViewBarcodeEvent = (TextView) findViewById(R.id.txt_barcode_event);
            if(textViewBarcodeEvent!=null) {
                if (barcodeEvent.isChecked()) {
                    textViewBarcodeEvent.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewBarcodeEvent.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }



        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        if (setDefaults != null) {
            setDefaults.setChecked(settings.getBoolean(Constants.PREF_PAIRING_BARCODE_CONFIG, true));

            TextView textViewSetDefaults = (TextView) findViewById(R.id.set_factory_defaults_txt);
            if(textViewSetDefaults!=null) {
                if (setDefaults.isChecked()) {
                    textViewSetDefaults.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewSetDefaults.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                }
            }
        }

        /* Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        int barcode = settings.getInt(Constants.PREF_PAIRING_BARCODE_TYPE, 0);
        barcodeType = (Spinner)findViewById(R.id.spinner_type);
        if (barcodeType != null) {
            barcodeType.setSelection(barcode);
        }
        */
        int barcode = BARCODE_TYPE_SCANTOCONNECT;
        setDisplayListProtocolType(barcode);
        int protocol = settings.getInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE, 0);
        comProtocol = (Spinner)findViewById(R.id.spinner_com_protocol);
        if (comProtocol != null) {
            comProtocol.setSelection(protocol);
        }

        if (SettingsChangedFromDefaults()) {
            btnResetDefaults.setEnabled(true);
        } else {
            btnResetDefaults.setEnabled(false);
        }

        String bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
        TextView bluetoothAddress = (TextView)findViewById(R.id.txt_bt_address);
        TableRow tblRowBTAddress = (TableRow)findViewById(R.id.tbl_row_bt_address);
        if(!Objects.equals(bluetoothMAC, "")){
            bluetoothAddress.setText("Bluetooth Address ( "+bluetoothMAC +" ) ");
            tblRowBTAddress.setVisibility(View.VISIBLE);
        }else {
            tblRowBTAddress.setVisibility(View.GONE);
            bluetoothAddress.setText("Bluetooth Address");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save preferences
        SaveSettings();
        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }

    private void SaveSettings() {

        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        SwitchCompat availableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        SwitchCompat activeScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        SwitchCompat barcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        //Spinner barcodeType = (Spinner)findViewById(R.id.spinner_type);
        Spinner comProtocol = (Spinner)findViewById(R.id.spinner_com_protocol);

        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        if (autoDetection != null) {
            settingsEditor.putBoolean(Constants.PREF_SCANNER_DETECTION, autoDetection.isChecked()).apply();
        }

        if (scannerDiscovery != null) {
            settingsEditor.putBoolean(Constants.PREF_SCANNER_DISCOVERY, scannerDiscovery.isChecked()).apply();
        }

        if (availableScanner != null) {
            settingsEditor.putBoolean(Constants.PREF_NOTIFY_AVAILABLE, availableScanner.isChecked() ).commit();
        }
        if (activeScanner != null) {
            settingsEditor.putBoolean(Constants.PREF_NOTIFY_ACTIVE, activeScanner.isChecked()).commit();
        }
        if (barcodeEvent != null) {
            settingsEditor.putBoolean(Constants.PREF_NOTIFY_BARCODE,barcodeEvent.isChecked() ).commit();
        }
        if (setDefaults != null) {
            settingsEditor.putBoolean(Constants.PREF_PAIRING_BARCODE_CONFIG, setDefaults.isChecked()).commit();
        }
        /*  Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        // save barcode type value to 0(ScanToConnect Suite)
        if (barcodeType != null) {
            settingsEditor.putInt(Constants.PREF_PAIRING_BARCODE_TYPE, barcodeType.getSelectedItemPosition()).commit();
        }
        */
        settingsEditor.putInt(Constants.PREF_PAIRING_BARCODE_TYPE, 0).commit();

        if (comProtocol != null) {
            settingsEditor.putInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE, comProtocol.getSelectedItemPosition()).commit();
        }
        initializeDcsSdkWithAppSettings();

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

    public  void setDisplayListProtocolType(int val){
        if(val == 0){
            protocolList.clear();
            protocolList.add("SSI over Bluetooth LE");
            protocolList.add("SSI over Bluetooth classic");
            protocolAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ApplySharedPref")
    public void clearBTAddress(View view) {
        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        settingsEditor.putString(Constants.PREF_BT_ADDRESS, "").commit(); // Commit is required here. So suppressing warning.

        TextView bluetoothAddress = (TextView)findViewById(R.id.txt_bt_address);
        bluetoothAddress.setText("Bluetooth Address");
        if(Application.sdkHandler !=null){
            Application.sdkHandler.dcssdkClearBTAddress();
            Application.sdkHandler.dcssdkSetSTCEnabledState(false);
        }
    }
}
