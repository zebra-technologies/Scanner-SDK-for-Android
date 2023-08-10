package com.zebra.scannercontrol.app.receivers;

import static com.zebra.scannercontrol.app.helpers.LogFile.iLogFormat;
import static com.zebra.scannercontrol.app.helpers.LogFile.logFilePath;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.zebra.scannercontrol.app.activities.ActiveScannerActivity;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.LogFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class acts as a receiver when explicit intent are triggered when the app is running in the background.
 * This receiver helps us to automate the SMS Package execution process.
 * */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RECEIVER", "SMS Receiver started..."+new LogFile(context));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        if(Application.currentConnectedScannerID != -1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!Settings.canDrawOverlays(context)){
                    LogFile.initiateReceiverLogFile(iLogFormat);
                    try{
                        if(iLogFormat == 0) { //Xml
                            LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                                    "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                                    "\t\t<message> Device Overlay Permission Not Enabled </message>\n"+
                                    "\t</info>\n" +
                                    "</firmware_update>\n" +
                                    "\t<end>SMS Completed Successfully</end>\n"+
                                    "</firmware>", logFilePath);
                        }else {
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " Device Overlay Permission Not Enabled "+ "\n",logFilePath);
                            LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                        }
                        logFilePath = null;
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }else{
                    LogFile.initiateReceiverLogFile(iLogFormat);
                    Intent in = new Intent(context, ActiveScannerActivity.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    in.putExtra(Constants.IS_HANDLING_INTENT,true);
                    context.startActivity(in);
                }
            }else{
                LogFile.initiateReceiverLogFile(iLogFormat);
                Intent in = new Intent(context, ActiveScannerActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                in.putExtra(Constants.IS_HANDLING_INTENT,true);
                context.startActivity(in);
            }
        }else{
            LogFile.initiateReceiverLogFile(iLogFormat);
            try{
                if(iLogFormat == 0) { //Xml
                    LogFile.writeTxtOrXmlContentToFile("\t<info>\n"+
                            "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                            "\t\t<message> No Scanner Connected </message>\n"+
                            "\t</info>\n" +
                            "</firmware_update>\n" +
                            "\t<end>SMS Completed Successfully</end>\n"+
                            "</firmware>", logFilePath);
                }else {
                    LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " No Scanner Connected "+ "\n",logFilePath);
                    LogFile.writeTxtOrXmlContentToFile(""+sdf.format(new Date())+ " SMS Completed Successfully "+ "\n",logFilePath);
                }
                logFilePath = null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
