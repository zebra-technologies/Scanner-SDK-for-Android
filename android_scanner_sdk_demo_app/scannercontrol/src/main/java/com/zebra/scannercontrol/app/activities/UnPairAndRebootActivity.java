package com.zebra.scannercontrol.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.DotsProgressBar;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_ACTION_REBOOT_AND_UN_PAIR;

public class UnPairAndRebootActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {

    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    int scannerID;

    int dialogFWProgressY = 220;
    int dialogFWReconnectionX = 50;

    DotsProgressBar dotProgressBar;
    static Dialog dialogFwRebooting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unpair_and_reboot);

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
            getSupportActionBar().setTitle(R.string.unpair_and_reboot);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        boolean isUnPairAndRebootAvailable = getIntent().getBooleanExtra(Constants.UNPAIR_AND_REBOOT_STATUS, false);
        boolean isCS6080Scanner = scannerName.trim().toLowerCase().contains(this.getResources().getString(R.string.cs6080_scanner));
        TextView textView = (TextView) findViewById(R.id.txt_no_unpair_and_reboot);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_unpair_and_reboot);
        if (isUnPairAndRebootAvailable | isCS6080Scanner) {
            linearLayout.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeDevConnectiosDelegate(this);
        Application.isFirmwareUpdateInProgress = false;
    }

    /**
     * Perform unpair and reboot operation once the button is clicked
     *
     * @param view Button view
     */
    public void UnPairAndReboot(View view) {
        int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        if (scannerID != -1) {
            setRebootAndUnPair(scannerID, RMD_ATTR_ACTION_REBOOT_AND_UN_PAIR);
            reboot();
            showRebooting();
        } else {
            Toast.makeText(this, Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Execute command for reboot operation
     */
    public void reboot() {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" + 6004 + "</id><datatype>X</datatype><value>" + Integer.valueOf(1) + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, in_xml, outXML, scannerID);
    }

    /**
     * Execute command for unpair and reboot operation
     *
     * @param scannerId   Scanner id
     * @param attributeId Id of un pair and reboot action attribute
     */
    public void setRebootAndUnPair(int scannerId, int attributeId) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>"
                + attributeId + "</id><datatype>X</datatype><value>" + 1 + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, in_xml, outXML, scannerId);
    }

    /**
     * Show progress dialog when the device reboots
     */
    private void showRebooting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ScannerControl", "Show Rebooting dialog");
                if (!isFinishing()) {
                    dialogFwRebooting = new Dialog(UnPairAndRebootActivity.this);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (dialogFwRebooting != null) {
            Window window = dialogFwRebooting.getWindow();
            if (window != null) {
                window.setLayout(getX(), getY());
            }
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
            intent = new Intent(UnPairAndRebootActivity.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle(this.getResources().getString(R.string.scanner_disconnect_string));
            dlg.setPositiveButton(this.getResources().getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(UnPairAndRebootActivity.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton(this.getResources().getString(R.string.cancel_string), new DialogInterface.OnClickListener() {
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
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        if (dialogFwRebooting != null) {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        if (dialogFwRebooting != null) {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        Application.barcodeData.clear();
        finish();
        return true;
    }

    /**
     * Get width of the progress dialog
     */
    private int getX() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogFWReconnectionX * scale + 0.5f);
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }

    /**
     * Get height of the progress dialog
     */
    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogFWProgressY * scale + 0.5f);
        return y;
    }
}