package com.zebra.scannercontrol.app.activities;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.documentfile.provider.DocumentFile;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.BuildConfig;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends BaseActivity implements View.OnClickListener, OnCheckedChangeListener {

    Button btnResetDefaults = null;
    Spinner comProtocol;
    Spinner logFormat;
    List<String> spinnerArrayHost;
    private List<String> protocolList;
    private List<String> logFormatList;
    ArrayAdapter<String> adapterHost;
    private ArrayAdapter<String> protocolAdapter;
    private ArrayAdapter<String> logFormatAdapter;
    private static int BARCODE_TYPE_SCANTOCONNECT = 0; // 0 : ScanToConnect Suite
    Dialog dialogAddFriendlyName;
    TableRow tableRowAddFriendlyName = null;
    private String customizedFriendlyName;
    public static Boolean isNavigateToChooseSMSDir = false;
    private static int SMS_DIR_CHOOSER_RESPONSE = 119;
    private static int MANAGE_ALL_FILE_ACCESS = 129;
    Uri persistedUri;
    TextView btnSMSDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (configuration.smallestScreenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (configuration.screenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        btnResetDefaults = (Button) findViewById(R.id.btnResetAppDefaults);
        tableRowAddFriendlyName = findViewById(R.id.tableRowAddFriendlyName);
        tableRowAddFriendlyName.setOnClickListener(this);
        btnSMSDir = (TextView) findViewById(R.id.sms_dir_path_button);

        //when the customized SMS folder already defined on shared preference
        if(!settings.getString(Constants.CUSTOM_SMS_DIR, "0").equals("0")){
            pathSetInTextView(settings.getString(Constants.CUSTOM_SMS_DIR, "0"));
        }
        btnSMSDir.setOnClickListener(this);

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

        SwitchCompat autoConnection = (SwitchCompat) findViewById(R.id.autoConnection);
        if (autoConnection != null) {
            autoConnection.setOnCheckedChangeListener(this);
        }

        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        if (scannerDiscovery != null) {
            scannerDiscovery.setOnCheckedChangeListener(this);
        }

        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        if (setDefaults != null) {
            setDefaults.setOnCheckedChangeListener(this);
        }

        SwitchCompat classicDeviceFiltration = (SwitchCompat) findViewById(R.id.classicDeviceFiltration);
        if (classicDeviceFiltration != null) {
            classicDeviceFiltration.setOnCheckedChangeListener(this);
        }


        comProtocol = (Spinner) findViewById(R.id.spinner_com_protocol);
        logFormat = (Spinner) findViewById(R.id.spinner_log_format);
        protocolList = new ArrayList<String>();
        logFormatList = new ArrayList<>();
        selectLogFileFormat();

        // Edit(2018/01/02) : SSDK_6172_Remove legacy barcode type
        // save barcode type value to 0(ScanToConnect Suite)
        SharedPreferences.Editor settingsEditor = settings.edit();

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
        AdapterView.OnItemSelectedListener listner = new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedProtocolIndex = 0;
                Spinner spinnerComProtocol = (Spinner) findViewById(R.id.spinner_com_protocol);
                if (spinnerComProtocol != null) {
                    selectedProtocolIndex = spinnerComProtocol.getSelectedItemPosition();
                }
                SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                settingsEditor.putInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE, selectedProtocolIndex).commit();// Commit is required here. So suppressing warning.
                selectProtocolByItemIndex(selectedProtocolIndex);
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


    /*
     * Method to select the log file format from spinner dropdown
     * */
    private void selectLogFileFormat(){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        logFormatList = new ArrayList<>();
        logFormatList.add(Constants.XML);
        logFormatList.add(Constants.TXT);
        logFormatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logFormatList);
        logFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        logFormat.setAdapter(logFormatAdapter);
        logFormat.setSelection(settings.getInt(Constants.LOG_FORMAT,0));

        logFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                settingsEditor.putInt(Constants.LOG_FORMAT,position).commit();

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

    }

    /**
     * Method to set the selected protocol enum by protocol list index
     *
     * @param itemIndex - index of the protocol list
     */
    private void selectProtocolByItemIndex(int itemIndex) {
        DCSSDKDefs.DCSSDK_BT_PROTOCOL selectedProtocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE;
        if (itemIndex == 0) {
            //BT LE
            selectedProtocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE;
        } else {
            //BT CLASSIC
            selectedProtocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST;
        }
        setCommunicationProtocol(selectedProtocol);
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

            case R.id.autoConnection: {
                TextView textView = (TextView) findViewById(R.id.txt_auto_connection);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_SCANNER_CONNECTION, ((SwitchCompat) findViewById(R.id.autoConnection)).isChecked()).apply();
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
            case R.id.availableScanner: {
                TextView textView = (TextView) findViewById(R.id.txt_available_scanner);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_NOTIFY_AVAILABLE, ((SwitchCompat) findViewById(R.id.availableScanner)).isChecked()).commit();
                break;
            }
            case R.id.activeScanner: {
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
            case R.id.barcodeEvent: {
                TextView textView = (TextView) findViewById(R.id.txt_barcode_event);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    }
                }
                settingsEditor.putBoolean(Constants.PREF_NOTIFY_BARCODE, ((SwitchCompat) findViewById(R.id.barcodeEvent)).isChecked()).commit();
                break;
            }
            case R.id.set_factory_defaults: {
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
            case R.id.classicDeviceFiltration: {
                TextView textView = (TextView) findViewById(R.id.txt_filter_device);
                if (textView != null) {
                    if (isChecked) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                        enableAddFriendlyName(true);
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                        enableAddFriendlyName(false);
                    }
                }
                Application.sdkHandler.dcssdkEnableBluetoothClassicFiltration(isChecked);
                settingsEditor.putBoolean(Constants.PREF_SCANNER_CLASSIC_FILTER,isChecked).apply();
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
            case R.id.tableRowAddFriendlyName: {
                openAddFriendlyNameDialog();
                break;
            }
            case R.id.sms_dir_path_button:{
                isNavigateToChooseSMSDir = true; // start browse with flag on and flag off on onActivityResult
                //choose SMS Directory
                startActivityForResult((new Intent("android.intent.action.OPEN_DOCUMENT_TREE")).putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildRootsUri("com.android.externalstorage.documents")), SMS_DIR_CHOOSER_RESPONSE);
                break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SMS_DIR_CHOOSER_RESPONSE && resultCode == -1) {

            this.persistedUri = data.getData();
            this.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            manageSMSDirPath();


        }
    }

    /**
     * decode display and save path
     */
    private void manageSMSDirPath(){
        //decode the file path from uri
        String fileLocation = persistedUri.toString().split("tree/")[1];
        try {
            String fileLocationDecoded = new URI(fileLocation).getPath();
            //displaySmsPackageFolder(fileLocationDecoded.replace("primary:","/"));
            String smsFolderPathString = fileLocationDecoded.replace("primary:", "/");
            if (smsFolderPathString.contains("/storage/emulated/0")) {
                smsFolderPathString = smsFolderPathString.replace("/storage/emulated/0", "");
            }
            //check the selected folder is valid or not
            if(smsFolderPathString.contains("Documents")||smsFolderPathString.contains("Download")){
                saveSMSFolderPath(smsFolderPathString);
                pathSetInTextView(smsFolderPathString);
                isNavigateToChooseSMSDir = false;//flag off
            }else{
                //show popup Incompatible folder Selected
                showAlertDialogTONavigatePermission(smsFolderPathString);
            }


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert to grant Manage All File Access Permission
     */
    private void showAlertDialogTONavigatePermission(String smsFolderPathString){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Incompatible folder Selected...!!")
                .setMessage("The selected folder is :"+ smsFolderPathString +". Please select folder inside Documents or Download")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            startActivityForResult((new Intent("android.intent.action.OPEN_DOCUMENT_TREE")).putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildRootsUri("com.android.externalstorage.documents")), SMS_DIR_CHOOSER_RESPONSE);
                        } catch (Exception ex) {
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * display SMS folder path on test view with text changes
     * @param path
     */
    private void pathSetInTextView(String path){
        btnSMSDir.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        btnSMSDir.setSelected(true);
        btnSMSDir.setSingleLine(true);
        btnSMSDir.setText(path);
        if (SDK_INT >= Build.VERSION_CODES.M) {
            btnSMSDir.setTextColor(getColor(R.color.font_color));
        }
    }

    /**
     * save selected path in shared preference
     * @param smsFolderPathToSharedPref
     */
    private void saveSMSFolderPath(String smsFolderPathToSharedPref){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.CUSTOM_SMS_DIR,smsFolderPathToSharedPref);
        editor.apply();
    }

    /**
     * Method to open the dialog to input customized friendly name
     *
     */
    private void openAddFriendlyNameDialog() {
        if (dialogAddFriendlyName == null) {
            dialogAddFriendlyName = new Dialog(SettingsActivity.this);
            dialogAddFriendlyName.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogAddFriendlyName.setContentView(R.layout.dialog_add_friendly_name);

            final EditText editTextFriendlyName = (EditText) dialogAddFriendlyName.findViewById(R.id.editTextFriendlyName);

            TextView cancelButton = (TextView) dialogAddFriendlyName.findViewById(R.id.textViewCancel);
            cancelButton.setOnClickListener(v -> {
                // Close dialog
                dialogAddFriendlyName.dismiss();
                dialogAddFriendlyName = null;
            });

            TextView addButton = (TextView) dialogAddFriendlyName.findViewById(R.id.textViewAdd);
            addButton.setOnClickListener(v -> {
                // Add name to the list
                customizedFriendlyName = editTextFriendlyName.getText().toString().trim();
                if(!customizedFriendlyName.isEmpty()) {
                    Application.sdkHandler.dcssdkAddCustomFriendlyName(customizedFriendlyName);
                    // Close dialog
                    dialogAddFriendlyName.dismiss();
                    dialogAddFriendlyName = null;
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.enter_friendly_name)+ customizedFriendlyName ,Toast.LENGTH_SHORT).show();
                }
            });

            dialogAddFriendlyName.setCancelable(false);
            dialogAddFriendlyName.setCanceledOnTouchOutside(false);
            dialogAddFriendlyName.show();
            Window window = dialogAddFriendlyName.getWindow();
            if (window != null)
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
            selectProtocolByItemIndex(0);
        }

        SwitchCompat setFactoryDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        if (setFactoryDefaults != null) {
            setFactoryDefaults.setChecked(true);
        }

        logFormat.setSelection(0);

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
        Spinner logFormat = (Spinner) findViewById(R.id.spinner_log_format);

        if (autoDetection != null && availableScanner!=null && activeScanner!=null && barcodeEvent!=null && setDefaults!=null /* SSDK_6172: && barcodeType!=null*/) {
            if ((autoDetection.isChecked())
                    && (!availableScanner.isChecked())
                    && (!activeScanner.isChecked())
                    && (!barcodeEvent.isChecked())
                    && (setDefaults.isChecked())
                    && (logFormat.getSelectedItemPosition() == 0)
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


        SwitchCompat autoConnection = (SwitchCompat) findViewById(R.id.autoConnection);
        if (autoConnection != null) {
            autoConnection.setChecked(settings.getBoolean(Constants.PREF_SCANNER_CONNECTION, false));
            TextView textViewAutoConnection = (TextView) findViewById(R.id.txt_auto_connection);
            if(textViewAutoConnection !=null) {
                if (autoConnection.isChecked()) {
                    textViewAutoConnection.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                } else {
                    textViewAutoConnection.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
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

        SwitchCompat classicFiltration = (SwitchCompat) findViewById(R.id.classicDeviceFiltration);
        if (classicFiltration != null) {
            classicFiltration.setChecked(settings.getBoolean(Constants.PREF_SCANNER_CLASSIC_FILTER, false));
            TextView textViewClassicFiltration = (TextView) findViewById(R.id.txt_filter_device);
            if(textViewClassicFiltration !=null) {
                if (classicFiltration.isChecked()) {
                    textViewClassicFiltration.setTextColor(ContextCompat.getColor(this, R.color.font_color));
                    enableAddFriendlyName(true);
                } else {
                    textViewClassicFiltration.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
                    enableAddFriendlyName(false);
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
        selectProtocolByItemIndex(protocol);
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

    /**
     * Method to enable/disable the add friendly name functionality
     *
     * @param enable - boolean indicating whether to enable filter or diable
     */
    private void enableAddFriendlyName(boolean enable) {
        TextView textViewAddFriendlyName = (TextView) findViewById(R.id.txt_add_friendly_name);
        if (enable) {
            textViewAddFriendlyName.setTextColor(ContextCompat.getColor(this, R.color.font_color));
            tableRowAddFriendlyName.setEnabled(true);
        } else {
            textViewAddFriendlyName.setTextColor(ContextCompat.getColor(this, R.color.inactive_text));
            tableRowAddFriendlyName.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save preferences
        SaveSettings();
        if (isInBackgroundMode(getApplicationContext())) {
            if(!isNavigateToChooseSMSDir) { //if user navigate to select SMS folder Settings activity should keep on background
                finish();
            }
        }
    }

    private void SaveSettings() {

        SwitchCompat autoDetection = (SwitchCompat) findViewById(R.id.autoDetection);
        SwitchCompat autoConnection = (SwitchCompat) findViewById(R.id.autoConnection);
        SwitchCompat scannerDiscovery = (SwitchCompat) findViewById(R.id.bluetoothScannerDetection);
        SwitchCompat availableScanner = (SwitchCompat) findViewById(R.id.availableScanner);
        SwitchCompat activeScanner = (SwitchCompat) findViewById(R.id.activeScanner);
        SwitchCompat barcodeEvent = (SwitchCompat) findViewById(R.id.barcodeEvent);
        SwitchCompat setDefaults = (SwitchCompat) findViewById(R.id.set_factory_defaults);
        //Spinner barcodeType = (Spinner)findViewById(R.id.spinner_type);
        Spinner comProtocol = (Spinner)findViewById(R.id.spinner_com_protocol);
        Spinner logFormat = (Spinner) findViewById(R.id.spinner_log_format);

        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        if (autoDetection != null) {
            settingsEditor.putBoolean(Constants.PREF_SCANNER_DETECTION, autoDetection.isChecked()).apply();
        }

        if (autoConnection != null) {
            settingsEditor.putBoolean(Constants.PREF_SCANNER_CONNECTION, autoConnection.isChecked()).apply();
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
            selectProtocolByItemIndex(comProtocol.getSelectedItemPosition());
        }

        if (logFormat != null){
            settingsEditor.putInt(Constants.LOG_FORMAT,logFormat.getSelectedItemPosition()).commit();
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
