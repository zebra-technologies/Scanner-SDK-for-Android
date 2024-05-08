package com.zebra.scannercontrol.app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;


import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent;
import com.zebra.commoniolib.usbiomgr;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.DotsProgressBar;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_CONFIG_NAME;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_DOM;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_FW_VERSION;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_MODEL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_SERIAL_NUMBER;
import static com.zebra.scannercontrol.app.application.Application.firmwareFile;
import static com.zebra.scannercontrol.app.application.Application.selectedFirmware;
import static java.security.AccessController.getContext;

public class UpdateFirmware extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate, ScannerAppEngine.IScannerAppEngineDevEventsDelegate {
    public static final int INVALID_SCANNER_ID = -1;
    static final int FILE_SELECT_REQUEST_READ_EX_STORAGE = 10;
    static final String INTENT_CATEGORY_DEFAULT = "android.intent.category.DEFAULT";
    static final String PACKAGE_NAME_FORMATTER = "package:%s";
    static MyAsyncTask cmdExecTask = null;
    static File selectedPlugIn = null;
    static DCSScannerInfo fwUpdatingScanner;
    static boolean fwReboot;
    static Dialog dialogFwProgress;
    static Dialog dialogFwRebooting;
    static Dialog dialogFwReconnectScanner;
    static boolean processMultiplePlugIn = false;
    static Dialog dialogFWHelp;
    static boolean isWaitingForFWUpdateToComplete;
    final int communicationModeBTLE = 4;
    Menu menu;
    MenuItem pairNewScannerMenu;
    int scannerID;
    int dialogFWProgessX = 90;
    int dialogFWProgessY = 220;
    int dialogFWReconnectionY = 250;
    int dialogFWReconnectionX = 50;
    int communicationMode = -1;
    ProgressBar progressBar;
    DotsProgressBar dotProgressBar;
    TextView txtPercentage;
    BarCodeView barCodeView = null;
    FrameLayout llBarcode = null;
    TableRow selectFirmwareRow,tblRowFW;
    String readWritePermission ="rw";
    int fwMax;
    AlertDialog.Builder alertDialogForValidateFirmwareUpdate;
    Uri documentUri;
    String documentUriString;
    private NavigationView navigationView;
    private usbiomgr usbCommonIOManager;
   // static final int PERMISSIONS_REQUEST_MANAGE_ALL_FILES_ACCESS = 2296;
    private String currentScannerFirmwareVersion;
    private String currentScannerModelNumber;
   
