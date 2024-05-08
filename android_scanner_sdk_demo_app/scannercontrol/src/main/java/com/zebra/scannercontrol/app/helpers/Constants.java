package com.zebra.scannercontrol.app.helpers;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mfv347 on 6/20/2014.
 * <p/>
 * Helper class
 */
public class Constants {
    public static final String PREFS_NAME = "BarcodeScannerPrefs";

    //For Debugging
    public static final boolean DEBUG = true;
    public static final int COLOR_BG_GRAY = 0XF0F0F0;


    public enum DEBUG_TYPE {
        TYPE_DEBUG, TYPE_ERROR
    }

    //For shared prefs
    public static final String PREF_OPMODE = "MOT_SETTING_OPMODE";
    public static final String PREF_SCANNER_DETECTION = "MOT_SETTING_SCANNER_DETECTION";
    public static final String PREF_SCANNER_CONNECTION = "MOT_SETTING_SCANNER_CONNECTION";
    public static final String PREF_SCANNER_DISCOVERY = "MOT_SETTING_SCANNER_DISCOVERY";
    public static final String PREF_SCANNER_CLASSIC_FILTER = "MOT_SETTING_SCANNER_CLASSIC_FILTER";

    public static final String PREF_EVENT_ACTIVE = "MOT_SETTING_EVENT_ACTIVE";
    public static final String PREF_EVENT_AVAILABLE = "MOT_SETTING_EVENT_AVAILABLE";
    public static final String PREF_EVENT_BARCODE = "MOT_SETTING_EVENT_BARCODE";
    public static final String PREF_EVENT_IMAGE = "MOT_SETTING_EVENT_IMAGE";
    public static final String PREF_EVENT_VIDEO = "MOT_SETTING_EVENT_VIDEO";
    public static final String PREF_EVENT_BINARY_DATA = "MOT_SETTING_EVENT_BINARY_DATA";

    public static final String PREF_DONT_SHOW_INSTRUCTIONS = "MOT_SETTING_DONT_SHOW_MSG";
    public static final String PREF_DONT_SHOW_OVERLAY_INSTRUCTIONS = "MOT_SETTING_OVERLAY_DONT_SHOW_MSG";

    public static final String PREF_BT_ADDRESS = "MOT_SETTING_BT_ADDRESS";

    public static final String CUSTOM_SMS_DIR = "CUSTOM_SMS_DIR_PATH";
    public static final String PREF_NOTIFY_ACTIVE = "MOT_SETTING_NOTIFICATION_ACTIVE";
    public static final String PREF_NOTIFY_AVAILABLE = "MOT_SETTING_NOTIFICATION_AVAILABLE";
    public static final String PREF_NOTIFY_BARCODE = "MOT_SETTING_NOTIFICATION_BARCODE";
    public static final String PREF_NOTIFY_IMAGE = "MOT_SETTING_NOTIFICATION_IMAGE";
    public static final String PREF_NOTIFY_VIDEO = "MOT_SETTING_NOTIFICATION_VIDEO";
    public static final String PREF_NOTIFY_BINARY_DATA = "MOT_SETTING_NOTIFICATION_BINARY_DATA";

    public static final String PREF_PAIRING_BARCODE_TYPE = "MOT_SETTING_PAIRING_BARCODE_TYPE";
    public static final String PREF_PAIRING_BARCODE_CONFIG = "MOT_SETTING_PAIRING_BARCODE_CONFIG";
    public static final String PREF_COMMUNICATION_PROTOCOL_TYPE = "MOT_SETTING_COMMUNICATION_PROTOCOL_TYPE";
    public static final int COMMUNICATION_PROTOCOL_TYPE_BTLE = 0;
    public static final int COMMUNICATION_PROTOCOL_TYPE_CLASSIC = 1;
    //Data related to notifications
    public static final String NOTIFICATIONS_TYPE = "notifications_type";
    public static final String NOTIFICATIONS_TEXT = "notifications_text";
    public static final String NOTIFICATIONS_ID = "notifications_id";

