package com.zebra.scannercontrol.app.helpers;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;

public class UIEnhancer {
    /*
     *Method to set Edge-to-Edge display content
     * */
    public static void enableEdgeToEdge(View view){
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static void enableEdgeForNavigationDrawer(View view){
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(0, 0, 0, 0);

            return WindowInsetsCompat.CONSUMED;
        });

    }

    public static void enableEdgeForNavigationDrawer(NavigationView view, Activity activity){
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(0, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    view.setItemHorizontalPadding(bars.left);
                    view.getHeaderView(0).setPadding(bars.left,16,16,16);
                }else{
                    view.setItemHorizontalPadding(50);
                    view.getHeaderView(0).setPadding(50,16,16,16);
                }
            }
            return WindowInsetsCompat.CONSUMED;
        });

    }


    /*
    * Method to Configure Orientation
    **/
    public static void configureOrientation(Activity activity){
        Configuration configuration = activity.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (configuration.smallestScreenWidthDp < Application.minScreenWidth) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (configuration.screenWidthDp < Application.minScreenWidth) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }



}
