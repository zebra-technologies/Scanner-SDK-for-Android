package com.zebra.scannercontrol.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDCConfig;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.application.Application;
import com.zebra.scannercontrol.app.helpers.Constants;
import com.zebra.scannercontrol.app.helpers.CustomProgressDialog;
import com.zebra.scannercontrol.app.helpers.ScannerAppEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_IMAGE_MODE;
import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_VIDEO_MODE;

public class ImageActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate, ScannerAppEngine.IScannerAppEngineDevEventsDelegate {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    public static Bitmap retrievedDecodeImage = null;
    int dialogResetSsaX = 50;
    int dialogResetSsaY = 170;
    int slowestImageGostImagePadding = 60;
    int imageViewFinderAttributr = 324;
    int enableAttribute = 1;
    int disableAttribute = 0;
    RadioGroup radioButtonGroup;
    RadioButton imageType;
    TextView imageTypeTextView;
    SeekBar seekBarVolume;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private ImageView SnapiImageView;
    private NavigationView navigationView;
    private int scannerID;
    private int scannerType;
    Button saveImageButton,videoMode;
    String imageSavedSuccessfully = "Image Successfully Saved into Download/Scanner Directory";
    String errorOccurred = "Error Occurred !";
    String errorOccurredImageNull = "Error Occurred ! retrievedDecodeImage is null";
    String errorNoPermission = "Error occurred ! permission denied ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.title_activity_image_video);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        setupUI();
        if(scannerType == 2)
        {
            videoMode.setEnabled(true);
        }
        else
        {
            setEnableViewFinder(false);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
        removeDevEventsDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);

    }

    private void setupUI()
    {
        SnapiImageView = (ImageView) findViewById(R.id.imgViewSnapiImage);

        imageTypeTextView =findViewById(R.id.imageTypeTextView);
        saveImageButton =findViewById(R.id.saveImageButton);
        radioButtonGroup = findViewById(R.id.radioButtonGroup);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        videoMode = findViewById(R.id.videoMode);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        scannerType = getIntent().getIntExtra(Constants.SCANNER_TYPE, -1);
        addDevConnectionsDelegate(this);
        addDevEventsDelegate(this);
    }

    public void imageAction(View view) {
        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
        StringBuilder sb = new StringBuilder();
        Application.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDK_DEVICE_IMAGE_MODE, inXML, sb, scannerID);
        setEnableImageFormat(true);
        saveImageButton.setEnabled(false);
        if(scannerType == 2)
        {
            setEnableViewFinder(true);
        }
        else
        {
            setEnableViewFinder(false);
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            disconnect(scannerID);
            Application.barcodeData.clear();
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            finish();
            intent = new Intent(ImageActivity.this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(this, ScannersActivity.class);

            startActivity(intent);
        } else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle(R.string.disconnect_current_scanner);
            dlg.setPositiveButton(R.string.continue_txt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    disconnect(scannerID);
                    Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    finish();
                    Intent intent = new Intent(ImageActivity.this, FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                }
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
    }

    public void onCheckboxViewFinder(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        IDCConfig idcConfig = new IDCConfig();
        String inXML;
        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checkBoxViewFinder:
                if (checked) {
                    inXML = "<inArgs>" +
                            "<scannerID>" +
                            getIntent().getIntExtra(Constants.SCANNER_ID, 0) +
                            "<scannerID>" +
                            "<cmdArgs>" +
                            "<arg-xml>" +
                            "<attrib_list>" +
                            "<attribute>" +
                            "<id>" + imageViewFinderAttributr + "</id>" +
                            "<datatype>F</datatype>" +
                            "<value>" + enableAttribute + "</value>" +
                            "</attribute>" +
                            "</attrib_list>" +
                            "</arg-xml>" +
                            "</cmdArgs>" +
                            "</inArgs>";
                } else {
                    inXML = "<inArgs>" +
                            "<scannerID>" +
                            getIntent().getIntExtra(Constants.SCANNER_ID, 0) +
                            "<scannerID>" +
                            "<cmdArgs>" +
                            "<arg-xml>" +
                            "<attrib_list>" +
                            "<attribute>" +
                            "<id>" + imageViewFinderAttributr + "</id>" +
                            "<datatype>F</datatype>" +
                            "<value>" + disableAttribute + "</value>" +
                            "</attribute>" +
                            "</attrib_list>" +
                            "</arg-xml>" +
                            "</cmdArgs>" +
                            "</inArgs>";
                }
                StringBuilder outXML = new StringBuilder();
                new MyAsyncTask(getIntent().getIntExtra(Constants.SCANNER_ID, 0), DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, outXML).execute(new String[]{inXML});
                break;
        }
    }

    // IScannerAppEngineDevConnectionsDelegate Events
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
        //pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        Application.barcodeData.clear();
        //pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        this.finish();
        Application.currentScannerId = Application.SCANNER_ID_NONE;
        Intent intent = new Intent(ImageActivity.this, HomeActivity.class);
        startActivity(intent);
        return true;
    }


    // IScannerAppEngineDevEventsDelegate Events
    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {
    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
    }

    @Override
    public void scannerImageEvent(byte[] imageData) {

        saveImageButton.setEnabled(true);
        SnapiImageView.setImageDrawable(null);
        final byte[] tempImgData = imageData;

        SnapiImageView.setPadding(0, 0, 0, 0);
        retrievedDecodeImage = BitmapFactory.decodeByteArray(tempImgData, 0, tempImgData.length);
        SnapiImageView.setImageBitmap(retrievedDecodeImage);


    }

    @Override
    public void scannerVideoEvent(byte[] videoData) {

        final byte[] tempImgData = videoData;
        SnapiImageView.setPadding(0, 0, 0, 0);
        retrievedDecodeImage = BitmapFactory.decodeByteArray(tempImgData, 0, tempImgData.length);
        SnapiImageView.setImageBitmap(retrievedDecodeImage);

        final byte[] tempVideoData = videoData;


    }


    public void saveImage(View view) {

        if (retrievedDecodeImage != null && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int os = android.os.Build.VERSION.SDK_INT;
            try {
                int selectedId = radioButtonGroup.getCheckedRadioButtonId();
                imageType = findViewById(selectedId);
                String mimeType;
                String imagetypetxt = (String) imageType.getText();
                if (imagetypetxt.equals("JPG")) {
                    mimeType = "image/jpeg";
                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    saveBitmap(this, retrievedDecodeImage, Bitmap.CompressFormat.JPEG, mimeType, "scannerImage" + fileName);
                    String message = imageSavedSuccessfully;
                    alertShow(message, false);
                } else if (imagetypetxt.equals("TIFF")) {
                    mimeType = "image/tiff";
                    String fileName = String.format("%d.tiff", System.currentTimeMillis());
                    saveBitmap(this, retrievedDecodeImage, Bitmap.CompressFormat.JPEG, mimeType, "scannerImage" + fileName);
                    String message = imageSavedSuccessfully;
                    alertShow(message, false);

                } else if (imagetypetxt.equals("BMP")) {
                    mimeType = "image/bmp";
                    String fileName = String.format("%d.bmp", System.currentTimeMillis());
                    saveBitmap(this, retrievedDecodeImage, Bitmap.CompressFormat.JPEG, mimeType, "scannerImage" + fileName);
                    String message = imageSavedSuccessfully;
                    alertShow(message, false);
                } else {
                    String message = errorOccurred;
                    alertShow(message, true);

                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (retrievedDecodeImage != null && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                int os = android.os.Build.VERSION.SDK_INT;


                if (ContextCompat.checkSelfPermission(ImageActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ImageActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(ImageActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted

                    saveImage();


                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String message = errorOccurredImageNull;
            alertShow(message, true);

        }
    }

    private void setEnableViewFinder(boolean enable){

        CheckBox checkBox =findViewById(R.id.checkBoxViewFinder);
        checkBox.setEnabled(enable);
        if(enable) {
            checkBox.setTextColor(getApplication().getResources().getColor(R.color.font_color));
        }else {
            checkBox.setTextColor(getApplication().getResources().getColor(R.color.inactive_text));
        }
    }

    private void setEnableImageFormat(boolean enable)
    {
        for (int i = 0; i < radioButtonGroup.getChildCount(); i++) {
            radioButtonGroup.getChildAt(i).setEnabled(enable);
        }
        if(enable) {
            imageTypeTextView.setTextColor(getApplication().getResources().getColor(R.color.font_color));
        }else {
            imageTypeTextView.setTextColor(getApplication().getResources().getColor(R.color.inactive_text));
        }
    }

    private void saveImage() {
        int selectedId = radioButtonGroup.getCheckedRadioButtonId();
        imageType = findViewById(selectedId);
        String imagetypetxt = (String) imageType.getText();
        FileOutputStream outStream = null;

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        try {


            if (imagetypetxt.equals("JPG")) {
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(directory, fileName);
                outStream = new FileOutputStream(outFile);
                retrievedDecodeImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                String message = imageSavedSuccessfully;
                alertShow(message, false);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);
            } else if (imagetypetxt.equals("TIFF")) {
                String fileName = String.format("%d.tiff", System.currentTimeMillis());
                File outFile = new File(directory, fileName);
                outStream = new FileOutputStream(outFile);
                retrievedDecodeImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                String message = imageSavedSuccessfully;
                alertShow(message, false);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);
            } else if (imagetypetxt.equals("BMP")) {
                String fileName = String.format("%d.bmp", System.currentTimeMillis());
                File outFile = new File(directory, fileName);
                outStream = new FileOutputStream(outFile);
                retrievedDecodeImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                String message = imageSavedSuccessfully;
                alertShow(message, false);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);
            } else {
                String message = errorOccurred;
                alertShow(message, true);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();


                } else {
                    String message = errorNoPermission;
                    alertShow(message, true);

                }
                return;
            }

        }
    }

    private void alertShow(String message, boolean error) {

        if (!error) {
            Toast.makeText(this, "Image saved to \'Pictures\' folder successfully!" ,Toast.LENGTH_SHORT).show();
            saveImageButton.setEnabled(false);
        } else {
            Toast.makeText(this, "Image Saved Failed!" , Toast.LENGTH_SHORT).show();
        }

    }

    private void saveBitmap(@NonNull final Context context, @NonNull final Bitmap bitmap,
                            @NonNull final Bitmap.CompressFormat format, @NonNull final String mimeType,
                            @NonNull final String displayName) throws IOException {
        final String relativeLocation = Environment.DIRECTORY_PICTURES;

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        final ContentResolver resolver = context.getContentResolver();

        OutputStream stream = null;
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, contentValues);

            if (uri == null) {
                throw new IOException("Failed to create new MediaStore record.");
            }

            stream = resolver.openOutputStream(uri);

            if (stream == null) {
                throw new IOException("Failed to get output stream.");
            }

            if (bitmap.compress(format, 95, stream) == false) {
                throw new IOException("Failed to save bitmap.");
            }

            String message = imageSavedSuccessfully;
            alertShow(message, false);
        } catch (IOException e) {
            if (uri != null) {
                resolver.delete(uri, null, null);
            }

            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void videoAction(View view) {

        String inXML = "<inArgs><scannerID>" + getIntent().getIntExtra(Constants.SCANNER_ID, 0) + "</scannerID></inArgs>";
        StringBuilder sb = new StringBuilder();
        boolean result = executeCommand(DCSSDK_DEVICE_VIDEO_MODE, inXML, sb, getIntent().getIntExtra(Constants.SCANNER_ID, 0));
        setEnableImageFormat(false);
        setEnableViewFinder(false);
        saveImageButton.setEnabled(false);
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        StringBuilder outXML;
        private CustomProgressDialog progressDialog;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
        }
    }


}