    //Action strings for various RFID Events
    public static final String ACTION_SCANNER_CONNECTED = "com.zebra.scannercontrol.connected";
    public static final String ACTION_SCANNER_DISCONNECTED = "com.zebra.scannercontrol.disconnected";
    public static final String ACTION_SCANNER_AVAILABLE = "com.zebra.scannercontrol.available";
    public static final String ACTION_SCANNER_CONN_FAILED = "com.zebra.scannercontrol.conn.failed";
    public static final String ACTION_SCANNER_BARCODE_RECEIVED = "com.zebra.scannercontrol.barcode.received";
    public static final String ACTION_SCANNER_IMAGE_RECEIVED = "com.zebra.scannercontrol.image.received";
    public static final String ACTION_SCANNER_VIDEO_RECEIVED = "com.zebra.scannercontrol.video.received";

    //Data regarding bluetooth
    public static final String DATA_BLUETOOTH_DEVICE = "com.zebra.scannercontrol.data.bluetooth.device";

    //Virtual tether
    public static final String PREF_VIRTUAL_TETHER_SCANNER_SETTINGS = "MOT_VIRTUAL_TETHER_SCANNER_SETTINGS";
    public static final String PREF_VIRTUAL_TETHER_HOST_FEEDBACK = "MOT_VIRTUAL_TETHER_HOST_FEEDBACK";
    public static final String PREF_VIRTUAL_TETHER_HOST_VIBRATION_ALARM = "MOT_VIRTUAL_TETHER_HOST_VIBRATION_ALARM";
    public static final String PREF_VIRTUAL_TETHER_HOST_AUDIO_ALARM = "MOT_VIRTUAL_TETHER_HOST_AUDIO_ALARM";
    public static final String PREF_VIRTUAL_TETHER_HOST_POPUP_MESSAGE = "MOT_VIRTUAL_TETHER_HOST_POPUP_MESSAGE";
    public static final String VIRTUAL_TETHER_HOST_BACKGROUND_MODE_NOTIFICATION = "Zebra Virtual Tether alarm activated";
    public static final String PREF_VIRTUAL_TETHER_HOST_SCREEN_FLASH = "MOT_VIRTUAL_TETHER_HOST_SCREEN_FLASH";
    public static final int VIRTUAL_TETHER_HOST_NOTIFICATION_CHANNEL_ID = 1111;
    public static final String VIRTUAL_TETHER_EVENT_NOTIFY = "intent_virtual_tether_event_notify";
    public static final String VIRTUAL_TETHER_SCANNER_ENABLE_VALUE = "1";
    public static final long[] VIRTUAL_TETHER_AUDIO_ALARM_PATTERN = {1500, 800, 800, 800};


    public static final String PREF_VIRTUAL_TETHER_HOST_BACKGROUND_COLOR  = "backgroundColor";
    public static final String LOG_FORMAT = "LogFormat";
    public static final String XML = ".xml";
    public static final String TXT = ".txt";
    public static final int VIRTUAL_TETHER_HOST_ANIMATION_DURATION = 1000;


    //Handling automatic Intent
    public static final String IS_HANDLING_INTENT = "intent_handling";
    public static final String LOG_FILE_PATH  = "intent_log_file_path";
    public static final String IS_DISPLAY_OVERLAY_DIALOG = "intent_display_overlay_dialog";


    //Intent Data
    public static final String INTENT_ACTION = "intent_action";
    public static final String INTENT_DATA = "intent_data";

    //Config Details Intent Data
    public static final String CONFIG_NAME = "intent_config_name";
    public static final String CONFIG_DESC = "intent_config_desc";
    public static final String CONFIG_VALUE = "intent_config_value";
    public static final String CONFIG_TITLE = "intent_config_title";
    public static final String CONFIG_MSG = "intent_config_message";
    public static final String LAUNCH_FROM_FCS = "launch_from_fcs";
    //Available Scanners Data
    public static final String SCANNER_NAME = "avail_scanner_name";
    public static final String SCANNER_TYPE = "avail_scanner_type";
    public static final String SCANNER_ADDRESS = "avail_scanner_address";
    public static final String SCANNER_ID = "active_scanner_id";
    public static final String SELECTED_BARCODE_SSA = "selected_barcode_ssa";
    public static final String AUTO_RECONNECTION = "auto_reconnection";
    public static final String PICKLIST_MODE = "picklist+mode";
    public static final String PAGER_MOTOR_STATUS = "pager_motor_status";
    public static final String CONNECTED = "connected";
    public static final String SHOW_BARCODE_VIEW = "barcode_view";
    public static final String FW_REBOOT = "fw_reboot";
    public static final String BATTERY_STATUS = "battery_status";
    public static final String SYMBOLOGY_SSA = "symbology_ssa";
    public static final String SYMBOLOGY_SSA_ENABLED = "symbology_ssa_enabled";
    public static final String SYMBOLOGY_SSA_UNSUPPORTED = "symbology_ssa_unsupported";
    public static final String SYMBOLOGY_SSA_STR = "symbology_ssa_str";
    public static final String BEEPER_VOLUME = "beeper_volume";
    public static final String SSA_STATUS = "ssa_status";
    public static final String SCALE_STATUS = "scale_status";


