package com.zebra.scannercontrol.app.fragments;

import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.ActiveScannerActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    /* Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
            * @return A new instance of fragment AdvancedFragment.
            */
    private View settingsFragmentView;
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settingsFragmentView  = inflater.inflate(R.layout.fragment_settings, container, false);
        SwitchCompat picklistMode = (SwitchCompat)settingsFragmentView.findViewById(R.id.switch_picklist_mode);
        final TextView txtPicklistMode = (TextView)settingsFragmentView.findViewById(R.id.txt_picklist_mode);
        if(picklistMode!=null) {
            int picklistInt = ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).getPickListMode();
            boolean picklistBool = false;
            if (picklistInt == 2) {
                picklistBool = true;
            }
            Log.i("PickListMode", "Setting "+picklistBool +" int value = "+picklistInt);
            picklistMode.setChecked(picklistBool);
            if(picklistBool){
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
            }else{
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.inactive_text));
            }
            picklistMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int picklistInt = 0;
                    if(isChecked){
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
                        picklistInt = 2;
                    }else{
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.inactive_text));
                    }
                    ((ActiveScannerActivity) getActivity()).setPickListMode(picklistInt);
                }
            });
        }

        final TextView txtVibration = (TextView)settingsFragmentView.findViewById(R.id.vibration_feedback_text);
        TableRow tblRowFW = (TableRow)settingsFragmentView.findViewById(R.id.vibration_feedback_tbl_row);

        if(txtVibration!=null && tblRowFW!=null) {
            boolean isPagerMotorAvailable= ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).isPagerMotorAvailable();
            if(isPagerMotorAvailable) {
                tblRowFW.setClickable(true);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
            }else{
                tblRowFW.setClickable(false);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.inactive_text));
            }
        }
//        SwitchCompat scanControl = (SwitchCompat)settingsFragmentView.findViewById(R.id.switch_scanning);
//        final TextView txtScanningControl = (TextView)settingsFragmentView.findViewById(R.id.txt_scanning_control);
//        if(scanControl!=null){
//            scanControl.setChecked(true);
//            scanControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if(isChecked) {
//                        ((ActiveScannerActivity) getActivity()).enableScanning(settingsFragmentView);
//                        txtScanningControl.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
//                    }else{
//                        ((ActiveScannerActivity) getActivity()).disableScanning(settingsFragmentView);
//                        txtScanningControl.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.inactive_text));
//                    }
//                }
//            });
//        }
        return settingsFragmentView;
    }

    @Override
    public void onResume (){
        super.onResume();
    }

}
