package com.zebra.scannercontrol.app.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.BackgroundSoundService;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import static com.zebra.scannercontrol.RMDAttributes.ACTION_COMMAND_VIRTUAL_TETHER_START_SIMULATION;
import static com.zebra.scannercontrol.RMDAttributes.ACTION_COMMAND_VIRTUAL_TETHER_STOP_SIMULATION;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_VIRTUAL_TETHER_ALARM_DISABLE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_VIRTUAL_TETHER_ALARM_ENABLE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS;
import static com.zebra.scannercontrol.app.application.Application.virtualTetherEventOccurred;
import static com.zebra.scannercontrol.app.application.Application.virtualTetherHostActivated;
import static com.zebra.scannercontrol.app.helpers.Constants.VIRTUAL_TETHER_AUDIO_ALARM_PATTERN;
import static com.zebra.scannercontrol.app.helpers.Constants.VIRTUAL_TETHER_EVENT_NOTIFY;
import static com.zebra.scannercontrol.app.helpers.Constants.VIRTUAL_TETHER_SCANNER_ENABLE_VALUE;


/**
 * This class is responsible for handling activities related to scanner tethering, virtual tether audio settings and virtual tether alarm indication settings
 */
public class VirtualTetherSettings extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {
    static VirtualTetherAsyncTask cmdExecTask = null;
    static VirtualTetherAttributeGetAsyncTask myAsyncTask = null;
    MenuItem pairNewScannerMenu;
    int scannerID;
    Menu menu;
    SwitchCompat enableVirtualTetherSwitch;
    SwitchCompat simulationSwitch;
    SwitchCompat virtualTetherHostFeedback;
    SwitchCompat virtualTetherHostVibrate;
    SwitchCompat virtualTetherHostAudioAlarm;
    SwitchCompat virtualTetherHostFlashingScreen;
    SwitchCompat virtualTetherHostPopUPMessage;
    Button pauseAlarmVirtualTetherButton;
    Button stopAlarmVirtualTetherButton;
    AlertDialog.Builder alertDialogForVirtualTetherPopupMessage;
    Handler simulationHandler;
    Handler pauseAlarmHandler;
    boolean isOnPause = false;
    int pauseDuration = 3000;
    int simulationDuration = 5000;
    Boolean virtualTetherSimulationOccurred;
    Boolean virtualTetherAlarmStopped;
    LinearLayout linearLayoutVirtualThether;
    ValueAnimator colorAnimation;
    private NavigationView navigationView;
    AlertDialog alertDialogVirtualTetherPopupMessage;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_tether_settings);

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
            getSupportActionBar().setTitle(R.string.title_activity_virtual_tether);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        linearLayoutVirtualThether = findViewById(R.id.linearLayoutVirtualThether);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);


        simulationSwitch = (SwitchCompat) findViewById(R.id.switch_simulation);
        enableVirtualTetherSwitch = (SwitchCompat) findViewById(R.id.switch_enable_virtual_tether);
        virtualTetherHostFeedback = (SwitchCompat) findViewById(R.id.virtual_tether_host_feedback);
        virtualTetherHostVibrate = (SwitchCompat) findViewById(R.id.virtual_tether_host_vibrate);
        virtualTetherHostAudioAlarm = (SwitchCompat) findViewById(R.id.virtual_tether_host_audio_alarm);
        virtualTetherHostFlashingScreen = (SwitchCompat) findViewById(R.id.virtual_tether_host_flashing_screen);
        virtualTetherHostPopUPMessage = (SwitchCompat) findViewById(R.id.virtual_tether_host_pop_up_message);

        pauseAlarmVirtualTetherButton = findViewById(R.id.btn_pause_alarm);
        stopAlarmVirtualTetherButton = findViewById(R.id.btn_virtual_tether_stop_alarm_on_host);

        alertDialogForVirtualTetherPopupMessage = new AlertDialog.Builder(this);
        simulationHandler = new Handler();
        pauseAlarmHandler = new Handler();
        loadSavedSettings();
        virtualTetherEventOccurred = getIntent().getBooleanExtra(VIRTUAL_TETHER_EVENT_NOTIFY, false);
      if(virtualTetherEventOccurred || virtualTetherHostActivated) {
                virtualTetherHostActivated = false;
                stopAlarmVirtualTetherButton.setEnabled(true);
                virtualTetherAlarmStopped = false;
                showVirtualTetherPopUpMessage();
                enableFlashScreen();
                startVirtualTetherAudioAlarm();
                startVirtualTetherVibrateAlarm();

        }


        stopAlarmVirtualTetherButton.setOnClickListener(v -> {
            boolean hasVibrator = vibrator.hasVibrator();
            if (virtualTetherEventOccurred || virtualTetherHostActivated ||virtualTetherSimulationOccurred) {
                if (hasVibrator) {
                    vibrator.cancel();
                }
                stopVirtualTetherAudioAlarm();
                stopAlarmVirtualTetherButton.setEnabled(false);
                virtualTetherHostActivated=false;
                virtualTetherAlarmStopped = true;

                if (colorAnimation != null) {
                    linearLayoutVirtualThether.setBackgroundColor(getResources().getColor(R.color.clear_color));
                    colorAnimation.removeAllListeners();
                    colorAnimation.cancel();
                }


            }
        });

        pauseAlarmVirtualTetherButton.setOnClickListener(v -> {
            isOnPause = true;
            simulationHandler.removeCallbacksAndMessages(null);
            stopVirtualTetherSimulation();
            pauseAlarmVirtualTetherButton.setEnabled(false);
            simulationSwitch.setClickable(false);

            pauseAlarmHandler.postDelayed(new Runnable() {
                public void run() {
                    pauseAlarmVirtualTetherButton.setEnabled(true);

                    initiateVirtualTetherSimulation();

                }
            }, pauseDuration);


        });
        enableVirtualTetherSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                enableVirtualTetherAlarm();
                enableHostFeedback();
            } else {
                disableVirtualTetherAlarm();
                disableHostFeedback();
            }
        });

        virtualTetherHostFeedback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_FEEDBACK, true).apply();
                    if (!virtualTetherHostVibrate.isChecked() || !virtualTetherHostAudioAlarm.isChecked() || !virtualTetherHostFlashingScreen.isChecked() || !virtualTetherHostPopUPMessage.isChecked()) {
                        enableHostFeedbackOptions();

                    }

                } else {
                    settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_FEEDBACK, false).apply();
                    disableHostFeedbackOptions();

                }
            }
        });
        virtualTetherHostVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM, true).apply();
                } else {
                    settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM, false).apply();
                    boolean hasVibrator = vibrator.hasVibrator();
                    if (hasVibrator) {
                        vibrator.cancel();
                    }
                }
                disableHostFeedbackSwitch();
            }
        });
        virtualTetherHostAudioAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
            if (isChecked) {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM, true).apply();
            } else {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM, false).apply();
            }
            disableHostFeedbackSwitch();
        });
        virtualTetherHostFlashingScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();

            if (isChecked) {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH, true).apply();

            } else {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH, false).apply();
            }
            disableHostFeedbackSwitch();
        });
        virtualTetherHostPopUPMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
            if (isChecked) {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE, true).apply();
            } else {
                settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE, false).apply();
            }
            disableHostFeedbackSwitch();
        });

        simulationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                pauseAlarmHandler.removeCallbacksAndMessages(null);
                initiateVirtualTetherSimulation();

                simulateVirtualTetherHostFeedback();
                virtualTetherSimulationOccurred=true;
                stopVirtualTetherHostFeedbackSimulation();
            }

        });


    }

    /**
     * Enable the screen flashing
     */
    private void enableFlashScreen() {

        SharedPreferences virtualTetherSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        if (virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH, true)) {

            int colorFrom = getResources().getColor(R.color.light_gray);
            int colorTo = getResources().getColor(R.color.blue);
            colorAnimation = ObjectAnimator.ofInt(linearLayoutVirtualThether, Constants.PREF_VIRTUAL_TETHER_HOST_BACKGROUND_COLOR, colorFrom, colorTo);

            colorAnimation.setDuration(Constants.VIRTUAL_TETHER_HOST_ANIMATION_DURATION);
            colorAnimation.setEvaluator(new ArgbEvaluator());

            colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.start();

        }

    }

    /**
     * This method is used to validate host feedback slider button with host feedback options
     */
    private void disableHostFeedbackSwitch() {
        if (!virtualTetherHostVibrate.isChecked() && !virtualTetherHostAudioAlarm.isChecked() && !virtualTetherHostFlashingScreen.isChecked() && !virtualTetherHostPopUPMessage.isChecked()) {
            virtualTetherHostFeedback.setChecked(false);

        } else if (virtualTetherHostVibrate.isChecked() || virtualTetherHostAudioAlarm.isChecked() || virtualTetherHostFlashingScreen.isChecked() || virtualTetherHostPopUPMessage.isChecked()) {

            virtualTetherHostFeedback.setChecked(true);

        }
    }

    /**
     * This method is used to enable all virtual tether host feedback slider buttons
     */
    private void enableHostFeedback() {

        virtualTetherHostFeedback.setEnabled(true);
        virtualTetherHostVibrate.setEnabled(true);
        virtualTetherHostAudioAlarm.setEnabled(true);
        virtualTetherHostFlashingScreen.setEnabled(true);
        virtualTetherHostPopUPMessage.setEnabled(true);

        virtualTetherHostFeedback.setChecked(true);
        virtualTetherHostVibrate.setChecked(true);
        virtualTetherHostAudioAlarm.setChecked(true);
        virtualTetherHostFlashingScreen.setChecked(true);
        virtualTetherHostPopUPMessage.setChecked(true);
    }

    /**
     * This method is used to enable all virtual tether host feedback options slider buttons
     */
    private void enableHostFeedbackOptions() {
        if (enableVirtualTetherSwitch.isChecked()) {
            virtualTetherHostVibrate.setChecked(true);
            virtualTetherHostAudioAlarm.setChecked(true);
            virtualTetherHostFlashingScreen.setChecked(true);
            virtualTetherHostPopUPMessage.setChecked(true);

        }
    }

    /**
     * This method is used to disable all virtual tether host feedback options slider buttons
     */
    private void disableHostFeedbackOptions() {
        virtualTetherHostVibrate.setChecked(false);
        virtualTetherHostAudioAlarm.setChecked(false);
        virtualTetherHostFlashingScreen.setChecked(false);
        virtualTetherHostPopUPMessage.setChecked(false);
    }

    /**
     * This method is used to disable all virtual tether host feedback slider buttons
     */
    private void disableHostFeedback() {
        virtualTetherHostFeedback.setEnabled(false);
        virtualTetherHostVibrate.setEnabled(false);
        virtualTetherHostAudioAlarm.setEnabled(false);
        virtualTetherHostFlashingScreen.setEnabled(false);
        virtualTetherHostPopUPMessage.setEnabled(false);
        virtualTetherHostFeedback.setChecked(false);
        virtualTetherHostVibrate.setChecked(false);
        virtualTetherHostAudioAlarm.setChecked(false);
        virtualTetherHostFlashingScreen.setChecked(false);
        virtualTetherHostPopUPMessage.setChecked(false);
    }

    /**
     * This method is responsible for simulating the Virtual Tether audio, LED, haptics and illumination alarms.
     */
    public void startVirtualTetherSimulation() {


        pauseAlarmVirtualTetherButton.setEnabled(true);
        pauseAlarmVirtualTetherButton.setClickable(true);

        simulationSwitch.setChecked(true);
        simulationSwitch.setClickable(false);


        String in_xml_for_simulation = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" + ACTION_COMMAND_VIRTUAL_TETHER_START_SIMULATION + "</arg-int></cmdArgs></inArgs>";
        cmdExecTask = new VirtualTetherAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION);
        cmdExecTask.execute(in_xml_for_simulation);


    }

    /**
     * This method is responsible for stopping the simulation of the Virtual Tether audio, LED, haptics and illumination alarms.
     */
    public void stopVirtualTetherSimulation() {


        String in_xml_for_simulation = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" + ACTION_COMMAND_VIRTUAL_TETHER_STOP_SIMULATION + "</arg-int></cmdArgs></inArgs>";
        cmdExecTask = new VirtualTetherAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION);
        cmdExecTask.execute(in_xml_for_simulation);

        pauseAlarmVirtualTetherButton.setEnabled(false);
        pauseAlarmVirtualTetherButton.setClickable(false);

        if (!isOnPause) {

            simulationSwitch.setChecked(false);
            simulationSwitch.setClickable(true);

        }


    }
    /**
     * This method is responsible for stopping the simulation of the Virtual Tether host alarms.
     */
    public void stopVirtualTetherHostFeedbackSimulation() {


        final Handler simulationHandler = new Handler(Looper.getMainLooper());
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {



                boolean hasVibrator = vibrator.hasVibrator();
                if (virtualTetherEventOccurred ||virtualTetherSimulationOccurred) {
                  if(virtualTetherHostPopUPMessage.isChecked()) {
                      alertDialogVirtualTetherPopupMessage.dismiss();
                  }
                    if (hasVibrator) {
                        vibrator.cancel();
                    }
                    stopVirtualTetherAudioAlarm();
                    stopAlarmVirtualTetherButton.setEnabled(false);
                    virtualTetherHostActivated = false;
                    virtualTetherAlarmStopped = true;

                    if (colorAnimation != null) {
                        linearLayoutVirtualThether.setBackgroundColor(getResources().getColor(R.color.clear_color));
                        colorAnimation.removeAllListeners();
                        colorAnimation.cancel();
                    }
                    virtualTetherSimulationOccurred=false;
                }
            }
        }, simulationDuration);


    }
    /**
     * This method is responsible for initiating the simulation process of Virtual Tether audio, LED ,haptics , illumination alarms
     */
    public void initiateVirtualTetherSimulation() {

        isOnPause = false;
        startVirtualTetherSimulation();

        simulationHandler.postDelayed(() -> stopVirtualTetherSimulation(), simulationDuration);

    }

    /**
     * Method to simulate virtual tether host feedback
     */
    private void simulateVirtualTetherHostFeedback() {

        if(virtualTetherHostFeedback.isChecked()) {
            stopAlarmVirtualTetherButton.setEnabled(true);
            stopAlarmVirtualTetherButton.setClickable(true);
        }
        //simulate host vibration
        if(virtualTetherHostVibrate.isChecked()) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                vibrator.vibrate(VIRTUAL_TETHER_AUDIO_ALARM_PATTERN, 0, attributes);
            } else {
                vibrator.vibrate(VIRTUAL_TETHER_AUDIO_ALARM_PATTERN, 0);
            }
        }
        //simulate host audio alarm
        if(virtualTetherHostAudioAlarm.isChecked()) {
            Intent playAudio = new Intent(getApplicationContext(), BackgroundSoundService.class);
            startService(playAudio);
        }
        //simulate host Pop-Up message
        if(virtualTetherHostPopUPMessage.isChecked()) {
            showVirtualTetherPopUpMessage();
        }
        //simulate host flash screen
        if(virtualTetherHostFlashingScreen.isChecked()) {
            enableFlashScreen();
        }
    }

    /**
     * Method to show pop-up message for the virtual tether event
     */
    private void showVirtualTetherPopUpMessage() {

        SharedPreferences virtualTetherSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        if (virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE, false)) {

            //Setting message manually and performing action on button click
            alertDialogForVirtualTetherPopupMessage.setMessage(R.string.virtual_tether_host_pop_up_message_description)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        boolean hasVibrator = vibrator.hasVibrator();
                        if (virtualTetherEventOccurred ||virtualTetherSimulationOccurred) {
                            if (hasVibrator) {
                                vibrator.cancel();
                            }
                            stopVirtualTetherAudioAlarm();
                            stopAlarmVirtualTetherButton.setEnabled(false);
                            virtualTetherHostActivated = false;
                            virtualTetherAlarmStopped = true;

                            if (colorAnimation != null) {
                                linearLayoutVirtualThether.setBackgroundColor(getResources().getColor(R.color.clear_color));
                                colorAnimation.removeAllListeners();
                                colorAnimation.cancel();
                            }

                        }
                    });

            //Creating dialog box
          alertDialogVirtualTetherPopupMessage = alertDialogForVirtualTetherPopupMessage.create();

            alertDialogVirtualTetherPopupMessage.show();
        }
    }

    /**
     * Method to start background sound service
     */
    private void startVirtualTetherAudioAlarm() {

        SharedPreferences virtualTetherSavedSettings = getSharedPreferences(Constants.PREFS_NAME, 0);
        if (virtualTetherSavedSettings.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM, false)) {
            Intent musicintent = new Intent(getApplicationContext(), BackgroundSoundService.class);
            startService(musicintent);
        }
    }

    /**
     * Method to start background vibration
     */
    private void startVirtualTetherVibrateAlarm() {

        SharedPreferences virtualTetherSavedSettings = getSharedPreferences(Constants.PREFS_NAME, 0);
        if (virtualTetherSavedSettings.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM, false)) {
            boolean hasVibrator = vibrator.hasVibrator();
            if (!hasVibrator) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();
                    vibrator.vibrate(VIRTUAL_TETHER_AUDIO_ALARM_PATTERN, 0, attributes);
                } else {
                    vibrator.vibrate(VIRTUAL_TETHER_AUDIO_ALARM_PATTERN, 0);
                }
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.virtual_tether_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.virtual_tether_help: {
                if (!isFinishing()) {
                    Toast.makeText(getApplicationContext(), "Help Topics", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {


        if (virtualTetherEventOccurred && !virtualTetherAlarmStopped) {
            boolean hasVibrator = vibrator.hasVibrator();

            if (hasVibrator) {
                vibrator.cancel();
            }
            stopVirtualTetherAudioAlarm();
            stopAlarmVirtualTetherButton.setEnabled(false);
            virtualTetherHostActivated= false;
            virtualTetherAlarmStopped = true;

            if (colorAnimation != null) {
                linearLayoutVirtualThether.setBackgroundColor(getResources().getColor(R.color.clear_color));
                colorAnimation.removeAllListeners();
                colorAnimation.cancel();
            }


        } else if (virtualTetherEventOccurred && virtualTetherAlarmStopped) {
            Intent intent = new Intent(VirtualTetherSettings.this, HomeActivity.class);
            startActivity(intent);
            finish();

        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            Application.setScannerDisconnectedIntention(true);
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            finish();
            intent = new Intent(VirtualTetherSettings.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            dlg.setPositiveButton("Continue", (dialog, arg) -> {

                disconnect(scannerID);
                Application.barcodeData.clear();
                Application.currentScannerId = Application.SCANNER_ID_NONE;
                Application.setScannerDisconnectedIntention(true);
                finish();
                Intent intent_virtual = new Intent(VirtualTetherSettings.this, FindCabledScanner.class);
                startActivity(intent_virtual);
            });

            dlg.setNegativeButton("Cancel", (dialog, arg) -> {

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
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
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

    /**
     * This method is responsible for enabling Virtual Tether audio, LED,haptics, illumination alarms at page load in order to prepare for a simulation event.
     *
     * @param attributeID      - Attribute ID of the scanner
     * @param attributeIDValue - value assigned for the particular Attribute ID
     */
    public void settingAttributesInScanner(int attributeID, int attributeIDValue) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" + attributeID + "</id><datatype>B</datatype><value>" + attributeIDValue + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        String inXml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>"+RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS+"</attrib_list></arg-xml></cmdArgs></inArgs>";
        cmdExecTask = new VirtualTetherAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_STORE);
        cmdExecTask.execute(in_xml);

    }

    /**
     * This method is responsible for enabling the Virtual Tether alarm in the scanner
     *

     */
    public void enableVirtualTetherAlarm() {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" + RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS + "</id><datatype>B</datatype><value>" + RMD_ATTR_VALUE_VIRTUAL_TETHER_ALARM_ENABLE + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        cmdExecTask = new VirtualTetherAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_STORE);
        cmdExecTask.execute(in_xml);


    }

    /**
     * This method is responsible for disabling the Virtual Tether alarm in the scanner
     *
     *
     */
    public void disableVirtualTetherAlarm() {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" + RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS + "</id><datatype>B</datatype><value>" + RMD_ATTR_VALUE_VIRTUAL_TETHER_ALARM_DISABLE + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        cmdExecTask = new VirtualTetherAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_STORE);
        cmdExecTask.execute(in_xml);


    }
    /**
     * This method is to get virtual tether attribute from the scanner
     */
    public boolean getVirtualTetherScannerStatus() {
        String inXml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>"+RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS+"</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        myAsyncTask = new VirtualTetherAttributeGetAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, outXML);
        myAsyncTask.execute(inXml);
        boolean attributeValue = false;
        try {
            myAsyncTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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
                        if (name.equals("value") && text != null) {
                            if(text.trim().equals(VIRTUAL_TETHER_SCANNER_ENABLE_VALUE))
                                attributeValue = true;
                            else
                                attributeValue = false;
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return attributeValue;
    }

    /**
     * Load the virtual tether configurations from application
     */

    private void loadSavedSettings() {

        boolean virtualTetherScannerStatus = getVirtualTetherScannerStatus();

        SharedPreferences virtualTetherSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        if (virtualTetherScannerStatus && !virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_SCANNER_SETTINGS, false)) {
            enableHostFeedback();
            enableVirtualTetherSwitch.setChecked(true);

            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_SCANNER_SETTINGS, true).apply();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_FEEDBACK, true).apply();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM, true).apply();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM, true).apply();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE, true).apply();
            settingsEditor.putBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH, true).apply();

        } else if (virtualTetherScannerStatus && virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_SCANNER_SETTINGS, false)) {
            enableVirtualTetherSwitch.setChecked(true);
            virtualTetherHostFeedback.setChecked(virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_FEEDBACK, false));
            virtualTetherHostVibrate.setChecked(virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM, false));
            virtualTetherHostAudioAlarm.setChecked(virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM, false));
            virtualTetherHostPopUPMessage.setChecked(virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE, false));
            virtualTetherHostFlashingScreen.setChecked(virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH, false));
        } else if (!virtualTetherScannerStatus && !virtualTetherSharedPreferences.getBoolean(Constants.PREF_VIRTUAL_TETHER_SCANNER_SETTINGS, false)) {
            disableHostFeedbackSwitch();
            disableHostFeedback();
            enableVirtualTetherSwitch.setChecked(false);

        }

    }

    /**
     * This method is to shop the audio alarm background service
     */
    private void stopVirtualTetherAudioAlarm() {
        Intent intent = new Intent(getApplicationContext(), BackgroundSoundService.class);
        stopService(intent);
    }

    private class VirtualTetherAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public VirtualTetherAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
            this.scannerId = scannerId;
            this.opcode = opcode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            return executeCommand(opcode, strings[0], sb, scannerId);

        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

        }
    }

    /**
     * This is to get virtual tether attribute from the scanner
     */
    private class VirtualTetherAttributeGetAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        private CustomProgressDialog progressDialog;

        public VirtualTetherAttributeGetAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(VirtualTetherSettings.this, "Execute Command...");
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

        }
    }
}