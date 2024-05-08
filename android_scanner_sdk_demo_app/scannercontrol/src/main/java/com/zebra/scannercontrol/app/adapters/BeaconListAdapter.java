package com.zebra.scannercontrol.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.BeaconActivity;
import com.zebra.scannercontrol.beacon.BeaconManager;
import com.zebra.scannercontrol.beacon.entities.ZebraBeacon;

import java.util.ArrayList;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.BeaconViewHolder>{
    private ArrayList<ZebraBeacon> beaconList;
    private Context context;
    private static int currentPosition = 0;

    public BeaconListAdapter(ArrayList<ZebraBeacon> beaconList, Context context){
        this.beaconList = beaconList;
        this.context = context;
    }

    @NonNull
    @Override
    public BeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_item, parent, false);
        return new BeaconViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BeaconViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ZebraBeacon zebraBeacon = beaconList.get(position);

        holder.itemView.setTag(zebraBeacon.getzSerialNumber());

        holder.tvBeaconName.setText(zebraBeacon.getzModelNumber() + " - " +zebraBeacon.getzSerialNumber());
        holder.tvBattery.setText(zebraBeacon.getzBatteryPercentage() + "%");
        holder.tvIsConnected.setText(zebraBeacon.getzConnected());
        holder.tvModel.setText(zebraBeacon.getzModelNumber());
        holder.tvSerialNumber.setText(zebraBeacon.getzSerialNumber());
        holder.tvCompanyID.setText(zebraBeacon.getzCompanyId());
        holder.tvZebraBeaconCode.setText(zebraBeacon.getzBeaconCode());
        holder.tvRSSI.setText(zebraBeacon.getzRSSIPowerReference());
        holder.tvBatteryInner.setText(zebraBeacon.getzBatteryPercentage() + "%");
        holder.tvInMotion.setText(zebraBeacon.getzInMotion());
        holder.tvBatteryStatus.setText(zebraBeacon.getzBatteryChargeStatus());
        holder.tvCradle.setText(zebraBeacon.getzInCradle());
        holder.tvVirtualTetherAlarm.setText(zebraBeacon.getzVirtualTetherAlarm());
        holder.tvProdRelName.setText(zebraBeacon.getzProductReleaseName());
        holder.tvDOM.setText(zebraBeacon.getzDateOfManufacture());
        holder.tvConfigFile.setText(zebraBeacon.getzConfigFileName());
        holder.tvConnected.setText(zebraBeacon.getzConnected());
        if(zebraBeacon.getzConnected().equalsIgnoreCase(BeaconManager.BEACON_FILTER_NO)) {
            holder.imgConnectionState.setImageResource(R.drawable.connection_status_no);
        }else{
            holder.imgConnectionState.setImageResource(R.drawable.connection_status_yes);
        }

        if (currentPosition != position){
            Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.collaps_anim);
            holder.linearLayout.startAnimation(slideUp);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //do something
                    holder.linearLayout.setVisibility(View.GONE);
                }
            }, 480);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {

                //getting the position of the item to expand it
                currentPosition = position;

                if(view.findViewById(R.id.linearLayout).getVisibility()!=View.VISIBLE){

                    Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.expand_anim);
                        //toggling visibility
                    view.findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                        //adding sliding effect
                    view.findViewById(R.id.linearLayout).startAnimation(slideDown);
                }else{
                    Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.collaps_anim);
                    view.findViewById(R.id.linearLayout).startAnimation(slideUp);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //do something
                            view.findViewById(R.id.linearLayout).setVisibility(View.GONE);
                        }
                    }, 480);
                }

                //reloding the list
                notifyDataSetChanged();

            }
        });

    }

    @Override
    public int getItemCount() {
        if(beaconList!= null) {
            return beaconList.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class BeaconViewHolder extends RecyclerView.ViewHolder{
        TextView tvBeaconName, tvBattery, tvIsConnected, tvModel, tvSerialNumber, tvCompanyID,
                tvZebraBeaconCode, tvRSSI, tvBatteryInner, tvInMotion, tvBatteryStatus, tvCradle,
                tvVirtualTetherAlarm, tvProdRelName, tvDOM, tvConfigFile, tvConnected;
        LinearLayout linearLayout;
        ImageView imgConnectionState;
        public BeaconViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBeaconName = itemView.findViewById(R.id.tvBeaconName);
            tvBattery = itemView.findViewById(R.id.tvBattery);
            tvIsConnected = itemView.findViewById(R.id.tvIsConnected);
            tvModel = itemView.findViewById(R.id.tvModel);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvCompanyID = itemView.findViewById(R.id.tvCompanyID);
            tvZebraBeaconCode = itemView.findViewById(R.id.tvZebraBeaconCode);
            tvRSSI = itemView.findViewById(R.id.tvRSSI);
            tvBatteryInner = itemView.findViewById(R.id.tvBatteryInner);
            tvInMotion = itemView.findViewById(R.id.tvInMotion);
            tvBatteryStatus = itemView.findViewById(R.id.tvBatteryStatus);
            tvCradle = itemView.findViewById(R.id.tvCradle);
            tvVirtualTetherAlarm = itemView.findViewById(R.id.tvVirtualTetherAlarm);
            tvProdRelName = itemView.findViewById(R.id.tvProdRelName);
            tvDOM = itemView.findViewById(R.id.tvDOM);
            tvConfigFile = itemView.findViewById(R.id.tvConfigFile);
            tvConnected = itemView.findViewById(R.id.tvConnected);
            imgConnectionState = itemView.findViewById(R.id.imgConnectionState);

            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}
