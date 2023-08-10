package com.zebra.scannercontrol.app.helpers;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.File;

/**
 * FileObserverService will continuously check each file change inside ZebraSMS folder and immediately notifies in OnEvent Method
 * */
public class FileObserverService extends Service {
    private FileObserver mFileObserver;
    private String INTENT_EXTRA_FILEPATH = "INTENT_EXTRA_FILEPATH";
    private final static String TAG = FileObserverService.class.getSimpleName();
    public final static String BUNDLED_LISTENER = "listener";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra(INTENT_EXTRA_FILEPATH)){
            // we store the path of directory inside the intent that starts the service
            mFileObserver = new FileObserver(intent.getStringExtra(INTENT_EXTRA_FILEPATH),FileObserver.CLOSE_WRITE) {
                @Override
                public void onEvent(int event, String path) {
                    // for example we can send a broadcast message with the event-idz
                    if(event == CLOSE_WRITE && (path.contains("smspkg") || path.contains(".SMSPKG"))){
                        Log.d("FILEOBSERVER_EVENT", "Event with id " + Integer.toHexString(event) + " happened "+"PATH: "+path); // event identifies the occured Event in hex
                    }
                }
            };
            mFileObserver.startWatching(); // The FileObserver starts watching
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        mFileObserver.stopWatching();
        super.onDestroy();
    }
}