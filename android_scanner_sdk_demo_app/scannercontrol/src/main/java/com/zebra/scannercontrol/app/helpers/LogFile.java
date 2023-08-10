package com.zebra.scannercontrol.app.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

import com.zebra.scannercontrol.app.BuildConfig;
import com.zebra.scannercontrol.app.application.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *LogFile Class to save log events in Xml/Txt format from Sms Package Execution
 */
public class LogFile {
    public static Context context;
    public static int iLogFormat = 0;
    public static File logFilePath;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
    static SharedPreferences preferences;
    static String SMS_DIR = "/ZebraSMS";
    static String SMS_LOG_DIR = "/logs/";
    static String CUSTOM_SMS_DIR;
    public LogFile(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        iLogFormat = preferences.getInt(Constants.LOG_FORMAT,0);
    }

    /**
     * Creating log file based on user log format selection either xml or txt
     * initiateLogFile(...) will be triggered only from ExecuteSMSActivity
     * */
    public static void initiateLogFile(int iLogFormat, String sScannerID, String sModelNumber,
                                       String sSerialNumber, String sDOM, String sFirmware,
                                       String sConfigName, String sCommMode){
        File file = null;
        if(iLogFormat == 0){
            file = generateLogXmlFile(sScannerID, sModelNumber,
                    sSerialNumber, sDOM, sFirmware,
                    sConfigName, sCommMode);
        }else{
            file = generateLogTxtFile(sScannerID, sModelNumber,
                    sSerialNumber, sDOM, sFirmware,
                    sConfigName, sCommMode);
        }
        logFilePath = file;
    }

