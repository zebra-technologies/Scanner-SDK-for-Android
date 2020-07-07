package com.zebra.scannercontrol.app.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.scannercontrol.app.R;

/**
 * Created by MFV347 on 6/24/2014.
 *
 * Adapter for the list view in HomeScreen
 */
public class HomeListAdapter extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] items;

    public HomeListAdapter(Activity context, String[] items) {
        super(context, R.layout.list_home_layout, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_home_layout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.homeItemText);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        String s = items[position];
        holder.text.setText(s);

        return rowView;
    }

    private static class ViewHolder {
        public TextView text;
        public ImageView image;
    }
}
