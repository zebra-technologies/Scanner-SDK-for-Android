package com.zebra.scannercontrol.app.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.ActiveScannerActivity;
import com.zebra.scannercontrol.app.activities.HomeActivity;
import com.zebra.scannercontrol.app.activities.ScannersActivity;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;

/**
 * This class acts as a receiver for the Scanner Events when the app is running in the background.
 * It adds the message as a notification.
 */
public class NotificationsReceiver extends BroadcastReceiver {

    //Default Notification ID
    public static int DEFAULT_NOTIFICATION_ID = 1;

    public NotificationsReceiver() {

    }

    private int getNotificationIcon() {
        return R.drawable.ic_app_white;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //TODO : Add support for stacked notifications(multiple Scanner events occur when the app is in background)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).
                setSmallIcon(getNotificationIcon()).
                setContentTitle("Scanner Control").
                setAutoCancel(true).
                setContentText(intent.getStringExtra(Constants.NOTIFICATIONS_TEXT));
        String notificationText = intent.getStringExtra(Constants.NOTIFICATIONS_TEXT);
        int notificationType = intent.getIntExtra(Constants.NOTIFICATIONS_TYPE,0);
        Intent resultIntent = new Intent(context, ScannersActivity.class);
        if(notificationType == Constants.BARCODE_RECEIVED){
            resultIntent = new Intent(context,ActiveScannerActivity.class);
            resultIntent.putExtra(Constants.SHOW_BARCODE_VIEW, true);
        }

        if(notificationType == Constants.SESSION_ESTABLISHED){
            resultIntent = new Intent(context,ActiveScannerActivity.class);
            resultIntent.putExtra(Constants.SHOW_BARCODE_VIEW, false);
        }

        if(notificationType == Constants.SESSION_TERMINATED){
            resultIntent = new Intent(context,HomeActivity.class);
        }

        if(notificationType == Constants.SCANNER_APPEARED){
            resultIntent = new Intent(context,HomeActivity.class);
        }

        if(notificationType == Constants.SCANNER_DISAPPEARED){
            resultIntent = new Intent(context,HomeActivity.class);
        }
        resultIntent.putExtra(Constants.SCANNER_ID,intent.getIntExtra(Constants.SCANNER_ID,-1));
        resultIntent.putExtra(Constants.SCANNER_NAME, Application.currentScannerName);
        resultIntent.putExtra(Constants.SCANNER_ADDRESS, Application.currentScannerAddress);
        resultIntent.putExtra(Constants.SCANNER_ID, Application.currentScannerId);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.putExtra(Constants.AUTO_RECONNECTION, Application.currentAutoReconnectionState);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(ActiveScannerActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        //Notify with the notification ID
        if(mgr!=null) mgr.notify(intent.getIntExtra(Constants.NOTIFICATIONS_ID, DEFAULT_NOTIFICATION_ID), mBuilder.build());
    }
}
