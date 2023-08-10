package com.zebra.scannercontrol.app.activities;

import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_CONFIG_NAME;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_DOM;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_FW_VERSION;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_MODEL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_SERIAL_NUMBER;
import static com.zebra.scannercontrol.app.helpers.LogFile.iLogFormat;
import static com.zebra.scannercontrol.app.helpers.LogFile.logFilePath;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.barcode.sdk.sms.ConfigHelper;
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent;
import com.zebra.barcode.sdk.sms.SmsPackageUpdateManager;
import com.zebra.barcode.sdk.sms.entities.ScannerConfigModel;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.DotsProgressBar;
import com.zebra.scannercontrol.app.helpers.FileObserverService;
import com.zebra.scannercontrol.app.helpers.LogFile;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class is responsible for handling activities related to SMS Package execution
 */
public class ExecuteSmsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevEventsDelegate, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_URI = 200;

    Menu menu;
    MenuItem pairNewScannerMenu;
    int scannerID;
    int communicationMode = -1;
    String comMode;
    private static Uri persistedUri;

    int dialogSMSProgessY = 220;
    int dialogSMSReconnectionX = 50;
    Dialog dialogSMSProgress;
    TextView txtPercentage, smsFileName, txtReleaseNoteInfo;
    ProgressBar progressBar;
    static Dialog dialogFwProgress;
    static Dialog dialogFwRebooting;
    static Dialog dialogFwReconnectScanner;
    DotsProgressBar dotProgressBar;
    BarCodeView barCodeView = null;
    FrameLayout frameLayoutBarcode = null;
    static boolean isWaitingForFWUpdateCompletion;
    private DCSScannerInfo fwUpdatingScanner;
    static boolean fwReboot;
    String currentFirmware,DOM,modelNumber,serialNumber,configName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
    LogFile logFile;
    SharedPreferences preferences;
    String SMS_DIR = "/ZebraSMS";
    String SMS_LOG_DIR = "/logs";
    String CUSTOM_SMS_DIR;
    boolean bServiceRunning = false;
    Button btn_execute_sms;
    Dialog dialog_overlay;


    private final Handler pnpHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: if (dialogFwRebooting != null) {
                    dialogFwRebooting.dismiss();
                    dialogFwRebooting = null;
                    reconnectScanner();
                }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_execute_sms);
        btn_execute_sms = findViewById(R.id.btn_execute_sms);
        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        //get selected directory from shared preferences
        CUSTOM_SMS_DIR = preferences.getString(Constants.CUSTOM_SMS_DIR, "0");

        if(!getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
            logFile = new LogFile(this);
            iLogFormat = preferences.getInt(Constants.LOG_FORMAT,0);
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Toolbar subActionBar = findViewById(R.id.sub_actionbar);
        setSupportActionBar(subActionBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_execute_sms);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);

        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        communicationMode = getIntent().getIntExtra(Constants.SCANNER_TYPE, -1);
        if(Application.currentConnectedScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_INVALID){
            comMode = "INVALID";
        }else if(Application.currentConnectedScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_NORMAL) {
            comMode = "BT NORMAL";
        }else if(Application.currentConnectedScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
            comMode = "USB SNAPI";
        }else if(Application.currentConnectedScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_LE) {
            comMode = "BT LE";
        }else if(Application.currentConnectedScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_CDC) {
            comMode = "USB ODC";
        }else {
            comMode = "";
        }
        if(scannerID != -1){
            fetchAssertInfo();
        }else {
            Toast.makeText(this, "No Scanner Available!", Toast.LENGTH_SHORT).show();
        }


        addDevEventsDelegate(this);
        addDevConnectionsDelegate(this);

        requestRuntimePermissionToCreateDirs(ExecuteSmsActivity.this);
        new Handler().postDelayed(() -> isSMSPackageAvailable(),500);

        smsFileName = (TextView) findViewById(R.id.txt_sms_package_name);
        txtReleaseNoteInfo = (TextView) findViewById(R.id.txt_fw_release_notes);

        isWaitingForFWUpdateCompletion = false;
        fwUpdatingScanner = Application.currentConnectedScanner;
        fwReboot = getIntent().getBooleanExtra(Constants.FW_REBOOT, false);


    }


    /**
     *Xml query to fetch assert information from the connected scanner
     */
    private void fetchAssertInfo() {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        if (scannerID != -1) {
            String in_xml = "<inArgs><scannerID>" + scannerID + " </scannerID><cmdArgs><arg-xml><attrib_list>";
            in_xml+=RMD_ATTR_MODEL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_SERIAL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_FW_VERSION;
            in_xml+=",";
            in_xml+=RMD_ATTR_CONFIG_NAME;
            in_xml+=",";
            in_xml+=RMD_ATTR_DOM;
            in_xml += "</attrib_list></arg-xml></cmdArgs></inArgs>";
            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy Removing Update FW IScannerAppEngineDevEventsDelegate from list");
        removeDevConnectiosDelegate(this);
        removeDevEventsDelegate(this);
        isWaitingForFWUpdateCompletion = false;
        Application.isFirmwareUpdateInProgress = false;
        Application.isSMSExecutionInProgress = false;
        logFilePath = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        displaySmsPackageName();
        fwReboot = getIntent().getBooleanExtra(Constants.FW_REBOOT, false);

        if (!fwReboot && isWaitingForFWUpdateCompletion) {
            Log.i(TAG, "Waiting for fw update to be completed");
        }  else if (fwReboot) {
            Application.isFirmwareUpdateInProgress = false;
        }
    }

    /**To display the pop up dialog for Overlay Permission*/
    private void displayDialogOverlayDialog(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
            boolean dntShowOverlayMessage = settings.getBoolean(Constants.PREF_DONT_SHOW_OVERLAY_INSTRUCTIONS, false);
            if(!Settings.canDrawOverlays(ExecuteSmsActivity.this) && !dntShowOverlayMessage){
                if(dialog_overlay == null || !dialog_overlay.isShowing()){
                    openDeviceOverlayActionDialog();
                }
            }
        }
    }

    /**
     * Display sms package name
     */
    private void displaySmsPackageName(){
        if (persistedUri != null) {
            displayDialogOverlayDialog();
            Uri smsPackageUri = getSmsPackageUri(persistedUri);
            if (smsPackageUri != null && (smsPackageUri.getPath().endsWith(".smspkg") || smsPackageUri.getPath().endsWith(".SMSPKG"))) {
                String name = getSmsPackageName(persistedUri);
                smsFileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                smsFileName.setSelected(true);
                smsFileName.setSingleLine(true);
                smsFileName.setText(name);

                handleSMSExecution();
            }
        }
    }

    /**This method is used to insert log when there is sms package available when the screen processed from SMSBroadcastReceiver*/
    private void isSMSPackageAvailable(){
        if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
            if(persistedUri != null){
                Uri smsPackageUri = getSmsPackageUri(persistedUri);
                if(smsPackageUri == null || !smsPackageUri.getPath().endsWith(".smspkg") || !smsPackageUri.getPath().endsWith(".SMSPKG")) {
                    try{
                        if(iLogFormat == 0){ //XML
                            LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                    "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                    "\t\t<message> SMS Package File Not Found </message>"+
                                    "\t</info>\n" +
                                    "</firmware_update>\n" +
                                    "\t<end>SMS Completed Successfully</end>\n"+
                                    "</firmware>",logFilePath);
                        }else{ //TXT
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Package File Not Found "+ "\n",logFilePath);
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    getIntent().putExtra(Constants.IS_HANDLING_INTENT,false);
                }
            }else{
                try{
                    if(iLogFormat == 0){ //XML
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message> SMS Package File Not Found </message>"+
                                "\t</info>\n" +
                                "</firmware_update>\n" +
                                "\t<end>SMS Completed Successfully</end>\n"+
                                "</firmware>",logFilePath);
                    }else{ //TXT
                        LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Package File Not Found "+ "\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                getIntent().putExtra(Constants.IS_HANDLING_INTENT,false);
            }
        }
    }

    /**
    * To Trigger the Execute SMS Button Programmatically
    * */
    private void handleSMSExecution(){
        if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)) {
            btn_execute_sms.performClick();
            getIntent().putExtra(Constants.IS_HANDLING_INTENT, false);
        }
    }


    /**Pop up dialog to display the instructions for Overlay permission*/
    private void openDeviceOverlayActionDialog(){
        dialog_overlay = new Dialog(ExecuteSmsActivity.this);
        dialog_overlay.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_overlay.setContentView(R.layout.dialog_overlay);
        TextView btn_continue = dialog_overlay.findViewById(R.id.btn_continue);
        TextView btn_cancel = dialog_overlay.findViewById(R.id.btn_cancel);

        CheckBox chkDontShow = (CheckBox) dialog_overlay.findViewById(R.id.chk_dont_show);
        chkDontShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                settingsEditor.putBoolean(Constants.PREF_DONT_SHOW_OVERLAY_INSTRUCTIONS, true).commit(); // Commit is required here. So suppressing warning.
            }
        });

        btn_continue.setOnClickListener(v -> {
            dialog_overlay.dismiss();
            dialog_overlay = null;
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        });

        btn_cancel.setOnClickListener(v -> {
            dialog_overlay.dismiss();
            dialog_overlay = null;
        });


        dialog_overlay.show();

    }

    /**
     * Method to get the sms package name
     * @return smspkg name
     */
    public String getSmsPackageName(Uri persistentUri) {
        String smsPackageName = null;
        DocumentFile[] zebraSmsFiles = DocumentFile.fromTreeUri(this, persistentUri).listFiles();

        for (DocumentFile documentFile : zebraSmsFiles) {
            if (documentFile.getName().toLowerCase().endsWith(".smspkg")) {
                smsPackageName = documentFile.getName();
            }
        }
        return smsPackageName;
    }

    /**
     * Creates necessary directory structure to upload the sms package
     * returns true if successfully created all the directories otherwise false
     */
    private boolean createDirectoryStructure(Context context) {
        if (createDirectory(SMS_DIR)) {
            return true;
        }
        Toast.makeText(context, "Error occurred during directory structure creation...", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Creates necessary directory structure to create and update the log files
     * returns true if successfully created all the directories otherwise false
     */
    private boolean createLogDirectoryStructure(Context context) {
        if (createDirectory(SMS_DIR + SMS_LOG_DIR)) {
            return true;
        }
        Toast.makeText(context, "Error occurred during directory structure creation...", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Creates a specified directory if not already created
     * @return true if successfully created the given directory otherwise false
     */
    private boolean createDirectory(String filePath) {
        String toLocation;
        if(CUSTOM_SMS_DIR.equals("0")) {
            //default
            toLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + filePath;
        }else{
            //selected from user in settings page
            toLocation = Environment.getExternalStorageDirectory().getPath()+ CUSTOM_SMS_DIR + filePath;
        }
        File directory = new File(toLocation);
        startFileObserverService(directory.getAbsolutePath());
        if (!directory.exists()) {
            return (directory.mkdir());
        } else {
            return true;
        }
    }

    /**
     * To Start the Service to Observe the ZebraSMS Directory for any changes
     * */
    private void startFileObserverService(String folderPath){
        if(!bServiceRunning){
            Intent serviceIntent = new Intent(this, FileObserverService.class);
            serviceIntent.putExtra("INTENT_EXTRA_FILEPATH", folderPath);
            startService(serviceIntent);
            bServiceRunning = true;
        }

    }

    @Override
    public void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent) {
        super.onActivityResult(paramInt1, paramInt2, paramIntent);
        if (paramInt1 == MY_PERMISSIONS_REQUEST_READ_WRITE_URI && paramInt2 == -1) {
            this.persistedUri = paramIntent.getData();
            this.getContentResolver().takePersistableUriPermission(paramIntent.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            displaySmsPackageName();
        }
    }

    /**
     * Method for requesting persistent storage permissions (To resolve storage permission issues from Android 11)
     */
    public void requestPersistentUriPermissions()  {
        if (!getContentResolver().getPersistedUriPermissions().isEmpty()) {

            //sort list by persistent time, to take last selected dir location permission
            List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
            Collections.sort(permissions, new Comparator<UriPermission>() {
                public int compare(UriPermission o1, UriPermission o2) {
                    return (String.valueOf(o2.getPersistedTime())).compareTo(String.valueOf(o1.getPersistedTime()));
                }
            });
            UriPermission p = permissions.get(0);

            if (p.getUri().toString().contains("ZebraSMS")) {
                this.persistedUri = p.getUri();
            } else if (!CUSTOM_SMS_DIR.equals("0")) {
                this.persistedUri = null; //clear persistent uri
                //decode path
                String fileLocation = p.getUri().toString().split("tree/")[1];
                try {
                    String fileLocationDecoded = new URI(fileLocation).getPath();

                    if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
                        try{
                            if(iLogFormat == 0){ //XML
                                LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                        "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                        "\t\t<message> Folder Permission Not Provided </message>"+
                                        "\t</info>\n" +
                                        "</firmware_update>\n" +
                                        "\t<end>SMS Completed Successfully</end>\n"+
                                        "</firmware>",logFilePath);
                            }else{ //TXT
                                LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " Folder permission not provided "+ "\n",logFilePath);
                                LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        getIntent().putExtra(Constants.IS_HANDLING_INTENT,false);
                    }
                    
                    startActivityForResult((new Intent("android.intent.action.OPEN_DOCUMENT_TREE")).putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", fileLocationDecoded + SMS_DIR)), MY_PERMISSIONS_REQUEST_READ_WRITE_URI);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {

                if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
                    try{
                        if(iLogFormat == 0){ //XML
                            LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                    "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                    "\t\t<message> Folder Permission Not Provided </message>"+
                                    "\t</info>\n" +
                                    "</firmware_update>\n" +
                                    "\t<end>SMS Completed Successfully</end>\n"+
                                    "</firmware>",logFilePath);
                        }else{ //TXT
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " Folder Permission Not Provided "+ "\n",logFilePath);
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    getIntent().putExtra(Constants.IS_HANDLING_INTENT,false);
                }

                startActivityForResult((new Intent("android.intent.action.OPEN_DOCUMENT_TREE")).putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Download/ZebraSMS")), MY_PERMISSIONS_REQUEST_READ_WRITE_URI);
            }
        } else {

            if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
                try{
                    if(iLogFormat == 0){ //XML
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message> Folder Permission Not Provided </message>"+
                                "\t</info>\n" +
                                "</firmware_update>\n" +
                                "\t<end>SMS Completed Successfully</end>\n"+
                                "</firmware>",logFilePath);
                    }else{ //TXT
                        LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " Folder Permission not provided"+ "\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                getIntent().putExtra(Constants.IS_HANDLING_INTENT,false);
            }
            startActivityForResult((new Intent("android.intent.action.OPEN_DOCUMENT_TREE")).putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Download/ZebraSMS")), MY_PERMISSIONS_REQUEST_READ_WRITE_URI);
        }
    }

    /**
     * Method to execute SMS
     * @param view
     */
    public void executeSms(View view) {
        if(persistedUri != null){
            Uri smsPackageUri = getSmsPackageUri(persistedUri);
            if(logFilePath==null){
                LogFile.initiateLogFile(iLogFormat,""+scannerID,modelNumber,serialNumber,DOM,currentFirmware,configName,comMode);
            }
            if (smsPackageUri != null && (smsPackageUri.getPath().endsWith(".smspkg") || smsPackageUri.getPath().endsWith(".SMSPKG"))) {
                // Asynchronously Call DCSSDK_EXECUTE_SMS_PACKAGE API with InXML
                String in_xml = "<inArgs><cmdArgs><arg-string>" + smsPackageUri + "</arg-string></cmdArgs></inArgs>";
                SmsExecuteAsyncTask cmdExecTask = new SmsExecuteAsyncTask(scannerID,
                        DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_EXECUTE_SMS_PACKAGE, null);
                cmdExecTask.execute(new String[]{in_xml});

            }else{
                SmsPackageUpdateManager smsPackageUpdateManager = SmsPackageUpdateManager.getInstance();
                String unzipLocation = this.getCacheDir().getAbsolutePath() + File.separator + SmsPackageUpdateManager.WORK_DIRECTORY;
                if(smsPackageUpdateManager.isAlreadyExtracted(unzipLocation)) {
                    String in_xml = "<inArgs><cmdArgs><arg-string>" + "</arg-string></cmdArgs></inArgs>";
                    SmsExecuteAsyncTask cmdExecTask = new SmsExecuteAsyncTask(scannerID,
                            DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_EXECUTE_SMS_PACKAGE, null);
                    cmdExecTask.execute(new String[]{in_xml});
                }else{
                    //Closing the log when there is no file is available
                    try{
                        if(logFilePath == null){
                            LogFile.initiateLogFile(iLogFormat,""+scannerID,modelNumber,serialNumber,DOM,currentFirmware,configName,comMode);
                        }
                        if(iLogFormat == 0){ //xml
                            LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                    "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                    "\t\t<message> SMS Package File Not Found </message>"+
                                    "\t\t"+getXmlAssetDetails()+
                                    "\t</info>\n</firmware_update>\n</firmware>",logFilePath);
                        }else{ //Log
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Package File Not Found "+getTxtAssetDetails()+ "\n",logFilePath);
                        }
                    }catch (IOException e){
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(ExecuteSmsActivity.this, "SMS Package file not found", Toast.LENGTH_SHORT).show();
                }
            }

        }else{
            Toast.makeText(ExecuteSmsActivity.this, "SMS Package Directory not selected", Toast.LENGTH_SHORT).show();
            requestPersistentUriPermissions();
        }
    }

    /**
     * Method to get the sms package uri
     * @return smspkg uri
     */
    public Uri getSmsPackageUri(Uri persistentUri) {
        Uri smsPackageUri = null;
        DocumentFile[] zebraSmsFiles = DocumentFile.fromTreeUri(this, persistentUri).listFiles();

        for (DocumentFile documentFile : zebraSmsFiles) {
            if (documentFile.getName().toLowerCase().endsWith(".smspkg")) {
                smsPackageUri = documentFile.getUri();
            }
        }
        return smsPackageUri;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            updateScannerID(Application.SCANNER_ID_NONE);
            finish();
            intent = new Intent(ExecuteSmsActivity.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("This will disconnect your current scanner");
            dlg.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    updateScannerID(Application.SCANNER_ID_NONE);
                    finish();
                    Intent intent = new Intent(ExecuteSmsActivity.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {
                    /*
                     * Cancel will do nothing. Other than close dialog
                     */
                }
            });
            dlg.show();
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    //this toast is calling when user press cancel. You can also use this logic on alertdialog's cancel listener too.
                    Toast.makeText(ExecuteSmsActivity.this, "You denied storage permission which is needed to create directory structure..", Toast.LENGTH_SHORT).show();
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //To access files inside the ZebraSMS folder
                    if (createDirectoryStructure(ExecuteSmsActivity.this)  && createLogDirectoryStructure(ExecuteSmsActivity.this)) {
                        requestPersistentUriPermissions();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Method for updating the current scanner ID
     * @param scannerID
     */
    public static void updateScannerID(int scannerID) {
        Application.currentScannerId = scannerID;
    }

    /**
     * Method for requesting run time permissions for creating directories
     * @param context
     */
    private void requestRuntimePermissionToCreateDirs(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if(getIntent().getBooleanExtra(Constants.IS_HANDLING_INTENT,false)){
                try{
                    if(iLogFormat == 0){ //XML
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message> Folder Permission Not Provided </message>"+
                                "\t\t"+getXmlAssetDetails()+
                                "\t</info>\n</firmware_update>\n</firmware>",logFilePath);
                    }else{ //TXT
                        LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " Folder Permission Not Provided "+getTxtAssetDetails()+ "\n",logFilePath);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        } else {
            if (createDirectoryStructure(ExecuteSmsActivity.this) && createLogDirectoryStructure(ExecuteSmsActivity.this)) {
                requestPersistentUriPermissions();
            }

        }
    }

    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {

    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
        switch(firmwareUpdateEvent.getEventType()){
            case SCANNER_UF_SESS_START:
                Log.i(TAG, "SCANNER_UF_SESS_START");
                try{
                    if(iLogFormat == 0){ //Xml
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message>Firmware Update Initialized</message>\n"+
                                getXmlAssetDetails(),logFilePath);
                    }else{ //txt
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Firmware Update Initialized "+getTxtAssetDetails()+"\n",logFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                txtReleaseNoteInfo.setText("Firmware update started...");
                onFWInfoCollected(firmwareUpdateEvent.getPluginPath());
                Application.isFirmwareUpdateInProgress = true;
                onSessionStart();
                updateButton(getResources().getString(R.string.firmware_updating));
                onProgressStart(firmwareUpdateEvent.getMaxRecords());
                Application.isSMSExecutionInProgress = true;
                break;
            case SCANNER_UF_DL_START:
                Log.i(TAG, "SCANNER_UF_DL_START");
                if(!txtReleaseNoteInfo.getText().toString().contains("Firmware downloading...")){
                    txtReleaseNoteInfo.append("\nFirmware downloading...");
                }
                break;
            case SCANNER_UF_DL_PROGRESS:
                Log.i(TAG, "SCANNER_UF_DL_PROGRESS");
                if(firmwareUpdateEvent.getCurrentRecord() != 0) {
                    onProgressUpdate(firmwareUpdateEvent.getCurrentRecord(), firmwareUpdateEvent.getMaxRecords());
                }
                break;
            case SCANNER_UF_DL_END:
                Log.i(TAG, "SCANNER_UF_DL_END");
                if(!txtReleaseNoteInfo.getText().toString().contains("Firmware downloaded...")) {
                    txtReleaseNoteInfo.append("\nFirmware downloaded...");
                }
                break;
            case SCANNER_UF_SESS_END:
                Log.i(TAG, "SCANNER_UF_SESS_END");
                try{
                    if(iLogFormat == 0){ //Xml
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message>Firmware Update Succeeded</message>\n"+
                                getXmlAssetDetails(),logFilePath);
                    }else{ //txt
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Firmware Update Succeeded "+getTxtAssetDetails()+"\n",logFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                txtReleaseNoteInfo.append("\nFirmware updated successfully...");
                onSessionEnd();
                isWaitingForFWUpdateCompletion = true;
                updateButton(getResources().getString(R.string.firmware_updated));
                onRebootStarted();
                Application.isSMSExecutionInProgress = false;
                break;
            case SCANNER_UF_STATUS:
                Log.i(TAG, "SCANNER_UF_STATUS");
                onUpdateFirmwareState(firmwareUpdateEvent.getStatus());
                break;
        }
    }

    @Override
    public void scannerImageEvent(byte[] imageData) {

    }

    @Override
    public void scannerVideoEvent(byte[] videoData) {

    }

    /**
     * start configuration progress bar with max value
     * @param max
     */
    public void onConfigProgressStart(int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogSMSProgress = new Dialog(ExecuteSmsActivity.this);
                dialogSMSProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogSMSProgress.setContentView(R.layout.dialog_sms_progress);
                progressBar = (ProgressBar) dialogSMSProgress.findViewById(R.id.progressBar);
                progressBar.setMax(max);
                Window window = dialogSMSProgress.getWindow();
                if (window != null) {
                    dialogSMSProgress.getWindow().setLayout(getX(), getY());
                }
                dialogSMSProgress.setCancelable(false);
                dialogSMSProgress.setCanceledOnTouchOutside(false);
                if (!isFinishing()) {
                    Log.i(TAG, "Show Progress dialog start");
                    dialogSMSProgress.show();
                    txtPercentage = (TextView) dialogSMSProgress.findViewById(R.id.txt_percentage);
                    txtPercentage.setText("0%");
                }
                if(!txtReleaseNoteInfo.getText().toString().contains("Configuration Update started...")){
                    txtReleaseNoteInfo.append("\nConfiguration Update downloading...");
                }
                updateButton(getResources().getString(R.string.configuration_updating));
            }
        });

    }

    /**
     * updating the configuration progress bar according to the progress
     * @param current
     * @param max
     */
    public void onConfigProgressUpdate(int current, int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setProgress(current);
                    double percentage = (current * 100.0 / max);
                    int iPercentage = (int) percentage;
                    if (txtPercentage != null) {
                        txtPercentage.setText(String.format("%s%%", Integer.toString(iPercentage)));
                    }
                }
                if (dialogSMSProgress != null && !dialogSMSProgress.isShowing() && !isFinishing()) {
                    Log.i(TAG, "Show Progress dialog progress");
                    dialogSMSProgress.show();
                }
                if(!txtReleaseNoteInfo.getText().toString().contains("Configuration Update In Progress...")){
                    txtReleaseNoteInfo.append("\nConfiguration Update In Progress...");
                }
            }
        });
    }

    /**
     * configuration update session end
     * dismissed the progress bar and update the UI details
     */
    public void onConfigSessionEnd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogSMSProgress != null) {
                    if (txtPercentage != null) {
                        txtPercentage.setText("100%");
                    }
                    Log.i(TAG, "Show Progress dialog end");
                    displaySupportedModels();
                    dialogSMSProgress.dismiss();
                    updateButton(getResources().getString(R.string.configuration_updated));
                    Application.isSMSExecutionInProgress = false;
                }
                if(!txtReleaseNoteInfo.getText().toString().contains("Configuration Update Completed...")){
                    txtReleaseNoteInfo.append("\nConfiguration Update Completed...");
                }
            }
        });
    }

    @Override
    public void scannerConfigurationUpdateEvent(ConfigurationUpdateEvent configurationUpdateEvent) {
        switch (configurationUpdateEvent.getEventType()) {
            case SCANNER_UC_SESS_START:
                try{
                    if(iLogFormat == 0){ //Xml
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message>Configuration update Initialized</message>\n"+
                                getXmlAssetDetails()+"\n",logFilePath);
                    }else{ //txt
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Configuration update Initialized "+getTxtAssetDetails()+"\n",logFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Application.isSMSExecutionInProgress = true;
                onConfigProgressStart(configurationUpdateEvent.getMaxRecords());
                break;
            case SCANNER_UC_PROGRESS:
                onConfigProgressUpdate(configurationUpdateEvent.getCurrentRecord(),configurationUpdateEvent.getMaxRecords());

                break;
            case SCANNER_UC_SESS_END:
                try{
                    if(iLogFormat == 0){ //Xml
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message>Configuration update succeeded</message>\n"+
                                getXmlAssetDetails()+"\n</firmware_update>\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile("<end>SMS Completed Successfully</end>\n</firmware>",logFilePath);
                    }else{ //txt
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Configuration update succeeded "+getTxtAssetDetails()+"\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" SMS Completed Successfully\n",logFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                onConfigSessionEnd();
                break;
            case SCANNER_UC_STATUS:
                try{
                    if(iLogFormat == 0){ //Xml
                        LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                "\t\t<message>Configuration Update Failed </message>\n"+
                                getXmlAssetDetails()+"\n</firmware_update>\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile("<end>SMS Completed Successfully</end>\n</firmware>",logFilePath);
                    }else{ //txt
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Configuration Update Failed "+getFlashResponseErrorDescription(configurationUpdateEvent.getStatus())+getTxtAssetDetails()+"\n",logFilePath);
                        LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" SMS Completed Successfully\n",logFilePath);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                persistedUri = null;
                Log.i(TAG,"\nConfiguration Update failed - "+ getFlashResponseErrorDescription(configurationUpdateEvent.getStatus()));
                break;
        }


    }

    /**
     * Display supported models
     */
    public void displaySupportedModels(){
        ConfigHelper configHelper = ConfigHelper.getInstance();
        ArrayList<ScannerConfigModel> models = configHelper.scannerConfig.getSCNCNFGMetaData().getModels();
        StringBuilder releaseNoteInfo = new StringBuilder();
        releaseNoteInfo.append("SUPPORTED MODELS\n");
        for(ScannerConfigModel scannerConfigModel : models){
            releaseNoteInfo.append("\n");
            releaseNoteInfo.append(scannerConfigModel.getModelValue());
        }
        txtReleaseNoteInfo.setText(releaseNoteInfo);
    }

    private int getX() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogSMSReconnectionX * scale + 0.5f);
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }

    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogSMSProgessY * scale + 0.5f);
        return y;
    }

    /**
     * collect firmware file path and display
     * @param info (firmware file path)
     */
    public void onFWInfoCollected(String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout updateFirmwarelayout = (LinearLayout) findViewById(R.id.layout_execute_sms);
                updateFirmwarelayout.setVisibility(View.VISIBLE);
                UpdatePlugInName(info);
            }
        });
    }

    /**
     * start progress bar with max value
     * @param max
     */
    public void onProgressStart(int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar = (ProgressBar) dialogFwProgress.findViewById(R.id.progressBar);
                progressBar.setMax(max);

                Window window = dialogFwProgress.getWindow();
                if (window != null) {
                    dialogFwProgress.getWindow().setLayout(getX(), getY());
                }
                dialogFwProgress.setCancelable(false);
                dialogFwProgress.setCanceledOnTouchOutside(false);
                if (!isFinishing()) {
                    Log.i(TAG, "Show Progress dialog");
                    dialogFwProgress.show();
                    txtPercentage = (TextView) dialogFwProgress.findViewById(R.id.txt_percentage);
                    txtPercentage.setText("0%");
                }
            }
        });

    }

    /**
     * updating the progress bar according to the progress
     * @param current
     * @param max
     */
    public void onProgressUpdate(int current, int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setProgress(current);
                    double percentage = (current * 100.0 / max);
                    int iPercentage = (int) percentage;
                    if (txtPercentage != null) {
                        txtPercentage.setText(String.format("%s%%", Integer.toString(iPercentage)));
                    }
                }
                if (dialogFwProgress != null && !dialogFwProgress.isShowing() && !isFinishing()) {
                    Log.i(TAG, "Show Progress dialog");
                    dialogFwProgress.show();
                }
            }
        });
    }

    /**
     * firmware update session end
     * dismissed the progress bar and update the UI details
     */
    public void onSessionEnd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogFwProgress != null) {
                    if (txtPercentage != null) {
                        txtPercentage.setText("100%");
                    }
                    dialogFwProgress.dismiss();
                    dialogFwProgress = null;
                }
            }
        });
    }

    /**
     * update screen on firmware update error response
     * @param dcssdk_result
     */
    public void onUpdateFirmwareState(DCSSDKDefs.DCSSDK_RESULT dcssdk_result) {
        turnOffLEDPattern();
        if (!isFinishing() && dialogFwProgress != null) {
            dialogFwProgress.dismiss();
            dialogFwProgress = null;
        }
        if (dcssdk_result != DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FIRMWARE_UPDATE_ABORTED) {
            if (dcssdk_result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateStatus(View.VISIBLE, null);
                    }
                });
            } else {
                final String failureReason  = getFlashResponseErrorDescription(dcssdk_result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateStatus(View.VISIBLE, failureReason);
                    }
                });
            }

        }
    }

    /**
     * Retrieves the flash response error description for the given status code
     *
     * @param statusCode status code for the flash response error
     * @return Corresponding flash response error description
     */
    private String getFlashResponseErrorDescription(DCSSDKDefs.DCSSDK_RESULT statusCode) {
        String errorDescription = "";
        switch (statusCode) {
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_LOW_BATTERY_LEVEL:
                errorDescription = this.getResources().getString(R.string.update_failed_low_battery_level);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_COMMANDS_ARE_OUT_OF_SYNC:
                errorDescription = this.getResources().getString(R.string.update_failed_commands_are_out_of_sync);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_HAS_OVERLAPPING_ADDRESS:
                errorDescription = this.getResources().getString(R.string.update_failed_has_overlapping_address);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_LOAD_COUNT_ERROR:
                errorDescription = this.getResources().getString(R.string.update_failed_load_count_error);
                break;
            case DCSSDK_RESULT_INVALID_CONFIG_FILE:
                errorDescription = this.getResources().getString(R.string.config_failed_invalid_config_file);
                break;
            case DCSSDK_RESULT_INCOMPATIBLE_CONFIG_FILE:
                errorDescription = this.getResources().getString(R.string.config_failed_incompatible_config_file);
                break;
        }
        return errorDescription;
    }

    /**
     * update UI with firmware update error reason
     * @param visibility
     * @param reason
     */
    private void UpdateStatus(int visibility, String reason) {
        TableRow tblRowFwStatus = (TableRow) findViewById(R.id.tbl_row_sms_status);
        TableRow tblRowFwFailureReason = (TableRow) findViewById(R.id.tbl_row_sms_status_reason);

        tblRowFwStatus.setVisibility(visibility);

        if (null == reason) {
            tblRowFwFailureReason.setVisibility(View.GONE);
        } else {
            tblRowFwFailureReason.setVisibility(View.VISIBLE);
            TextView textFwFailureReason = (TextView) findViewById(R.id.txt_sms_status_reason);
            textFwFailureReason.setText(reason);
        }

    }

    /**
     * led turn on, on finish firmware update
     */
    private void turnOnLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                85 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
    }

    /**
     * led turn off, on start firmware update
     */
    private void turnOffLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                90 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
    }


    /**
     * on firmware update session start create progress bar and progress UI objects
     */
    public void onSessionStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogFwProgress = new Dialog(ExecuteSmsActivity.this);
                dialogFwProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogFwProgress.setContentView(R.layout.dialog_fw_progress);
            }
        });
    }

    /**
     * display the firmware file name
     * @param plugInName
     */
    private void UpdatePlugInName(String plugInName) {
        File file = new File(plugInName);
        String fileName = file.getName();
        TextView textView = (TextView) findViewById(R.id.txt_sms_package_name);
        if(plugInName.contains("%")) {
            textView.setText(Uri.parse(plugInName).getPath()+"");
        } else {
            textView.setText(fileName);
        }
    }

    /**
     * Method to open the reboot dialog and change the status
     */
    public void onRebootStarted() {

        try{
            if(iLogFormat == 0){ //Xml
                LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                        "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                        "\t\t<message>Scanner Reboot Success</message>\n"+
                        getXmlAssetDetails()+"\n",logFilePath);
            }else{ //txt
                LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" Scanner Reboot Success "+getTxtAssetDetails()+"\n",logFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        isWaitingForFWUpdateCompletion = true;
        getIntent().putExtra(Constants.FW_REBOOT, true);

        showRebooting();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                disconnect(fwUpdatingScanner.getScannerID());
                Application.barcodeData.clear();
                Application.currentScannerId = Application.SCANNER_ID_NONE;
                Application.isSMSExecutionInProgress = true;
            }
        }, 2000);
    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        if (fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
            Application.sdkHandler.dcssdkEstablishCommunicationSession(fwUpdatingScanner.getScannerID());
        } else if (fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_CDC) {
            Application.sdkHandler.dcssdkEstablishCommunicationSession(fwUpdatingScanner.getScannerID());
        }
        return true;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        if (isWaitingForFWUpdateCompletion && dialogFwRebooting != null) {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        if (isWaitingForFWUpdateCompletion && dialogFwReconnectScanner != null) {
            dialogFwReconnectScanner.dismiss();
            dialogFwReconnectScanner = null;
        }
        Application.isScannerConnectedAfterSMS = true;
        Application.isSMSExecutionInProgress = false;
        Application.currentConnectedScannerID = scannerID;
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        if (isWaitingForFWUpdateCompletion) {
            pnpHandler.sendMessageDelayed(pnpHandler.obtainMessage(1), 180000);
        } else {
            Application.barcodeData.clear();
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if(Application.getAnyScannerConnectedStatus()){
            Intent intent = new Intent(ExecuteSmsActivity.this, ActiveScannerActivity.class);
            intent.putExtra(Constants.SCANNER_NAME, Application.currentConnectedScanner.getScannerName());
            intent.putExtra(Constants.SCANNER_ADDRESS, Application.currentConnectedScanner.getScannerHWSerialNumber());
            intent.putExtra(Constants.SCANNER_ID, Application.currentConnectedScanner.getScannerID());
            intent.putExtra(Constants.SCANNER_TYPE, Application.currentScannerType);
            intent.putExtra(Constants.AUTO_RECONNECTION, Application.currentConnectedScanner.isAutoCommunicationSessionReestablishment());
            intent.putExtra(Constants.CONNECTED, true);
            startActivity(intent);
        }else {
            super.onBackPressed();
        }
    }

    private class SmsExecuteAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public SmsExecuteAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            turnOnLEDPattern();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }

        @Override
        protected void onPostExecute(Boolean execCommandStatus) {
            super.onPostExecute(execCommandStatus);
            String executionStatusMessage = "";
            if (!execCommandStatus) {
                executionStatusMessage = "SMS Package Execution Failed";
                Toast.makeText(ExecuteSmsActivity.this, "SMS package execution failed..!", Toast.LENGTH_SHORT).show();
            } else {
                executionStatusMessage = "SMS Package Execution Successful";
            }
            try{
                if(iLogFormat == 0){ //Xml
                    LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                            "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                            "\t\t<message>"+executionStatusMessage+"</message>\n"+
                            getXmlAssetDetails()+"\n",logFilePath);
                }else{ //txt
                    LogFile.writeTxtOrXmlContentToFile(sdf.format(new Date())+" "+executionStatusMessage+" "+getTxtAssetDetails()+"\n",logFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            turnOffLEDPattern();
        }
    }

    /**
     * Method to update the SMS execute button
     */
    public void updateButton(String updateBtnText) {
        runOnUiThread(() -> {
            TableRow tblRowFWSuccess = (TableRow) findViewById(R.id.tbl_row_sms_execution_success);
            TextView txtSuccess = (TextView) findViewById(R.id.txt_sms_success);
            if (tblRowFWSuccess != null) {
                tblRowFWSuccess.setVisibility(View.VISIBLE);
                txtSuccess.setText(updateBtnText);
            }
            TableRow tblRowFW = (TableRow) findViewById(R.id.tbl_row_execute_sms);
            if (tblRowFW != null) {
                tblRowFW.setVisibility(View.INVISIBLE);
                tblRowFW.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Method to show reboot status dialog
     */
    private void showRebooting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Show Rebooting dialog");
                if (!isFinishing()) {
                    dialogFwRebooting = new Dialog(ExecuteSmsActivity.this);
                    dialogFwRebooting.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogFwRebooting.setContentView(R.layout.dialog_fw_rebooting);
                    TextView cancelButton = (TextView) dialogFwRebooting.findViewById(R.id.btn_cancel);
                    // if decline button is clicked, close the custom dialog
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Close dialog
                            dialogFwRebooting.dismiss();
                            dialogFwRebooting = null;
                            finish();
                        }
                    });

                    dotProgressBar = (DotsProgressBar) dialogFwRebooting.findViewById(R.id.progressBar);
                    dotProgressBar.setDotsCount(6);

                    Window window = dialogFwRebooting.getWindow();
                    if (window != null) {
                        dialogFwRebooting.getWindow().setLayout(getX(), getY());
                    }
                    dialogFwRebooting.setCancelable(false);
                    dialogFwRebooting.setCanceledOnTouchOutside(false);
                    Log.i(TAG, "Showing dot progress dialog");
                    dialogFwRebooting.show();
                } else {
                    finish();
                }
            }
        });
    }

    /**
     * Method to display the reconnect scanner dialog with STC pairing barcode when autoconnection fails
     */
    private void reconnectScanner() {
        getIntent().putExtra(Constants.FW_REBOOT, false);
        setWaitingForFWRebootInSMS(true);

        if (!isFinishing()) {
            dialogFwReconnectScanner = new Dialog(ExecuteSmsActivity.this);
            dialogFwReconnectScanner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFwReconnectScanner.setContentView(R.layout.dialog_fw_reconnect_scanner);
            TextView cancelButton = (TextView) dialogFwReconnectScanner.findViewById(R.id.btn_cancel);
            // if decline button is clicked, close the custom dialog
            cancelButton.setOnClickListener(v -> {
                // Close dialog
                dialogFwReconnectScanner.dismiss();
                dialogFwReconnectScanner = null;
                finish();
            });
            frameLayoutBarcode = (FrameLayout) dialogFwReconnectScanner.findViewById(R.id.scan_to_connect_barcode);
            barCodeView = null;

            if (fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
                barCodeView = Application.sdkHandler.dcssdkGetUSBSNAPIWithImagingBarcode();
            } else {
                barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig);
                if (barCodeView == null) {
                    barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig, HomeActivity.btAddress);
                }
            }
            if (barCodeView != null && frameLayoutBarcode != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 7 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                frameLayoutBarcode.addView(barCodeView, layoutParams);
            }
            dialogFwReconnectScanner.setCancelable(false);
            dialogFwReconnectScanner.setCanceledOnTouchOutside(false);
            dialogFwReconnectScanner.show();

            Window window = dialogFwReconnectScanner.getWindow();
            if (window != null) {
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, getY());
            }
        } else {
            finish();
        }
    }

    /**
     * To get Asset Information from the connected scanner
     */
    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
            this.scannerId = scannerId;
            this.opcode = opcode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ExecuteSmsActivity.this, "Execute Command...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                if (result) {
                    try {
                        Log.i(TAG, sb.toString());
                        int i = 0;
                        int attrId = -1;
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
                                    Log.i(TAG, "Name of the end tag: " + name);
                                    if (text != null) {
                                        if (name.equals("id")) {
                                            attrId = Integer.parseInt(text.trim());
                                            Log.i(TAG, "ID tag found: ID: " + attrId);
                                        } else if (name.equals("value")) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);
                                            if (RMD_ATTR_MODEL_NUMBER == attrId) {
                                                modelNumber = attrVal;
                                            } else if (RMD_ATTR_SERIAL_NUMBER == attrId) {
                                                serialNumber = attrVal;
                                            } else if (RMD_ATTR_FW_VERSION == attrId) {
                                                currentFirmware = attrVal;
                                            } else if (RMD_ATTR_DOM == attrId) {
                                                DOM = attrVal;
                                            } else if (RMD_ATTR_CONFIG_NAME == attrId) {
                                                configName = attrVal;
                                            }
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public String getTxtAssetDetails(){
        return "ID:"+scannerID+"; " +
                "Model:"+modelNumber+"; " +
                "SN"+serialNumber+"; " +
                "DoM:"+DOM+"; " +
                "Firmware:"+currentFirmware+"; " +
                "Config name:"+configName+"; "+
                "Protocol: "+comMode;
    }

    public String getXmlAssetDetails(){
        return "\t\t<id>"+scannerID+"</id>\n"+
                "\t\t<sn>"+serialNumber+"</sn>\n"+
                "\t\t<model>"+modelNumber+"</model>\n"+
                "\t\t<dom>"+DOM+"</dom>\n"+
                "\t\t<firmware>"+currentFirmware+"</firmware>\n"+
                "\t\t<config_name>"+configName+"</config_name>\n"+
                "\t\t<protocol>"+comMode+"</protocol>\n"+
                "\t</info>\n";
    }



}