package com.zebra.scannercontrol.app.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.barcode.BarcodeTypes;
import com.zebra.scannercontrol.app.helpers.Barcode;

import java.util.ArrayList;

/**
 * Created by qvfr34 on 9/19/2014.
 */
public class BarcodeListAdapter extends ArrayAdapter<Barcode> {
    private final Activity context;
    private final ArrayList<Barcode> barcodes;

    public BarcodeListAdapter(Activity context,ArrayList<Barcode> barcodes) {
        super(context, R.layout.list_barcode_layout);
        this.context = context;
        this.barcodes = barcodes;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_barcode_layout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tv_barCode = (TextView) rowView.findViewById(R.id.txt_barcode_data);
            viewHolder.tv_barCodeType = (TextView) rowView.findViewById(R.id.txt_barcode_type);
            viewHolder.txtBarcodeCounter = (TextView) rowView.findViewById(R.id.txt_barcode_counter);
            viewHolder.txtBarcodeLength = (TextView) rowView.findViewById(R.id.txt_barcode_length);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Barcode barcode = barcodes.get(position);
        holder.tv_barCode.setText(new String(barcode.getBarcodeData()));
        holder.tv_barCodeType.setText(BarcodeTypes.getBarcodeTypeName(barcode.getBarcodeType()));
        holder.txtBarcodeCounter.setText(Integer.toString(position + 1) );
        holder.txtBarcodeLength.setText(" Characters = "+ Integer.toString(barcode.getBarcodeData().length));
        return rowView;
    }

    private static class ViewHolder {
        public TextView txtBarcodeCounter;
        public TextView tv_barCode;
        public TextView tv_barCodeType;
        public TextView txtBarcodeLength;
    }

    @Override
    public int getCount() {
        return barcodes.size();
    }
}
