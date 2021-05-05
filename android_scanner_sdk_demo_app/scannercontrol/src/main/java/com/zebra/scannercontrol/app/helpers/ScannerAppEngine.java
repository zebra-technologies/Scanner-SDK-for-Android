package com.zebra.scannercontrol.app.helpers;

import android.content.Context;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;

import java.util.List;

/**
 * Interface to be implemented by classes which wish to communicate with the SDK
 */
public interface ScannerAppEngine{
    /**
     * Interface to notify about the change in scanner list due to (apperance/disappearance) of scanners
     */
    public interface IScannerAppEngineDevListDelegate {
        /**
         * Method to notify about the change in scanner list due to (apperance/disappearance) of scanners
         */
        boolean scannersListHasBeenUpdated();
    }

    /**
     * Interface to notify about the various states of a scanner
     */
    public interface IScannerAppEngineDevConnectionsDelegate {
        /**
         * Method to notify about the appearance of a scanner
         * @param scannerID ID of the scanner which has appeared
         * @return -
         */
        boolean scannerHasAppeared(int scannerID);

        /**
         * Method to notify about the disappearance of a scanner
         * @param scannerID ID of the scanner which has disappeared
         * @return -
         */
        boolean scannerHasDisappeared(int scannerID);

        /**
         * Method to notify about that connection has been established with a scanner
         * @param scannerID ID of the scanner with which a connection was established
         * @return -
         */
        boolean scannerHasConnected(int scannerID);

        /**
         * Method to notify about that connection has been terminated with a scanner
         * @param scannerID ID of the connected scanner which was disconnected
         * @return -
         */
        boolean scannerHasDisconnected(int scannerID);
    }

    /**
     * Interface to notify about events like barcode received etc
     */
    public interface IScannerAppEngineDevEventsDelegate {
        void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID);
        void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent);
        void scannerImageEvent(byte[] imageData);
        void scannerVideoEvent(byte[] videoData);
    }



    /**
     * Method to handle the initialization
     */
    void initializeDcsSdkWithAppSettings();

    /**
     * Utility function to display a message box(when app is in foreground)
     * @param message - Message to be displayed
     */
    void showMessageBox(String message);

    /**
     * Utility function to display a notification when the app is in background
     * @param text - Notification to be displayed
     * @return -
     */
    int showBackgroundNotification(String text);

    /**
     * Utility function to dismiss a background notification
     * @return -
     */
    int dismissBackgroundNotifications();

    /**
     * Utility function to know if an app is in background or not
     * @return -
     */
    boolean isInBackgroundMode(final Context context);


    /* API calls for UI View Controllers */
    /**
     * Method to remove a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be added
     */
    void addDevListDelegate(IScannerAppEngineDevListDelegate delegate);

    /**
     * Method to add a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be added
     */
    void addDevConnectionsDelegate(IScannerAppEngineDevConnectionsDelegate delegate);

    /**
     * Method to add a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be added
     */
    void addDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate);

    /**
     * Method to remove a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be removed
     */
    void removeDevListDelegate(IScannerAppEngineDevListDelegate delegate);

    /**
     * Method to remove a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be removed
     */
    void removeDevConnectiosDelegate(IScannerAppEngineDevConnectionsDelegate delegate);

    /**
     * Method to remove a {@link ScannerAppEngine.IScannerAppEngineDevListDelegate} from the list of delegates
     * @param delegate Delegate to be removed
     */
    void removeDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate);

    /**
     * Method to fetch the list of available scanners
     * @return List of scanners
     */
    List<DCSScannerInfo> getActualScannersList();

    /**
     * Method to fetch the info about a scanner
     * @param dev_index Index of the scanner in question
     * @return Scanner Info
     */
    DCSScannerInfo getScannerInfoByIdx(int dev_index);

    /**
     * Method to fetch the info about a scanner by it's ID
     * @param scannerId ID of the scanner of interest
     * @return Scanner Info
     */
    DCSScannerInfo getScannerByID(int scannerId);

    /**
     * Method to raise a notification
     */
    void raiseDeviceNotificationsIfNeeded();

    /**
     * Method to update the list of scanners from paired devices
     */
    void updateScannersList();

    /**
     * Method to initiate a connection to a scanner
     * @param scannerId ID of the scanner to which we want to connect
     */
    DCSSDKDefs.DCSSDK_RESULT connect(int scannerId);

    /**
     * Method to terminate a connection with a scanner
     * @param scannerId ID of the scanner with which the connection should be terminated
     */
    void disconnect(int scannerId);

    /**
     * Method to set the reconnection option for a scanner
     * @param scannerId ID of the scanner of interest
     * @param enable Enable/Disable auto reconnection
     */
    DCSSDKDefs.DCSSDK_RESULT setAutoReconnectOption(int scannerId, boolean enable);

    /**
     * Method to enable/disable discovery of scanners
     * @param enable enable/disable the discovery
     */
    void enableScannersDetection(boolean enable);

    /**
     * Method to enable/disable discovery of Bluetooth scanners
     * @param enable enable/disable the Bluetooth scanner discovery
     */
    void enableBluetoothScannerDiscovery(boolean enable);

    /**
     * Method to enable/disable 'scanner available' notifications
     * @param enable enable/disable the notifications
     */
    void configureNotificationAvailable(boolean enable);

    /**
     * Method to enable/disable 'scanner active' notifications
     * @param enable enable/disable the notifications
     */
    void configureNotificationActive(boolean enable);

    /**
     * Method to enable/disable 'barcode received' notifications
     * @param enable enable/disable the notifications
     */
    void configureNotificationBarcode(boolean enable);

    /**
     * Method to enable/disable 'image received' notifications
     * @param enable enable/disable the notifications
     */
    void configureNotificationImage(boolean enable);

    /**
     * Method to enable/disable 'video received' notifications
     * @param enable enable/disable the notifications
     */
    void configureNotificationVideo(boolean enable);

    /**
     * Method to configure the BT connection mode(if multiple modes are supported)
     * @param mode Mode to be used
     */
    void configureOperationalMode(DCSSDKDefs.DCSSDK_MODE mode);
    /**
     * Method to execute command for scanner
     * @param opCode operantional code to be used
     * @param inXML input xml to scanner
     * @param outXML output xml from scanner
     * @param scannerID id of scanner
     */
    boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID);

    /**
     * Method to execute command for scanner
     * @param opCode operantional code to be used
     * @param inXML input xml to scanner
     * @param outXML output xml from scanner
     * @param scannerID id of scanner
     */
    boolean executeSSICommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID);

}