    private Handler pnpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (dialogFwRebooting != null) {
                    dialogFwRebooting.dismiss();
                    dialogFwRebooting = null;
                    ShowReconnectScanner();
                }
            }
        }
    };

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_firmware);
        alertDialogForValidateFirmwareUpdate = new AlertDialog.Builder(this);

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
        Toolbar subActionBar = (Toolbar) findViewById(R.id.sub_actionbar);
        setSupportActionBar(subActionBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_update_firmware);
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
        isWaitingForFWUpdateToComplete = false;
        fwUpdatingScanner = Application.currentConnectedScanner;
         tblRowFW = (TableRow) findViewById(R.id.tbl_row_fw_update);
        Log.i("ScannerControl", "Adding Update FW IScannerAppEngineDevEventsDelegate into list");
        addDevConnectionsDelegate(this);
        addDevEventsDelegate(this);
        communicationMode = getIntent().getIntExtra(Constants.SCANNER_TYPE, -1);
        ShowBLEModeDialog(communicationMode);
        fwReboot = getIntent().getBooleanExtra(Constants.FW_REBOOT, false);
        getScannerFirmwareVersion();
        selectFirmwareRow =findViewById(R.id.tbl_row_select_firmware);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isWaitingForFWUpdateToComplete) {
            Log.i("ScannerControl", "onPause Removing Update FW IScannerAppEngineDevEventsDelegate from list");
            removeDevEventsDelegate(this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("ScannerControl", "onDestroy Removing Update FW IScannerAppEngineDevEventsDelegate from list");
        removeDevConnectiosDelegate(this);
        removeDevEventsDelegate(this);
        Application.isFirmwareUpdateInProgress = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogFWHelp != null && dialogFWHelp.isShowing()) {
            dialogFWHelp.dismiss();
            dialogFWHelp = null;
        }
        fwReboot = getIntent().getBooleanExtra(Constants.FW_REBOOT, false);



        if (!fwReboot && isWaitingForFWUpdateToComplete) {
            Log.i("ScannerControl", "Waiting for fw update to be completed");
        }  else if (fwReboot) {
            Application.isFirmwareUpdateInProgress = false;
            if(selectedFirmware != null){
                documentUri =selectedFirmware;
                UIUpdater uiUpdater = new UIUpdater(fwReboot);
                uiUpdater.execute();
            }
        }
        else {
            if (dialogFwProgress == null && dialogFwRebooting == null ) {
                if (Application.currentConnectedScanner != null) {

                    boolean fileAccess =false;
                    ParcelFileDescriptor parcelFileDescriptor = null;
                        try {
                            parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(selectedFirmware, readWritePermission);
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            InputStream inputStream = new FileInputStream(fileDescriptor);
                            inputStream.close();
                            fileAccess =true;
                        } catch (Exception e) {
                            fileAccess =false;
                        }finally {
                            closeSafely(parcelFileDescriptor);
                        }

                    if (fileAccess) {
                        selectFirmwareRow.setVisibility(View.INVISIBLE);
                        selectFirmwareRow.setVisibility(View.GONE);
                        UIUpdater uiUpdater = new UIUpdater(fwReboot);
                        uiUpdater.execute();
                        if (tblRowFW != null) {
                            tblRowFW.setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (tblRowFW != null) {
                            tblRowFW.setVisibility(View.INVISIBLE);
                            tblRowFW.setVisibility(View.GONE);
                        }

                    }

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_REQUEST_READ_EX_STORAGE) {

            if (data != null) {

               Application.firmwareFile = new File(String.valueOf(data.getData()));
                 documentUri =data.getData();
                if(documentUri.toString().contains("content://com.android.providers")){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout updateFirmwarelayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                            updateFirmwarelayout.setVisibility(View.INVISIBLE);
                            updateFirmwarelayout.setVisibility(View.GONE);
                            ShowPlugInPathChangeDialog();
                        }
                    });

                } else{
                    documentUriString = documentUri.toString();
                    selectedFirmware=documentUri;
                    UIUpdater uiUpdater = new UIUpdater(fwReboot);
                    uiUpdater.execute();
                }

            }
        } else {

            if (tblRowFW != null) {
                tblRowFW.setVisibility(View.INVISIBLE);
                tblRowFW.setVisibility(View.GONE);
            }
                }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.firmware, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.firmware_help: {
                if (!isFinishing()) {
                    dialogFWHelp = new Dialog(UpdateFirmware.this);
                    dialogFWHelp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogFWHelp.setContentView(R.layout.dialog_firmware_help);
                    dialogFWHelp.setCancelable(false);
                    dialogFWHelp.setCanceledOnTouchOutside(false);
                    dialogFWHelp.show();
                    TextView textView = (TextView) dialogFWHelp.findViewById(R.id.url);
                    if (textView != null) {
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                    TextView declineButton = (TextView) dialogFWHelp.findViewById(R.id.btn_ok);
                    // if decline button is clicked, close the custom dialog
                    declineButton.setOnClickListener(v -> {
                        // Close dialog
                        dialogFWHelp.dismiss();
                        dialogFWHelp = null;
                    });
                }
                return true;
            }
            case android.R.id.home:{
                super.onBackPressed();
                selectedFirmware =null;
                firmwareFile =null;
                return true;
        }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method is used to check file existence with given Uri
     * @param firmwarePath Uri to be checked
     * @return true if file exist
     */
    private boolean isFileExists(Uri firmwarePath) {


        if (null != firmwarePath) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(firmwarePath, readWritePermission);
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                InputStream inputStream = new FileInputStream(fileDescriptor);
                inputStream.close();
                return true;
            } catch (Exception e) {
                return false;
            }finally {
                closeSafely(parcelFileDescriptor);
            }
        }else {
            return false;
        }
    }
    private boolean ShowFirmwareInfo(boolean bFwReboot) {
        List<ScannerPlugIn> matchingPlugins = new ArrayList<ScannerPlugIn>();
        File fileDownloadDir =Application.firmwareFile;
        if(selectedFirmware.getPath().endsWith(".scnplg") || selectedFirmware.getPath().endsWith(".SCNPLG")){
            String scannerModel = getScannerModelNumber(scannerID); // 533

            String unzipLocation = this.getCacheDir().getAbsolutePath() + File.separator;
            String metaDataFilePath = extractMetaData(unzipLocation, selectedFirmware);
            List<String> metaSupportedModels = getSupportedModels(metaDataFilePath);

            if (metaSupportedModels.contains(scannerModel)) {
                final String plugInRev = getPlugInRev(metaDataFilePath);
                ScannerPlugIn matchingPlugin = new ScannerPlugIn(fileDownloadDir, metaSupportedModels, plugInRev);
                matchingPlugins.add(matchingPlugin);
            }


            if (matchingPlugins.size() == 0) {
                // Plug-in model mis match
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout updateFirmwarelayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                        updateFirmwarelayout.setVisibility(View.INVISIBLE);
                        updateFirmwarelayout.setVisibility(View.GONE);
                        ShowPlugInScannerMismatchDialog();
                    }
                });
            } else if (matchingPlugins.size() == 1) {
                // Only one matching plug-in available
                ProcessPlugIn(matchingPlugins.get(0).getPath(), bFwReboot);
            } else {
                // Plug-in model mis match
                runOnUiThread(() -> {
                    LinearLayout updateFirmwareLayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                    updateFirmwareLayout.setVisibility(View.INVISIBLE);
                    updateFirmwareLayout.setVisibility(View.GONE);
                    ShowPlugInScannerMismatchDialog();
                });
            }
        }

        else if(selectedFirmware.getPath().endsWith(".dat") || selectedFirmware.getPath().endsWith(".DAT") ||
        selectedFirmware.getPath().endsWith(".fcdat") || selectedFirmware.getPath().endsWith(".FCDAT")) {
            String unzipLocation = this.getCacheDir().getAbsolutePath() + File.separator;
            String DatFilePath = copyDatFile(unzipLocation, selectedFirmware);
            ProcessPlugIn(fileDownloadDir, bFwReboot);
        }else{
            // No DAT or Plug-in files in Download folder
            runOnUiThread(() -> {
                LinearLayout updateFirmwareLayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                updateFirmwareLayout.setVisibility(View.INVISIBLE);
                updateFirmwareLayout.setVisibility(View.GONE);
                ShowNoPlugInFoundDialog();
            });
        }

        return true;
    }

    private void ShowMultipleFWFilesFoundDialog(boolean isPlugIn) {
        if (!isFinishing()) {
            final Dialog dialog = new Dialog(UpdateFirmware.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_multiple_dat_files);
            if (isPlugIn) {
                TextView msg = (TextView) dialog.findViewById(R.id.txt_msg);
                msg.setText(R.string.multiple_plug_in_files_msg_1);
                TextView title = (TextView) dialog.findViewById(R.id.txt_title);
                title.setText(R.string.multiple_plugin_files);
            }
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(v -> {
                // Close dialog
                dialog.dismiss();
            });
        }
    }

    /**
     * Display dialog for ble mode suggesting that firmware update will be much faster with bluetooth classic
     *
     * @param communicationMode Communication mode of the scanner
     */
    private void ShowBLEModeDialog(int communicationMode) {
        if (!isFinishing() && communicationMode == communicationModeBTLE && !fwReboot) {

            final Dialog firmwareUpdateClassicModeDialog = new Dialog(UpdateFirmware.this);
            firmwareUpdateClassicModeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            firmwareUpdateClassicModeDialog.setContentView(R.layout.dialog_ble_com_mode_detected);

            TextView firmwareUpdateClassicModeMessage = (TextView) firmwareUpdateClassicModeDialog.findViewById(R.id.txt_message_ble_mode);
            firmwareUpdateClassicModeMessage.setText(R.string.ble_communication_mode_detected);
            firmwareUpdateClassicModeDialog.setCancelable(false);
            firmwareUpdateClassicModeDialog.setCanceledOnTouchOutside(false);
            firmwareUpdateClassicModeDialog.show();
            TextView declineButton = (TextView) firmwareUpdateClassicModeDialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(v -> {
                // Close dialog
                firmwareUpdateClassicModeDialog.dismiss();
            });

        }
    }

    private ScannerPlugIn getLatestPlugIn(List<ScannerPlugIn> matchingPlugins) {
        ScannerPlugIn latestPlugIn = matchingPlugins.get(0);

        for (ScannerPlugIn plugIn : matchingPlugins) {
            if (Integer.valueOf(plugIn.getRevision()) > Integer.valueOf(latestPlugIn.getRevision())) {
                latestPlugIn = plugIn;
            }
        }
        return latestPlugIn;
    }

    private void ProcessPlugIn(File plugInFile, final boolean bFwReboot) {
        try {
            if (isPlugIn(plugInFile)) {
                // This is a plug-in file
                String unzipLocation = this.getCacheDir().getAbsolutePath() + File.separator;
                String metaDataFilePath = extractMetaData(unzipLocation, Application.selectedFirmware);
                String scannerModel = getScannerModelNumber(scannerID); // 533
                Uri metaDataFileUri = Uri.parse(metaDataFilePath);
                List<String> metaSupportedModels = getSupportedModels(metaDataFilePath);
                if (metaSupportedModels.contains(scannerModel)) {
                    // Matching model found in meta.xml
                    runOnUiThread(() -> {
                        LinearLayout updateFirmwareLayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                        updateFirmwareLayout.setVisibility(View.VISIBLE);
                    });
                    final String scannerFirmwareVersion = getScannerFirmwareVersion(scannerID); // 20012
                    final String plugInName = getPlugInName(metaDataFilePath);
                    final String plugInRev = getPlugInRev(metaDataFilePath);
                    final String plugInDate = getPlugInDate(metaDataFilePath);
                    final String matchingPlugInFwName = getMatchingFWName(getPlugInFwNames(metaDataFilePath), scannerFirmwareVersion);
                    final String pngFilePath = extractPng(unzipLocation, Application.selectedFirmware);
                    final String releaseNotePath = extractReleaseNote(unzipLocation, Application.selectedFirmware);
                   // selectedPlugIn = plugInFile;
                        selectedPlugIn = new File(Application.selectedFirmware.getPath());
                        runOnUiThread(() -> {
                            UpdateImage(pngFilePath);
                            UpdateReleaseNote(releaseNotePath);
                            UpdatePlugInName(plugInName);
                            selectFirmwareRow.setVisibility(View.INVISIBLE);
                            selectFirmwareRow.setVisibility(View.GONE);
                            if (bFwReboot) {
                                TurnOffLEDPattern();
                                isWaitingForFWUpdateToComplete = false;
                                if (matchingPlugInFwName.equals(scannerFirmwareVersion)) { // Update success
                                    UpdateScannerFirmwareVersion(scannerFirmwareVersion, true);
                                    UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, true);
                                    UpdateButton();
                                    selectedFirmware = null;
                                    Application.firmwareFile = null;
                                } else { // Update failed
                                    UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                                    UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, false);
                                    UpdateStatus(View.VISIBLE, null);
                                }
                            } else {
                                UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                                UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, false);
                            }
                        });
                } else {
                    // Plug-in model mis match
                    runOnUiThread(() -> {
                        LinearLayout updateFirmwareLayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                        updateFirmwareLayout.setVisibility(View.INVISIBLE);
                        updateFirmwareLayout.setVisibility(View.GONE);
                        ShowPlugInScannerMismatchDialog();
                    });
                }
            } else {
                // This is a DAT file
                runOnUiThread(() -> {
                    LinearLayout updateFirmwareLayout = (LinearLayout) findViewById(R.id.layout_update_firmware);
                    updateFirmwareLayout.setVisibility(View.VISIBLE);
                });
                final String scannerFirmwareVersion = getScannerFirmwareVersion(scannerID); // 20012
                final String plugInName = plugInFile.getName();
               // selectedPlugIn = plugInFile;
                runOnUiThread(() -> {
                    UpdatePlugInName(plugInName);

                    if (bFwReboot) {
                        TurnOffLEDPattern();
                        isWaitingForFWUpdateToComplete = false;
                        UpdateScannerFirmwareVersion(scannerFirmwareVersion, true);
                        UpdateToFirmwareVersion(plugInName, true);
                        UpdateButton();

                    } else {
                        UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                        UpdateToFirmwareVersion(plugInName, false);
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        selectedFirmware =null;
        firmwareFile =null;
    }

    private boolean isPlugIn(File plugInFile) {
        if (plugInFile.getAbsolutePath().endsWith(".SCNPLG") || plugInFile.getAbsolutePath().endsWith(".scnplg")) {
            return true;
        }
        return false;
    }

    private String getMatchingFWName(ArrayList<String> plugInFwNames, String scannerFirmwareVersion) {
        String matchingFWName = "";
        for (String plugInFwName : plugInFwNames) {
            if (scannerFirmwareVersion.equals(plugInFwName)) {
                matchingFWName = plugInFwName;
                break;
            }
        }

        if (matchingFWName.equals("")) {
            for (String plugInFwName : plugInFwNames) {
                if (plugInFwName.length() > 3 && scannerFirmwareVersion.startsWith(plugInFwName.substring(0, 3))) {
                    matchingFWName = plugInFwName;
                    break;
                }
            }
        }

        return matchingFWName;
    }

    private void UpdateStatus(int visibility, String reason) {
        TableRow tblRowFwStatus = (TableRow) findViewById(R.id.tbl_row_fw_status);
        TableRow tblRowFwFailureReason = (TableRow) findViewById(R.id.tbl_row_fw_status_reason);

        tblRowFwStatus.setVisibility(visibility);

        if (null == reason) {
            tblRowFwFailureReason.setVisibility(View.GONE);
        } else {
            tblRowFwFailureReason.setVisibility(View.VISIBLE);
            TextView textFwFailureReason = (TextView) findViewById(R.id.txt_firmware_status_reason);
            textFwFailureReason.setText(reason);
        }

    }

    private void UpdateButton() {
        TableRow tblRowFWSuccess = (TableRow) findViewById(R.id.tbl_row_fw_update_success);
        if (tblRowFWSuccess != null) {
            tblRowFWSuccess.setVisibility(View.VISIBLE);
        }
        TableRow tblRowFW = (TableRow) findViewById(R.id.tbl_row_fw_update);
        if (tblRowFW != null) {
            tblRowFW.setVisibility(View.INVISIBLE);
            tblRowFW.setVisibility(View.GONE);

        }
        selectFirmwareRow.setVisibility(View.INVISIBLE);
        selectFirmwareRow.setVisibility(View.GONE);
    }

    private void UpdateToFirmwareVersion(String plugInRev, String plugInDate, String plugInCombinedFwName, boolean afterFWUpdate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(plugInDate));// all done
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String toString;
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        String date = String.valueOf(cal.get(Calendar.DATE));
        if (date.length() == 1) {
            date = "0" + date;
        }
        if (afterFWUpdate) {
            toString = "Current: Release " + plugInRev + " - " + cal.get(Calendar.YEAR) + "." + month + "." + date + " (" + plugInCombinedFwName + ")";
        } else {
            toString = "To: Release " + plugInRev + " - " + cal.get(Calendar.YEAR) + "." + month + "." + date + " (" + plugInCombinedFwName + ")";
        }
        TextView textView = (TextView) findViewById(R.id.txt_to_fw_version);
        textView.setText(toString);
    }

    private void UpdateToFirmwareVersion(String datFileName, boolean afterFWUpdate) {

        String toString;

        String filePath = Uri.parse(datFileName).getPath();
        if(datFileName.contains("%") && filePath != null) {
            datFileName = filePath+"";
        }

        if (afterFWUpdate) {
            toString = "Current: " + datFileName;
        } else {
            toString = "To: " + datFileName;
        }
        TextView textView = (TextView) findViewById(R.id.txt_to_fw_version);
        textView.setText(toString);
    }

    private void UpdateScannerFirmwareVersion(String scannerFirmwareVersion, boolean afterFWUdate) {
        TextView textView = (TextView) findViewById(R.id.txt_from_fw_version);
        if (afterFWUdate) {
            textView.setText("");
        } else {
            textView.setText("From: " + scannerFirmwareVersion);
        }
    }

    private void UpdatePlugInName(String plugInName) {
        TextView textView = (TextView) findViewById(R.id.txt_fw_plugin_name);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setSelected(true);
        textView.setSingleLine(true);
        if(plugInName.contains("%")) {
            textView.setText(Uri.parse(plugInName).getPath()+"");
        } else {
            textView.setText(plugInName);
        }
    }

    private void UpdateImage(String pngFilePath) {
        ImageView scanner = (ImageView) findViewById(R.id.image_scanner);
        scanner.setImageBitmap(BitmapFactory.decodeFile(pngFilePath));
    }

    private void UpdateReleaseNote(String releaseNotePath) {
        BufferedReader reader = null;
        StringBuilder text = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(releaseNotePath)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        TextView releaseNote = (TextView) findViewById(R.id.txt_fw_release_notes);
        releaseNote.setText((CharSequence) text);
    }

    private String extractPng(String extractPath, Uri zipPath) {
        InputStream is;
        ZipInputStream zis = null;
        String unzipFile = "";

        try(ParcelFileDescriptor parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(zipPath,readWritePermission)){
            String filename;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            is = new FileInputStream(fileDescriptor);

            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (filename.endsWith(".png") || filename.endsWith(".PNG")) {
                    unzipFile = extractPath + filename;

                    File targetDirectory = new File(extractPath);
                    File file = new File(unzipFile);
                    String canonicalDestinationPath = file.getCanonicalPath();

                    if (!canonicalDestinationPath.startsWith(targetDirectory.getCanonicalPath())) {
                        throw new IOException("Entry is outside of the target directory");
                    }

                    try (FileOutputStream fout = new FileOutputStream(file) ) {

                        // cteni zipu a zapis
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    }
                }
                zis.closeEntry();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeSafely(zis);
        }
        return unzipFile;
    }

    private String extractReleaseNote(String extractPath, Uri zipPath) {
        InputStream is;
        ZipInputStream zis = null;
        String unzipFile = "";

        try(ParcelFileDescriptor parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(zipPath,readWritePermission)){
            String filename;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            is = new FileInputStream(fileDescriptor);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (filename.endsWith(".txt") || filename.endsWith(".TXT")) {
                    unzipFile = extractPath + filename;

                    File targetDirectory = new File(extractPath);
                    File file = new File(unzipFile);
                    String canonicalDestinationPath = file.getCanonicalPath();

                    if (!canonicalDestinationPath.startsWith(targetDirectory.getCanonicalPath())) {
                        throw new IOException("Entry is outside of the target directory");
                    }

                    try (FileOutputStream fout = new FileOutputStream(file)) {
                        // cteni zipu a zapis
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeSafely(zis);
        }
        return unzipFile;
    }

    private ArrayList<String> getPlugInFwNames(String metaDataFilePath) {
        ArrayList<String> plugInFWVersion = new ArrayList<String>();
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

            int event = parser.getEventType();
            String text = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("combined-firmware")) {
                            plugInFWVersion.add(parser.getAttributeValue(null, "name").trim());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText().trim();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("component")) {
                            if (text != null) {
                                plugInFWVersion.add(text.trim());
                            }
                        }

                        break;

                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInFWVersion;
    }

    private String getPlugInDate(String metaDataFilePath) {
        String plugInRev = "";
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (name.equals("release-date") && text != null) {
                            plugInRev = text.trim();
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInRev;
    }

    private String getPlugInRev(String metaDataFilePath) {
        String plugInRev = "";
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (name.equals("revision") && text != null) {
                            plugInRev = text.trim();
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInRev;
    }

    private String getPlugInName(String metaDataFilePath) {
        StringBuilder plugInName = new StringBuilder();
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (text != null) {
                            if (name.equals("family")) {
                                plugInName = new StringBuilder(text.trim());
                                plugInName.append("-");
                            }
                            if (name.equals("name")) {
                                plugInName.append(text.trim());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInName.toString();
    }

    private void ShowPlugInScannerMismatchDialog() {
        if (!isFinishing()) {
            final Dialog dialog = new Dialog(UpdateFirmware.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_plugin_scanner_mismatch);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();

                }
            });
        }
    }

    private void ShowPlugInPathChangeDialog() {
        if (!isFinishing()) {
            final Dialog dialog = new Dialog(UpdateFirmware.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_plugin_path_change);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();

                }
            });
        }
    }
    private List<String> getSupportedModels(String metaDataFileUri) {
        List<String> modelList = new ArrayList<String>();

        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFileUri);
            parser.setInput(input, null);

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
                        if (name.equals("model")) {
                            if (text != null) {
                                modelList.add(text.trim());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return modelList;
    }

    private String extractMetaData(String extractPath, Uri zipPath) {
        InputStream is = null;
        ZipInputStream zis = null;
        String unzipFile = "";

        try(ParcelFileDescriptor parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(zipPath,readWritePermission)){
            String filename;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            is = new FileInputStream(fileDescriptor);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (filename.equalsIgnoreCase("Metadata.xml")) {
                    unzipFile = extractPath + filename;

                    File targetDirectory = new File(extractPath);
                    File file = new File(unzipFile);
                    String canonicalDestinationPath = file.getCanonicalPath();

                    if (!canonicalDestinationPath.startsWith(targetDirectory.getCanonicalPath())) {
                        throw new IOException("Entry is outside of the target directory");
                    }

                    try (FileOutputStream fout = new FileOutputStream(unzipFile) ) {
                        // cteni zipu a zapis
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    }
                }
            }

        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return "";
        } finally {
            closeSafely(is);
            closeSafely(zis);
        }
        return unzipFile;
    }


    private String copyDatFile(String extractPath, Uri zipPath) {

        String datFilePath ="";
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try(ParcelFileDescriptor parcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(zipPath,readWritePermission)){
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            datFilePath =extractPath+ getFileName(zipPath);
            inStream = new FileInputStream(fileDescriptor);
            outStream = new FileOutputStream(datFilePath);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return "";
        } finally {
            closeSafely(inStream);
            closeSafely(outStream);
        }

        return datFilePath;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                closeSafely(cursor);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getScannerFirmwareVersion(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>20012</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outXML, scannerID);
        String firmwareVersion = getSingleStringValue(outXML,RMD_ATTR_FW_VERSION);
        Log.i(TAG, "Scanner firmware version(2nd attempt): " + firmwareVersion);

        if (null != firmwareVersion && (firmwareVersion.isEmpty() || firmwareVersion.equals("0"))) {
            firmwareVersion = currentScannerFirmwareVersion;
            Log.i(TAG, "Scanner firmware version taken from the 1st attempt: " + firmwareVersion);
        }

        return firmwareVersion;
    }

    private String getSingleStringValue(StringBuilder outXML , int attributeId) {
        String attr_val = "";
        int attriId = 0;
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

                        if(text!=null) {
                            if (name.equals("id")) {
                                attriId = Integer.parseInt(text.trim());

                            } else if (name.equals("value")) {

                                if (attriId == attributeId) {
                                    attr_val = text.trim();
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
        return attr_val;
    }

    private String getScannerModelNumber(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>533</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outxML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outxML, scannerID);

        String modelNumber = getSingleStringValue(outxML,RMD_ATTR_MODEL_NUMBER);

        if (null != modelNumber && (modelNumber.isEmpty() || modelNumber.equals(""))) {
            modelNumber = currentScannerModelNumber;
        }

        return modelNumber;
    }

    private void ShowNoPlugInFoundDialog() {
        if (!isFinishing()) {
            final Dialog dialog = new Dialog(UpdateFirmware.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_no_plugin);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            finish();
            intent = new Intent(UpdateFirmware.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_beacons){
            intent = new Intent(this, BeaconActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            dlg.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(UpdateFirmware.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
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
        if (isWaitingForFWUpdateToComplete && dialogFwRebooting != null) {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        if (isWaitingForFWUpdateToComplete && dialogFwReconnectScanner != null) {
            dialogFwReconnectScanner.dismiss();
            dialogFwReconnectScanner = null;
        }
        return false;
    }

    private void ShowReconnectScanner() {
        if (!isFinishing()) {
            dialogFwReconnectScanner = new Dialog(UpdateFirmware.this);
            dialogFwReconnectScanner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFwReconnectScanner.setContentView(R.layout.dialog_fw_reconnect_scanner);
            TextView cancelButton = (TextView) dialogFwReconnectScanner.findViewById(R.id.btn_cancel);
            // if decline button is clicked, close the custom dialog
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialogFwReconnectScanner.dismiss();
                    dialogFwReconnectScanner = null;
                    finish();
                }
            });
            llBarcode = (FrameLayout) dialogFwReconnectScanner.findViewById(R.id.scan_to_connect_barcode);
            barCodeView = null;

            if (fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
                barCodeView = Application.sdkHandler.dcssdkGetUSBSNAPIWithImagingBarcode();
            } else {
                barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig);
                if (barCodeView == null) {
                    barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig, HomeActivity.btAddress);
                }
            }
            //dialogFwReconnectScanner.getWindow().setLayout(getXReconnection(), getYReconnection());
            if (barCodeView != null && llBarcode != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 7 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                llBarcode.addView(barCodeView, layoutParams);
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

    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogFWProgessY * scale + 0.5f);
        return y;
    }

    private int getYReconnection() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogFWReconnectionY * scale + 0.5f);
        return y;
    }

    private int getXReconnection() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogFWReconnectionX * scale + 0.5f);
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }

    private int getX() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogFWReconnectionX * scale + 0.5f);
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }


    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        if (isWaitingForFWUpdateToComplete) {
            getIntent().putExtra(Constants.FW_REBOOT, false);
            setWaitingForFWReboot(true);
            pnpHandler.sendMessageDelayed(pnpHandler.obtainMessage(1), 180000);
        } else {
            Application.barcodeData.clear();
            finish();
        }

        if (dialogFwProgress != null) {
            dialogFwProgress.dismiss();
            dialogFwProgress = null;
        }
        return true;
    }

    private void showRebooting() {
        disconnect(scannerID);
        Application.barcodeData.clear();
        Application.currentScannerId = Application.SCANNER_ID_NONE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ScannerControl", "Show Rebooting dialog");
                if (!isFinishing()) {
                    dialogFwRebooting = new Dialog(UpdateFirmware.this);
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
                    Log.i("ScannerControl", "Showing dot progress dialog");
                    dialogFwRebooting.show();
                } else {
                    finish();
                }
            }
        });
    }


    public void updateFirmware(View view) {
        Application.isFirmwareUpdateInProgress = true;
        UpdateStatus(View.GONE, null);
        TurnOnLEDPattern();
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-string>" + documentUri + "</arg-string></cmdArgs></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_UPDATE_FIRMWARE, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    private void TurnOnLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                85 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
    }

    private void TurnOffLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                90 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
    }

    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {

    }

    @Override
    public void scannerImageEvent(byte[] imageData) {

    }

    @Override
    public void scannerVideoEvent(byte[] videoData) {
    }

    @Override
    public void scannerConfigurationUpdateEvent(ConfigurationUpdateEvent configurationUpdateEvent) {
        //Overridden abstract method not used here
    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
        Log.i("ScannerControl", "scannerFirmwareUpdateEvent type = " + firmwareUpdateEvent.getEventType());
        if (firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_SESS_START) {
            dialogFwProgress = new Dialog(UpdateFirmware.this);
            dialogFwProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFwProgress.setContentView(R.layout.dialog_fw_progress);
            TextView cancelButton = (TextView) dialogFwProgress.findViewById(R.id.btn_cancel);
            final int scannerIDFWSessionStarted = firmwareUpdateEvent.getScannerInfo().getScannerID();
            // if decline button is clicked, close the custom dialog
            cancelButton.setOnClickListener(v -> {

                //Setting message manually and performing action on button click
                alertDialogForValidateFirmwareUpdate.setMessage(R.string.msg_firmware_update_validation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.alert_action_title_yes, (dialog, id) -> {
                            //  Action for 'Yes' Button
                            String in_xml = "<inArgs><scannerID>" + scannerIDFWSessionStarted + "</scannerID></inArgs>";
                            cmdExecTask = new MyAsyncTask(scannerIDFWSessionStarted, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ABORT_UPDATE_FIRMWARE, null);
                            cmdExecTask.execute(new String[]{in_xml});

                        })
                        .setNegativeButton(R.string.alert_action_title_no, (dialog, id) -> {
                            //  Action for 'No' Button
                            dialog.cancel();

                        });
                //Creating dialog box
                AlertDialog alert = alertDialogForValidateFirmwareUpdate.create();
                //Setting the title manually
                alert.setTitle(R.string.title_firmware_update_validation);
                alert.show();

            });

            progressBar = (ProgressBar) dialogFwProgress.findViewById(R.id.progressBar);
            progressBar.setMax(firmwareUpdateEvent.getMaxRecords());
            fwMax = firmwareUpdateEvent.getMaxRecords();

            Window window = dialogFwProgress.getWindow();
            if (window != null) {
                dialogFwProgress.getWindow().setLayout(getX(), getY());
            }
            dialogFwProgress.setCancelable(false);
            dialogFwProgress.setCanceledOnTouchOutside(false);
            if (!isFinishing()) {
                Log.i("ScannerControl", "Show Progress dialog");
                dialogFwProgress.show();
                txtPercentage = (TextView) dialogFwProgress.findViewById(R.id.txt_percentage);
                txtPercentage.setText("0%");
            }

        }
        if (firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_DL_PROGRESS) {
            if (progressBar != null) {
                progressBar.setProgress(firmwareUpdateEvent.getCurrentRecord());
                double percentage = (firmwareUpdateEvent.getCurrentRecord() * 100.0 / fwMax);
                int iPercentage = (int) percentage;
                if (txtPercentage != null) {
                    txtPercentage.setText(String.format("%s%%", Integer.toString(iPercentage)));
                }
            }
            if (dialogFwProgress != null && !dialogFwProgress.isShowing() && !isFinishing()) {
                Log.i("ScannerControl", "Show Progress dialog");
                dialogFwProgress.show();
            }
        }

        if (firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_SESS_END) {
            if (dialogFwProgress != null) {
                if (txtPercentage != null) {
                    txtPercentage.setText("100%");
                }
                dialogFwProgress.dismiss();
                dialogFwProgress = null;
                String in_xml = "<inArgs><scannerID>" + firmwareUpdateEvent.getScannerInfo().getScannerID() + "</scannerID></inArgs>";
                StringBuilder outXML = new StringBuilder();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_START_NEW_FIRMWARE, in_xml, outXML, firmwareUpdateEvent.getScannerInfo().getScannerID());
                isWaitingForFWUpdateToComplete = true;
                getIntent().putExtra(Constants.FW_REBOOT, true);
                showRebooting();
            }
        }

        if (firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_STATUS) {
            TurnOffLEDPattern();
            isWaitingForFWUpdateToComplete = false;
            if (!isFinishing() && dialogFwProgress != null) {
                dialogFwProgress.dismiss();
                dialogFwProgress = null;
            }
            if (firmwareUpdateEvent.getStatus() != DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FIRMWARE_UPDATE_ABORTED) {
                if (firmwareUpdateEvent.getStatus() == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
                    runOnUiThread(() -> UpdateStatus(View.VISIBLE, null));
                } else {
                    final String failureReason  = getFlashResponseErrorDescription(firmwareUpdateEvent.getStatus());
                    runOnUiThread(() -> UpdateStatus(View.VISIBLE, failureReason));
                }

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
        }
        return errorDescription;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (dialogFwProgress != null) {
            Window window = dialogFwProgress.getWindow();
            if (window != null) {
                window.setLayout(getX(), getY());
            }
        }
        if (dialogFwRebooting != null) {
            Window window = dialogFwRebooting.getWindow();
            if (window != null) {
                window.setLayout(getX(), getY());
            }
        }
        if (dialogFwReconnectScanner != null) {
            if (barCodeView != null && llBarcode != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 7 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                llBarcode.removeAllViews();
                llBarcode.addView(barCodeView, layoutParams);
            }
            Window window = dialogFwReconnectScanner.getWindow();
            if (window != null) {
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, getY());
            }
        }

    }

    /**
     * select firmware file 
     * @param view user interface
     */
    public void selectFirmware(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, FILE_SELECT_REQUEST_READ_EX_STORAGE);
    }

    /**
     * Get the scanner firmware version
     */
    private void getScannerFirmwareVersion() {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, INVALID_SCANNER_ID);

        if (scannerID != INVALID_SCANNER_ID) {

            String in_xml = "<inArgs><scannerID>" + scannerID + " </scannerID><cmdArgs><arg-xml><attrib_list>";
            in_xml += RMD_ATTR_MODEL_NUMBER;
            in_xml += ",";
            in_xml += RMD_ATTR_SERIAL_NUMBER;
            in_xml += ",";
            in_xml += RMD_ATTR_FW_VERSION;
            in_xml += ",";
            in_xml += RMD_ATTR_CONFIG_NAME;
            in_xml += ",";
            in_xml += RMD_ATTR_DOM;
            in_xml += "</attrib_list></arg-xml></cmdArgs></inArgs>";

            new AsyncTaskFirmwareVersion(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{in_xml});
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        private CustomProgressDialog progressDialog;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(UpdateFirmware.this, "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (!b) {
                Toast.makeText(UpdateFirmware.this, "Cannot perform the action", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UIUpdater extends AsyncTask<String, Integer, Boolean> {
        private CustomProgressDialog progressDialog;
        private boolean bFwReboot;

        public UIUpdater(boolean fwReboot) {
            bFwReboot = fwReboot;
            Log.d("Firmware", "onResume() reboot true 3");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Firmware", "onResume() reboot true 4");
            progressDialog = new CustomProgressDialog(UpdateFirmware.this, "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d("Firmware", "onResume() reboot true 5");
            return ShowFirmwareInfo(bFwReboot);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (!b) {
                Toast.makeText(UpdateFirmware.this, "Cannot perform the action", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AsyncTaskFirmwareVersion extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        private CustomProgressDialog progressDialog;

        public AsyncTaskFirmwareVersion(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
            this.scannerId = scannerId;
            this.opcode = opcode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(UpdateFirmware.this, "Execute Command...");
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
                        Log.i(TAG, "Getting firmware info from the scanner");

                        Log.i(TAG, sb.toString());
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
                                    Log.i(TAG, "Name of the END_TAG: " + name);
                                    if (text != null) {
                                        if (name.equals("id")) {
                                            attrId = Integer.parseInt(text.trim());
                                            Log.i(TAG, "ID tag found: ID: " + attrId);
                                        } else if (name.equals("value")) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);
                                            if (RMD_ATTR_FW_VERSION == attrId) {
                                                currentScannerFirmwareVersion = attrVal;
                                                Log.i(TAG, "Scanner firmware version(1st attempt): " + attrVal);
                                            }
                                            if (RMD_ATTR_MODEL_NUMBER == attrId) {
                                                currentScannerModelNumber = attrVal;
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
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }


    private boolean closeSafely (Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}
