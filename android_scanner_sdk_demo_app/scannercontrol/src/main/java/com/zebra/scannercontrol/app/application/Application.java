package com.zebra.scannercontrol.app.application;

import android.os.Environment;
import android.os.Handler;

import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.app.helpers.Barcode;
import com.zebra.scannercontrol.app.helpers.Foreground;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Application class to maintain global state
 */
public class Application extends android.app.Application {
    //Instance of SDK Handler
    public static SDKHandler sdkHandler;

    //Handler to handle bluetooth events
    public static Handler globalMsgHandler;

    //Var to access scanner app engine
    public static ScannerAppEngine scannerAppEngine;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //Settings for notifications
    public static int MOT_SETTING_OPMODE ;
    public static boolean MOT_SETTING_SCANNER_DETECTION;
    public static boolean MOT_SETTING_EVENT_ACTIVE;
    public static boolean MOT_SETTING_EVENT_AVAILABLE;
    public static boolean MOT_SETTING_EVENT_BARCODE;
    public static boolean MOT_SETTING_EVENT_IMAGE;
    public static boolean MOT_SETTING_EVENT_VIDEO;
    public static boolean MOT_SETTING_EVENT_BINARY_DATA;

    public static boolean MOT_SETTING_NOTIFICATION_ACTIVE;
    public static boolean MOT_SETTING_NOTIFICATION_AVAILABLE;
    public static boolean MOT_SETTING_NOTIFICATION_BARCODE;
    public static boolean MOT_SETTING_NOTIFICATION_IMAGE;
    public static boolean MOT_SETTING_NOTIFICATION_VIDEO;
    public static boolean MOT_SETTING_NOTIFICATION_BINARY_DATA;

    public static volatile int INTENT_ID = 0xFFFF;

    public static int SCANNER_ID_NONE=  -1;
    public static String currentScannerName ="";
    public static String currentScannerAddress ="";
    public static int currentScannerId =SCANNER_ID_NONE;
    public static boolean currentAutoReconnectionState =true;
    public static boolean isAnyScannerConnected = false; //True, if currently connected to any scanner
    public static int currentConnectedScannerID = -1; //Track scannerId of currently connected Scanner
    public static boolean isFirmwareUpdateInProgress = false;
    public static boolean intentionallyDisconnected = false;
    public static boolean virtualTetherHostActivated = false;
    //Scanners (both available and active)
    public static ArrayList<DCSScannerInfo> mScannerInfoList=new ArrayList<DCSScannerInfo>();
    public static ArrayList<ScannerAppEngine.IScannerAppEngineDevListDelegate> mDevListDelegates=new ArrayList<ScannerAppEngine.IScannerAppEngineDevListDelegate>();
    //Barcode data
    public static ArrayList<Barcode> barcodeData=new ArrayList<Barcode>();
    public static DCSScannerInfo currentConnectedScanner;
    public static DCSScannerInfo lastConnectedScanner;


    public static int minScreenWidth = 360;

    @Override
    public void onCreate() {
        super.onCreate();
        Foreground.init(this);
        //this keyword referring to Context of the sample application
        sdkHandler = new SDKHandler(this, true);
    }
}
