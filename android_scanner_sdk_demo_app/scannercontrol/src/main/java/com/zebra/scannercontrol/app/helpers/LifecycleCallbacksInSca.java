package com.zebra.scannercontrol.app.helpers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by pndv47 on 4/29/2016.
 */
public class LifecycleCallbacksInSca implements Application.ActivityLifecycleCallbacks {

    private static LifecycleCallbacksInSca instance;

    public static void init(Application app){
        if (instance == null){
            instance = new LifecycleCallbacksInSca();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static LifecycleCallbacksInSca get(){
        return instance;
    }

    private LifecycleCallbacksInSca(){}
    private boolean foreground;

    public boolean isForeground(){
        return foreground;
    }

    public boolean isBackground(){
        return !foreground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    // TODO: implement the lifecycle callback methods!

}