    public static final String CONNECTION_HELP_TYPE = "connection_help";
    public static final int CONNECTION_HELP_TYPE_CS4070 = 0;
    public static final int CONNECTION_HELP_TYPE_LI4278 = 1;
    public static final int CONNECTION_HELP_TYPE_RFD8500 = 2;

    public static final String CONNECTION_HELP_CS4070_RESET_DEFAULTS = "Reset Factory Defaults";
    public static final String CONNECTION_HELP_CS4070_SSI_PROFILE = "Bluetooth SSI Profile";
    public static final String CONNECTION_HELP_LI4278_SET_DEFAULTS = "Set Factory Defaults";
    public static final String CONNECTION_HELP_LI4278_SSI_HOST_SERVER = "SSI Host Server";

    //Error Messages
    public static final String INVALID_SCANNER_ID_MSG = "Invalid Scanner ID";

    //Type of data recieved

    public static final int BARCODE_RECEIVED = 30;
    public static final int SESSION_ESTABLISHED = 31;
    public static final int SESSION_TERMINATED = 32;
    public static final int SCANNER_APPEARED = 33;
    public static final int SCANNER_DISAPPEARED = 34;
    public static final int FW_UPDATE_EVENT = 35;
    public static final int AUX_SCANNER_CONNECTED = 36;
    public static final int IMAGE_RECEIVED = 37;
    public static final int VIDEO_RECEIVED = 38;
    public static final int CONFIG_UPDATE_EVENT = 39;

    ///---
    public static final String BTH_SCAN_TO_CONNECT = "[BTH_CONNECT]";

    //Xml tags
    public static final String XMLTAG_SCANNER_ID = "<scannerID>";
    public static final String XMLTAG_ARGXML = "<inArgs>";

    //Weight status
    public static final String WEIGHT_XML_ELEMENT = "weight";
    public static final String WEIGHT_MODE_XML_ELEMENT = "weight_mode";
    public static final String WEIGHT_STATUS_XML_ELEMENT = "status";

    public static final String SCALE_STATUS_SCALE_NOT_ENABLED = "Scale Not Enabled";
    public static final String SCALE_STATUS_SCALE_NOT_READY = "Scale Not Ready";
    public static final String SCALE_STATUS_STABLE_WEIGHT_OVER_LIMIT = "Stable Weight OverLimit";
    public static final String SCALE_STATUS_STABLE_WEIGHT_UNDER_ZERO = "Stable Weight Under Zero";
    public static final String SCALE_STATUS_NON_STABLE_WEIGHT = "Non Stable Weight";
    public static final String SCALE_STATUS_STABLE_ZERO_WEIGHT = "Stable Zero Weight";
    public static final String SCALE_STATUS_STABLE_NON_ZERO_WEIGHT = "Stable NonZero Weight";

    //Scan speed analytics
    public static final String SCAN_SPEED_ANALYTICS_BTLE_IMAGE_NOT_SUPPORT_MESSAGE_TITLE = "Image not supported";
    public static final String SCAN_SPEED_ANALYTICS_BTLE_IMAGE_NOT_SUPPORT_MESSAGE_CONTENT = "Image feature not supported in Bluetooth Low Energy mode";

