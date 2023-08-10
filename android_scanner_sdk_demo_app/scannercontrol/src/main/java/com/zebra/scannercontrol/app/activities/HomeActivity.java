package com.zebra.scannercontrol.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.IDcsScannerEventsOnReLaunch;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.AvailableScanner;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity
        implements IDcsScannerEventsOnReLaunch, NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {
    public static final String BLUETOOTH_ADDRESS_VALIDATOR = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 10;
    private static final int MAX_ALPHANUMERIC_CHARACTERS = 12;
    private static final int MAX_BLUETOOTH_ADDRESS_CHARACTERS = 17;
    private static final String DEFAULT_EMPTY_STRING = "";
    private static final String COLON_CHARACTER = ":";
    static boolean firstRun = true;
    static String btAddress;
    static String userEnteredBluetoothAddress;
    static AvailableScanner curAvailableScanner = null;

    Menu menu;
    MenuItem pairNewScannerMenu;
    Dialog dialog;
    Dialog dialogBTAddress;
    private FrameLayout llBarcode;
    private NavigationView navigationView;
    private GoogleApiClient googleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    AlertDialog alertDialog;
    ArrayList<String> permissionsList;
    int permissionsCount = 0;
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_13_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private final static int REQUEST_ENABLE_BT=1;
    BluetoothAdapter mBluetoothAdapter;

    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String,Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                for (int i = 0; i < list.size(); i++) {
                                    if (shouldShowRequestPermissionRationale(ANDROID_13_BLE_PERMISSIONS[i])) {
                                        permissionsList.add(ANDROID_13_BLE_PERMISSIONS[i]);
                                    } else if (!hasPermission(HomeActivity.this, ANDROID_13_BLE_PERMISSIONS[i])) {
                                        permissionsCount++;
                                    }
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                                for (int i = 0; i < list.size(); i++) {
                                    if (shouldShowRequestPermissionRationale(ANDROID_12_BLE_PERMISSIONS[i])) {
                                        permissionsList.add(ANDROID_12_BLE_PERMISSIONS[i]);
                                    }else if (!hasPermission(HomeActivity.this, ANDROID_12_BLE_PERMISSIONS[i])){
                                        permissionsCount++;
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    if (shouldShowRequestPermissionRationale(BLE_PERMISSIONS[i])) {
                                        permissionsList.add(BLE_PERMISSIONS[i]);
                                    }else if (!hasPermission(HomeActivity.this, BLE_PERMISSIONS[i])){
                                        permissionsCount++;
                                    }
                                }
                            }
                            if (permissionsList.size() > 0) {
                                //Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                //Show alert dialog
                                showPermissionDialog();
                            } else {
                                //All permissions granted
                                initialize();
                            }
                        }
                    });
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        googleApiClient = getAPIClientInstance();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.addAll(Arrays.asList(ANDROID_13_BLE_PERMISSIONS));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionsList.addAll(Arrays.asList(ANDROID_12_BLE_PERMISSIONS));
        } else {
            permissionsList.addAll(Arrays.asList(BLE_PERMISSIONS));
        }
        askForPermissions(permissionsList);
    }

    /**
     * Method to request required permissions with the permission launcher
     * @param permissionsList list of permissions
     */
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            showPermissionDialog();
        }


    }


    /**
     * Method to redirect to application settings if user denies the permissions
     */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setCancelable(false)
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    //askForPermissions(permissionsList);
                    openAppSettings();
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    /**
     * Method to open application settings
     */
    public void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",HomeActivity.this.getPackageName(), null);
        intent.setData(uri);
        HomeActivity.this.startActivity(intent);
    }

    /**
     * Method to check permissions granted status
     * @param context
     * @param permissionStr
     * @return true if permission granted otherwise false
     */
    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * GoogleApiClient instance
     *
     * @return GoogleApiClient
     */
    private GoogleApiClient getAPIClientInstance() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }

    /**
     * check the location settings status and ask for Location settings on
     */
    private void requestDeviceLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(500);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, e.toString());

                        }
                        break;
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initialize() {
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        initializeDcsSdk();
        llBarcode = (FrameLayout) findViewById(R.id.scan_to_connect_barcode);
        addDevConnectionsDelegate(this);
        setTitle("Pair New Scanner");
        broadcastSCAisListening();
    }

    private void broadcastSCAisListening() {
        Intent intent = new Intent();
        intent.setAction("com.zebra.scannercontrol.LISTENING_STARTED");
        sendBroadcast(intent);
    }

    private void updateBarcodeView(LinearLayout.LayoutParams layoutParams, BarCodeView barCodeView) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int orientation = this.getResources().getConfiguration().orientation;
        int x = width * 9 / 10;
        int y = x / 3;

        if (getDeviceScreenSize() > 6) { // TODO: Check 6 is ok or not
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                x = width / 2;
            } else {
                x = width * 2 / 3;
            }
            y = x / 3;
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
            double x = Math.pow(mWidthPixels / dm.xdpi, 2);
            double y = Math.pow(mHeightPixels / dm.ydpi, 2);
            screenInches = Math.sqrt(x + y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenInches;
    }



    private void initializeDcsSdk() {
        Application.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            if (Application.getAnyScannerConnectedStatus()) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle("This will disconnect your current scanner");
                //dlg.setIcon(android.R.drawable.ic_dialog_alert);
                dlg.setPositiveButton("Continue", (dialog, arg) -> {

                    disconnect(Application.currentConnectedScannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent_home = new Intent(HomeActivity.this, HomeActivity.class);
                    startActivity(intent_home);
                });

                dlg.setNegativeButton("Cancel", (dialog, arg) -> {

                });
                dlg.show();

            }
        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            intent = new Intent(this, FindCabledScanner.class);
            startActivity(intent);
        } else if (id == R.id.nav_connection_help) {
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

        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO: https://jiraemv.zebra.com/browse/SSDK-5961
        // Application.sdkHandler.dcssdkClose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestDeviceLocationSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: HOME--------------------------------------------");
        addDevConnectionsDelegate(this);
        navigationView.getMenu().findItem(R.id.nav_about).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_pair_device).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_pair_device).setCheckable(false);
        navigationView.getMenu().findItem(R.id.nav_devices).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_connection_help).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_settings).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_find_cabled_scanner).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_find_cabled_scanner).setCheckable(false);
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        TextView txtBarcodeType = (TextView) findViewById(R.id.scan_to_connect_barcode_type);
        TextView txtScannerConfiguration = (TextView) findViewById(R.id.scan_to_connect_scanner_config);
        String sourceString = "";
        txtBarcodeType.setText(Html.fromHtml(sourceString));
        txtScannerConfiguration.setText("");
        boolean dntShowMessage = settings.getBoolean(Constants.PREF_DONT_SHOW_INSTRUCTIONS, false);
        int barcode = settings.getInt(Constants.PREF_PAIRING_BARCODE_TYPE, 0);
        boolean setDefaults = settings.getBoolean(Constants.PREF_PAIRING_BARCODE_CONFIG, true);
        int protocolInt = settings.getInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE, 0);
        String strProtocol = "SSI over Bluetooth LE";
        llBarcode = (FrameLayout) findViewById(R.id.scan_to_connect_barcode);
        DCSSDKDefs.DCSSDK_BT_PROTOCOL protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.LEGACY_B;
        DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG config = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT;
        if (barcode == 0) {
            txtBarcodeType.setText("");
            txtScannerConfiguration.setText("");
            sourceString = "STC Barcode ";
            txtBarcodeType.setText(Html.fromHtml(sourceString));
            switch (protocolInt) {
                case 0:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE;//SSI over Bluetooth LE
                    strProtocol = "Bluetooth LE";
                    break;
                case 1:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST;//SSI over Classic Bluetooth
                    strProtocol = "SSI over Classic Bluetooth";
                    break;
                default:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE;//SSI over Bluetooth LE
                    break;
            }
            if (setDefaults) {
                config = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.SET_FACTORY_DEFAULTS;
                txtScannerConfiguration.setText(Html.fromHtml("<i> Set Factory Defaults, Com Protocol = " + strProtocol + "</i>"));
            } else {
                txtScannerConfiguration.setText(Html.fromHtml("<i> Keep Current Settings, Com Protocol = " + strProtocol + "</i>"));
            }
        } else {
            sourceString = "Legacy Pairing ";
            txtBarcodeType.setText(Html.fromHtml(sourceString));
            txtScannerConfiguration.setText("");
        }
        selectedProtocol = protocol;
        setCommunicationProtocol(selectedProtocol);
        selectedConfig = config;
        generatePairingBarcode();
        if (dialogBTAddress == null && firstRun && !dntShowMessage) {
            dialog = new Dialog(HomeActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_launch_instructions);

            CheckBox chkDontShow = (CheckBox) dialog.findViewById(R.id.chk_dont_show);
            chkDontShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @SuppressLint("ApplySharedPref")
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                        settingsEditor.putBoolean(Constants.PREF_DONT_SHOW_INSTRUCTIONS, true).commit(); // Commit is required here. So suppressing warning.
                    }
                }
            });
            TextView continueButton = (TextView) dialog.findViewById(R.id.btn_continue);
            // if decline button is clicked, close the custom dialog
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                    dialog = null;
                }
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null)
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            firstRun = false;
        }


        if ((barcode == 0)
                && (setDefaults == true)
                && (protocolInt == 0)) {
            txtScannerConfiguration.setText(Html.fromHtml("<i> Factory Defaults Are Set, Com Protocol = " + strProtocol + "</i>"));
        } else {
            if (barcode == 0) {
                txtBarcodeType.setText(Html.fromHtml(sourceString));
            } else {
                txtBarcodeType.setText(Html.fromHtml(sourceString));
            }

        }

        if(Application.isScannerConnectedAfterSMS && Application.currentConnectedScannerID != -1) {
            scannerHasConnected(Application.currentConnectedScannerID);
        }

    }

    private void generatePairingBarcode() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        BarCodeView barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig);
        if (barCodeView != null) {
            updateBarcodeView(layoutParams, barCodeView);
        } else {
            // SDK was not able to determine Bluetooth MAC. So call the dcssdkGetPairingBarcode with BT Address.

            btAddress = getDeviceBTAddress(settings);
            if (btAddress.equals("")) {
                llBarcode.removeAllViews();
            } else {
                Application.sdkHandler.dcssdkSetBTAddress(btAddress);
                barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig, btAddress);
                if (barCodeView != null) {
                    updateBarcodeView(layoutParams, barCodeView);
                }
            }
        }
    }

    private String getDeviceBTAddress(SharedPreferences settings) {
        String bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
        if (bluetoothMAC.equals("")) {
            if (dialogBTAddress == null) {
                dialogBTAddress = new Dialog(HomeActivity.this);
                dialogBTAddress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogBTAddress.setContentView(R.layout.dialog_get_bt_address);

                final TextView cancelContinueButton = (TextView) dialogBTAddress.findViewById(R.id.cancel_continue);
                final TextView abtPhoneButton = (TextView) dialogBTAddress.findViewById(R.id.abt_phone);
                final TextView skipButton = dialogBTAddress.findViewById(R.id.skip);
                abtPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent statusSettings = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                        startActivity(statusSettings);
                    }

                });
                cancelContinueButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(View view) {
                        if (cancelContinueButton.getText().equals(getResources().getString(R.string.cancel))) {
                            if (dialogBTAddress != null) {
                                dialogBTAddress.dismiss();
                                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                                startActivity(intent);
                            }

                        } else {
                            Application.sdkHandler.dcssdkSetSTCEnabledState(true);
                            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                            settingsEditor.putString(Constants.PREF_BT_ADDRESS, userEnteredBluetoothAddress).commit();// Commit is required here. So suppressing warning.
                            if (dialogBTAddress != null) {
                                dialogBTAddress.dismiss();
                                dialogBTAddress = null;
                            }
                            startHomeActivityAgain();
                        }
                    }
                });

                skipButton.setOnClickListener(view -> {
                    Application.sdkHandler.dcssdkSetSTCEnabledState(false);
                    Intent intent = new Intent(HomeActivity.this, ScannersActivity.class);
                    startActivity(intent);
                });

                final EditText editTextBluetoothAddress = (EditText) dialogBTAddress.findViewById(R.id.text_bt_address);
                editTextBluetoothAddress.addTextChangedListener(new TextWatcher() {
                    String previousMac = null;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String enteredMacAddress = editTextBluetoothAddress.getText().toString().toUpperCase();
                        String cleanMacAddress = clearNonMacCharacters(enteredMacAddress);
                        String formattedMacAddress = formatMacAddress(cleanMacAddress);

                        int selectionStart = editTextBluetoothAddress.getSelectionStart();
                        formattedMacAddress = handleColonDeletion(enteredMacAddress, formattedMacAddress, selectionStart);
                        int lengthDiff = formattedMacAddress.length() - enteredMacAddress.length();

                        setMacEdit(cleanMacAddress, formattedMacAddress, selectionStart, lengthDiff);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        userEnteredBluetoothAddress = s.toString();
                        if (userEnteredBluetoothAddress.length() > MAX_BLUETOOTH_ADDRESS_CHARACTERS)
                            return;

                        if (isValidBTAddress(userEnteredBluetoothAddress)) {

                            Drawable dr = getResources().getDrawable(R.drawable.tick);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            editTextBluetoothAddress.setCompoundDrawables(null, null, dr, null);
                            cancelContinueButton.setText(getResources().getString(R.string.continue_txt));

                        } else {
                            editTextBluetoothAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            cancelContinueButton.setText(getResources().getString(R.string.cancel));

                        }
                    }

                    /**
                     * Strips all characters from a string except A-F and 0-9
                     * (Keep Bluetooth address allowed characters only).
                     *
                     * @param inputMacString User input string.
                     * @return String containing bluetooth MAC-allowed characters.
                     */
                    private String clearNonMacCharacters(String inputMacString) {
                        return inputMacString.replaceAll("[^A-Fa-f0-9]", DEFAULT_EMPTY_STRING);
                    }

                    /**
                     * Adds a colon character to an unformatted bluetooth MAC address after
                     * every second character (strips full MAC trailing colon)
                     *
                     * @param cleanMacAddress Unformatted MAC address.
                     * @return Properly formatted MAC address.
                     */
                    private String formatMacAddress(String cleanMacAddress) {
                        int groupedCharacters = 0;
                        String formattedMacAddress = DEFAULT_EMPTY_STRING;

                        for (int i = 0; i < cleanMacAddress.length(); ++i) {
                            formattedMacAddress += cleanMacAddress.charAt(i);
                            ++groupedCharacters;

                            if (groupedCharacters == 2) {
                                formattedMacAddress += COLON_CHARACTER;
                                groupedCharacters = 0;
                            }
                        }

                        // Removes trailing colon for complete MAC address
                        if (cleanMacAddress.length() == MAX_ALPHANUMERIC_CHARACTERS)
                            formattedMacAddress = formattedMacAddress.substring(0, formattedMacAddress.length() - 1);

                        return formattedMacAddress;
                    }

                    /**
                     * Upon users colon deletion, deletes bluetooth MAC character preceding deleted colon as well.
                     *
                     * @param enteredMacAddress     User input MAC.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @return Formatted MAC address.
                     */
                    private String handleColonDeletion(String enteredMacAddress, String formattedMacAddress, int selectionStartPosition) {
                        if (previousMac != null && previousMac.length() > 1) {
                            int previousColonCount = colonCount(previousMac);
                            int currentColonCount = colonCount(enteredMacAddress);

                            if (currentColonCount < previousColonCount) {
                                try {
                                    formattedMacAddress = formattedMacAddress.substring(0, selectionStartPosition - 1) + formattedMacAddress.substring(selectionStartPosition);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String cleanMacAddress = clearNonMacCharacters(formattedMacAddress);
                                formattedMacAddress = formatMacAddress(cleanMacAddress);
                            }
                        }
                        return formattedMacAddress;
                    }

                    /**
                     * Gets bluetooth MAC address current colon count.
                     *
                     * @param formattedMacAddress Formatted MAC address.
                     * @return Current number of colons in MAC address.
                     */
                    private int colonCount(String formattedMacAddress) {
                        return formattedMacAddress.replaceAll("[^:]", DEFAULT_EMPTY_STRING).length();
                    }

                    /**
                     * Removes TextChange listener, sets MAC EditText field value,
                     * sets new cursor position and re-initiates the listener.
                     *
                     * @param cleanMacAddress       Clean MAC address.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @param characterDifferenceLength     Formatted/Entered MAC number of characters difference.
                     */
                    private void setMacEdit(String cleanMacAddress, String formattedMacAddress, int selectionStartPosition, int characterDifferenceLength) {
                        editTextBluetoothAddress.removeTextChangedListener(this);
                        if (cleanMacAddress.length() <= MAX_ALPHANUMERIC_CHARACTERS) {
                            editTextBluetoothAddress.setText(formattedMacAddress);

                            editTextBluetoothAddress.setSelection(selectionStartPosition + characterDifferenceLength);
                            previousMac = formattedMacAddress;
                        } else {
                            editTextBluetoothAddress.setText(previousMac);
                            editTextBluetoothAddress.setSelection(previousMac.length());
                        }
                        editTextBluetoothAddress.addTextChangedListener(this);
                    }

                });

                dialogBTAddress.setCancelable(false);
                dialogBTAddress.setCanceledOnTouchOutside(false);
                dialogBTAddress.show();
                Window window = dialogBTAddress.getWindow();
                if (window != null)
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
            } else {
                dialogBTAddress.show();
            }
        }
        return bluetoothMAC;
    }

    private void startHomeActivityAgain() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);
    }

    public boolean isValidBTAddress(String text) {
        return text != null && text.length() > 0 && text.matches(BLUETOOTH_ADDRESS_VALIDATOR);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadBarcode();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null)
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void reloadBarcode() {
        generatePairingBarcode();
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


        SsaSetSymbologyActivity.resetScanSpeedAnalyticSettings();

        ArrayList<DCSScannerInfo> activeScanners = new ArrayList<DCSScannerInfo>();
        Application.sdkHandler.dcssdkGetActiveScannersList(activeScanners);
        Intent intent = new Intent(HomeActivity.this, ActiveScannerActivity.class);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);

        for (DCSScannerInfo scannerInfo : Application.mScannerInfoList) {
            if (scannerInfo.getScannerID() == scannerID) {
                intent.putExtra(Constants.SCANNER_NAME, scannerInfo.getScannerName());
                intent.putExtra(Constants.SCANNER_TYPE, scannerInfo.getConnectionType().value);
                intent.putExtra(Constants.SCANNER_ADDRESS, scannerInfo.getScannerHWSerialNumber());
                intent.putExtra(Constants.SCANNER_ID, scannerInfo.getScannerID());
                intent.putExtra(Constants.AUTO_RECONNECTION, scannerInfo.isAutoCommunicationSessionReestablishment());
                intent.putExtra(Constants.CONNECTED, true);
                intent.putExtra(Constants.PICKLIST_MODE, getPickListMode(scannerID));
                intent.putExtra(Constants.BEEPER_VOLUME, getBeeperVolume(scannerID));

                if (scannerInfo.getScannerModel() != null && scannerInfo.getScannerModel().startsWith("PL3300")) { // remove this condition when CS4070 get the capability
                    intent.putExtra(Constants.PAGER_MOTOR_STATUS, true);
                } else {
                    intent.putExtra(Constants.PAGER_MOTOR_STATUS, isPagerMotorAvailable(scannerID));
                }

                Application.setAnyScannerConnectedStatus(true);
                Application.currentConnectedScannerID = scannerID;
                Application.currentConnectedScanner = scannerInfo;
                Application.currentScannerType = scannerInfo.getConnectionType().value;
                Application.lastConnectedScanner = Application.currentConnectedScanner;
                if (!Application.isSMSExecutionInProgress) {
                    startActivity(intent);
                }
                break;
            }
        }
        return true;
    }

    private int getBeeperVolume(int scannerID) {
        int beeperVolume = 0;

        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>140</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outXML, scannerID);

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(outXML.toString()));
            int event = parser.getEventType();
            String text = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("value")) {
                            beeperVolume = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                    default: break; //XmlPullParser.START_TAG
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (beeperVolume == 0) {
            return 100;
        } else if (beeperVolume == 1) {
            return 50;
        } else {
            return 0;
        }
    }

    private boolean isPagerMotorAvailable(int scannerID) {
        boolean isFound = false;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>613</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outXML, scannerID);
        if (outXML.toString().contains("<id>613</id>")) {
            isFound = true;
        }
        return isFound;
    }

    private int getPickListMode(int scannerID) {
        int attrVal = 0;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>402</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outXML, scannerID);

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(outXML.toString()));
            int event = parser.getEventType();
            String text = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("value")) {
                            attrVal = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                    default: break; //XmlPullParser.START_TAG
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return attrVal;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        Application.setAnyScannerConnectedStatus(false);
        Application.currentConnectedScannerID = -1;
        Application.lastConnectedScanner = Application.currentConnectedScanner;
        Application.currentConnectedScanner = null;
        return false;
    }


    /**
     * callback from BluetoothLEManager to show detected last connected device and get
     * setting "Auto connect to last scanner"
     * @param device
     * @return boolean - (application settings scanner connect to last scanner)?true:false
     */
    @Override
    public boolean onLastConnectedScannerDetect(BluetoothDevice device) {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        return settings.getBoolean(Constants.PREF_SCANNER_CONNECTION, false);
    }

    /**
     * callback from BluetoothLeManager to show connecting last connected device
     * @param device
     */
    @Override
    public void onConnectingToLastConnectedScanner(BluetoothDevice device) {

        int progressUntil = 31 * 1000;
        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage(getString(R.string.progress_connecting_on_launch)+device.getName());
        progressDialog.setCancelable(false);
        progressDialog.show();

        // progress will dismiss after 'progressUntil'
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(progressDialog!=null){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                }
            }
        }, progressUntil);
    }


    /**
     * callback from BluetoothLeManager to show disconnected device
     */
    @Override
    public void onScannerDisconnect() {
        if(progressDialog!=null){
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(progressDialog!=null){
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

}
