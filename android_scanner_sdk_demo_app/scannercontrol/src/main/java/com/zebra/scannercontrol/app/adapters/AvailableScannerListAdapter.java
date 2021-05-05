package com.zebra.scannercontrol.app.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.helpers.AvailableScanner;
import com.zebra.scannercontrol.app.helpers.Barcode;

import java.util.ArrayList;

/**
 * Created by pndv47 on 7/5/2016.
 */
public class AvailableScannerListAdapter extends ArrayAdapter<Barcode> {
    private final Activity context;
    private final ArrayList<AvailableScanner> availableScanners;
    public static final float defaultTextSize = 12f;

    public AvailableScannerListAdapter(Activity context,ArrayList<AvailableScanner> availableScanners) {
        super(context, R.layout.list_available_device_layout);
        this.context = context;
        this.availableScanners = availableScanners;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_available_device_layout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.txtScannerName = (TextView) rowView.findViewById(R.id.txt_scanner_name);
            viewHolder.txtScannerHWSerial = (TextView) rowView.findViewById(R.id.txt_scanner_hw_serial);
            viewHolder.isConnected = (CheckBox) rowView.findViewById(R.id.chk_connected);
            viewHolder.typeIcon = (ImageView) rowView.findViewById(R.id.type_icon);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        AvailableScanner scanner = availableScanners.get(position);
        holder.isConnected.setChecked(scanner.isConnected());
        holder.txtScannerName.setText(scanner.getScannerName());
        holder.txtScannerHWSerial.setText(scanner.getScannerAddress());

        if(scanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_NORMAL || scanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_LE) {
            if(scanner.isConnected()){
                holder.typeIcon.setImageResource(R.drawable.ic_action_bluetooth_connected);
            }else {
                holder.typeIcon.setImageResource(R.drawable.ic_action_bluetooth);
            }
            holder.txtScannerHWSerial.setTextSize(15);
        }else if(scanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI){
            holder.typeIcon.setImageResource(R.drawable.ic_action_usb);
            holder.txtScannerHWSerial.setTextSize(12);
        }else if(scanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_CDC){
            holder.typeIcon.setImageResource(R.drawable.ic_action_usb_cdc);
            holder.txtScannerHWSerial.setTextSize(defaultTextSize);
        }

        if(!scanner.isConnectable()){
            holder.txtScannerName.setTextColor(ContextCompat.getColor(context, R.color.inactive_text));
            holder.txtScannerHWSerial.setTextColor(ContextCompat.getColor(context, R.color.inactive_text));
        }else{
            holder.txtScannerName.setTextColor(ContextCompat.getColor(context, R.color.font_color));
            holder.txtScannerHWSerial.setTextColor(ContextCompat.getColor(context, R.color.font_color));
        }
        return rowView;
    }

    private static class ViewHolder {
        public CheckBox isConnected;
        public TextView txtScannerName;
        public TextView txtScannerHWSerial;
        public ImageView typeIcon;
    }

    @Override
    public int getCount() {
        return availableScanners.size();
    }
}