    //Beacon filters
    public static final String PREF_BCON_FILTER_MODEL_NUMBER = "PREF_BCON_FILTER_MODEL_NUMBER";
    public static final String PREF_BCON_FILTER_SERIAL_NUMBER = "PREF_BCON_FILTER_SERIAL_NUMBER";
    public static final String PREF_BCON_FILTER_RSSI_P_REF = "PREF_BCON_FILTER_RSSI_P_REF";
    public static final String PREF_BCON_FILTER_BATTERY_PERCENTAGE_CONDITION = "PREF_BCON_FILTER_BATTERY_PERCENTAGE_CONDITION";
    public static final String PREF_BCON_FILTER_BATTERY_PERCENTAGE = "PREF_BCON_FILTER_BATTERY_PERCENTAGE";
    public static final String PREF_BCON_FILTER_BATTERY_CHARGE_STATUS = "PREF_BCON_FILTER_BATTERY_CHARGE_STATUS";
    public static final String PREF_BCON_FILTER_IN_MOTION = "PREF_BCON_FILTER_IN_MOTION";
    public static final String PREF_BCON_FILTER_IN_CRADLE= "PREF_BCON_FILTER_IN_CRADLE";
    public static final String PREF_BCON_FILTER_IS_CONNECTED= "PREF_BCON_FILTER_IS_CONNECTED";
    public static final String PREF_BCON_FILTER_VIRTUAL_TETHER= "PREF_BCON_FILTER_VIRTUAL_TETHER";
    public static final String PREF_BCON_FILTER_PRODUCT_RELEASE_NAME= "PREF_BCON_FILTER_PRODUCT_RELEASE_NAME";
    public static final String PREF_BCON_FILTER_DOM_CONDITION= "PREF_BCON_FILTER_DOM_CONDITION";
    public static final String PREF_BCON_FILTER_DOM= "PREF_BCON_FILTER_DOM";
    public static final String PREF_BCON_FILTER_CONFIG_FILE_NAME= "PREF_BCON_FILTER_CONFIG_FILE_NAME";
    public static final String DATE_FORMAT = "dd MMM yyyy";

    public static final String BCON_FILTER_SELECTOR_ALL = "All";

    public static final String BCON_FILTER_SELECTOR_EQUALS = "Equals";

    public static final String BCON_FILTER_SELECTOR_TO_DATE = "To Date";

    public static final String BCON_FILTER_SELECTOR_FROM_DATE = "From Date";

    public static final String  BCON_FILTER_SELECTOR_BETWEEN = "Between";

    public static final String BCON_FILTER_SELECTOR_GRATER_THAN = "Grater than";

    public static final String BCON_FILTER_SELECTOR_LESS_THAN = "Less than";

    public static final String BCON_FILTER_SELECTOR_YES = "Yes";

    public static final String BCON_FILTER_SELECTOR_NO = "No";

    public static final String BCON_FILTER_SELECTOR_ON = "On";

    public static final String BCON_FILTER_SELECTOR_OFF = "Off";

    public static final List<String> SPINNER_DATA_DATE = Collections.unmodifiableList(Arrays.asList(BCON_FILTER_SELECTOR_ALL,BCON_FILTER_SELECTOR_EQUALS, BCON_FILTER_SELECTOR_TO_DATE,BCON_FILTER_SELECTOR_FROM_DATE,BCON_FILTER_SELECTOR_BETWEEN));

    public static final List<String> SPINNER_DATA_BATTERY = Collections.unmodifiableList(Arrays.asList(BCON_FILTER_SELECTOR_ALL,BCON_FILTER_SELECTOR_EQUALS, BCON_FILTER_SELECTOR_GRATER_THAN,BCON_FILTER_SELECTOR_LESS_THAN,BCON_FILTER_SELECTOR_BETWEEN));


    public static final List<String> SPINNER_DATA_YES_NO = Collections.unmodifiableList(Arrays.asList(BCON_FILTER_SELECTOR_ALL, BCON_FILTER_SELECTOR_YES, BCON_FILTER_SELECTOR_NO));

    public static final List<String> SPINNER_DATA_ON_OFF = Collections.unmodifiableList(Arrays.asList(BCON_FILTER_SELECTOR_ALL, BCON_FILTER_SELECTOR_ON, BCON_FILTER_SELECTOR_OFF));


    //Scanner models
    public static final String SCANNER_MODEL_CS4070 = "CS4070";

    /**
     * Method to be used throughout the app for logging debug messages
     *
     * @param type    - One of TYPE_ERROR or TYPE_DEBUG
     * @param TAG     - Simple String indicating the origin of the message
     * @param message - Message to be logged
     */
    public static void logAsMessage(DEBUG_TYPE type, String TAG, String message) {
        if (DEBUG) {
            if (type == DEBUG_TYPE.TYPE_DEBUG)
                Log.d(TAG, message);
            else if (type == DEBUG_TYPE.TYPE_ERROR)
                Log.e(TAG, message);
        }
    }
}