    /**
     * Creating log file based on user log format selection either xml or txt
     * initiateReceiverLogFile(...) will be triggered only from BroadcastReceiver
     * */
    public static void initiateReceiverLogFile(int iLogFormat){
        String currentDateandTime = "SMS_"+sdf.format(new Date());
        try{
            preferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            CUSTOM_SMS_DIR = preferences.getString(Constants.CUSTOM_SMS_DIR, "0");
            String rootPath = null;
            if(CUSTOM_SMS_DIR.equals("0")) {
                //default location
                rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + SMS_DIR + SMS_LOG_DIR;
            }else{
                //user selected location
                rootPath = Environment.getExternalStorageDirectory().getPath()+ CUSTOM_SMS_DIR + SMS_DIR + SMS_LOG_DIR;
            }
            if(iLogFormat == 0){
                File f = new File(rootPath + currentDateandTime+".xml");
                f.createNewFile();
                FileOutputStream outWriteXmlFile = new FileOutputStream(f);
                String sContent = "<firmware><start>SMS Execution Started. (SDK Version:"+
                        Application.sdkHandler.dcssdkGetVersion() +
                        ", SCA Version: "+ BuildConfig.VERSION_NAME+")</start>\n"+
                        "<firmware_update>\n"+
                        "\t<info>\n"+
                        "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                        "\t\t<message>Log File Initiated from SMSBroadcastReceiver</message>\n"+
                        "\t</info>\n";
                outWriteXmlFile.write(sContent.getBytes());
                outWriteXmlFile.flush();
                outWriteXmlFile.close();
                logFilePath = f;
            }else{
                File f = new File(rootPath + currentDateandTime+".txt");
                f.createNewFile();
                FileOutputStream outWriteTxtFile = new FileOutputStream(f);
                String sContent = sdf.format(new Date())+" SMS Execution Started. (SDK Version: "+Application.sdkHandler.dcssdkGetVersion()+", SCA Version: "+BuildConfig.VERSION_NAME+")";
                sContent = sContent+"\n"+sdf.format(new Date())+" Log File Initiated from SMSBroadcastReceiver"+"\n";
                outWriteTxtFile.write(sContent.getBytes());
                outWriteTxtFile.flush();
                outWriteTxtFile.close();
                logFilePath = f;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Creating xml log file
     * */
    public static File generateLogXmlFile(String sScannerID, String sModelNumber,
                                          String sSerialNumber, String sDOM, String sFirmware,
                                          String sConfigName, String sCommMode){
        try {
            String currentDateandTime = "SMS_"+sdf.format(new Date());
            preferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            CUSTOM_SMS_DIR = preferences.getString(Constants.CUSTOM_SMS_DIR, "0");
            String rootPath = null;
            if(CUSTOM_SMS_DIR.equals("0")) {
                //default location
                rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + SMS_DIR + SMS_LOG_DIR;
            }else{
                //user selected location
                rootPath = Environment.getExternalStorageDirectory().getPath()+ CUSTOM_SMS_DIR + SMS_DIR + SMS_LOG_DIR;
            }
            File f = new File(rootPath + currentDateandTime+".xml");
            f.createNewFile();
            FileOutputStream outWriteXmlFile = new FileOutputStream(f);
            String sContent = "<firmware><start>SMS Execution Started. (SDK Version:"+
                    Application.sdkHandler.dcssdkGetVersion() +
                    ", SCA Version: "+ BuildConfig.VERSION_NAME+")</start>\n"+
                    "<firmware_update>\n"+
                    "\t<info>\n"+
                    "\t\t<date>"+sdf.format(new Date())+"</date>\n"+
                    "\t\t<message>Scanner Assets</message>\n"+
                    "\t\t<id>"+sScannerID+"</id>\n"+
                    "\t\t<sn>"+sSerialNumber+"</sn>\n"+
                    "\t\t<model>"+sModelNumber+"</model>\n"+
                    "\t\t<dom>"+sDOM+"</dom>\n"+
                    "\t\t<firmware>"+sFirmware+"</firmware>\n"+
                    "\t\t<config_name>"+sConfigName+"</config_name>\n"+
                    "\t\t<protocol>"+sCommMode+"</protocol>\n"+
                    "\t</info>\n";
            outWriteXmlFile.write(sContent.getBytes());
            outWriteXmlFile.flush();
            outWriteXmlFile.close();
            Toast.makeText(context, "Log File Created "+currentDateandTime+".xml", Toast.LENGTH_SHORT).show();
            return f;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Creating Txt log file
     */
    public static File generateLogTxtFile(String sScannerID, String sModelNumber,
                                          String sSerialNumber, String sDOM, String sFirmware,
                                          String sConfigName, String sCommMode){
        try {
            String currentDateandTime = "SMS_"+sdf.format(new Date());
            preferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            CUSTOM_SMS_DIR = preferences.getString(Constants.CUSTOM_SMS_DIR, "0");
            String rootPath = null;
            if(CUSTOM_SMS_DIR.equals("0")) {
                //default location
                rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + SMS_DIR + SMS_LOG_DIR;
            }else{
                //user selected location
                rootPath = Environment.getExternalStorageDirectory().getPath()+ CUSTOM_SMS_DIR + SMS_DIR + SMS_LOG_DIR;
            }
            File f = new File(rootPath + currentDateandTime+".txt");
            f.createNewFile();
            FileOutputStream outWriteTxtFile = new FileOutputStream(f);
            String sContent = sdf.format(new Date())+" SMS Execution Started. (SDK Version: "+Application.sdkHandler.dcssdkGetVersion()+", SCA Version: "+BuildConfig.VERSION_NAME+")";
            sContent = sContent+"\n"+sdf.format(new Date())+" Scanner Assets ID:"+sScannerID+"; Model:"+sModelNumber+"; SN:"+sSerialNumber
                    +"; DoM:"+sDOM+"; Firmware:"+sFirmware+"; Config name:"+sConfigName+"; Protocol:"+sCommMode+"\n";
            outWriteTxtFile.write(sContent.getBytes());
            outWriteTxtFile.flush();
            outWriteTxtFile.close();
            Toast.makeText(context, "Log File Created "+currentDateandTime+".txt", Toast.LENGTH_SHORT).show();
            return f;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updating the log file with the sms package execution events and errors
     */
    public static void writeTxtOrXmlContentToFile(String contentMsg, File file) throws IOException {
        FileOutputStream outWriteFile = new FileOutputStream(file, true);
        outWriteFile.write(contentMsg.getBytes());
        outWriteFile.flush();
        outWriteFile.close();
    }

}
