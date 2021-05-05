package com.zebra.scannercontrol.app.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;


import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.ActiveScannerActivity;
import com.zebra.scannercontrol.app.activities.VirtualTetherSettings;
import com.zebra.scannercontrol.app.helpers.Constants;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Objects;

import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VIRTUAL_TETHER_ALARM_STATUS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdvancedFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AdvancedFragment extends Fragment {
    private View advancedFragmentView;
    boolean virtualTetherSupport;
    final int communicationModeClassic = 1;
    public static AdvancedFragment newInstance() {
        return new AdvancedFragment();
    }
    public AdvancedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        advancedFragmentView  = inflater.inflate(R.layout.fragment_advanced, container, false);

        final TextView virtualTetherAlarm = (TextView)advancedFragmentView.findViewById(R.id.txt_virtual_tether_setting);
        final TableRow virtualTetherSetting = (TableRow)advancedFragmentView.findViewById(R.id.tbl_row_virtual_tether);
        final ImageView rightNavIcon = (ImageView)advancedFragmentView.findViewById(R.id.right_nav_icon);

        final TextView imageVideoText = (TextView)advancedFragmentView.findViewById(R.id.txt_image_video);
        final TableRow imageVideoRow = (TableRow)advancedFragmentView.findViewById(R.id.tbl_row_image_video);
        final ImageView imageVideoRightNavIcon = (ImageView)advancedFragmentView.findViewById(R.id.image_video_right_nav_icon);

        virtualTetherSupport = ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).virtualTetheringSupported();
        if(virtualTetherSupport == true){
            virtualTetherAlarm.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
            virtualTetherSetting.setClickable(true);
            rightNavIcon.setColorFilter(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
        }else{
            virtualTetherAlarm.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.light_gray));
            virtualTetherSetting.setClickable(false);
            rightNavIcon.setColorFilter(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.light_gray));
        }
        if(((ActiveScannerActivity) Objects.requireNonNull(getActivity())).getScannerCommunicationMode() == communicationModeClassic)
        {
            imageVideoText.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
            imageVideoRow.setClickable(true);
            imageVideoRightNavIcon.setColorFilter(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.font_color));
        }
        else
        {
            imageVideoText.setTextColor(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.light_gray));
            imageVideoRow.setClickable(false);
            imageVideoRightNavIcon.setColorFilter(ContextCompat.getColor(((ActiveScannerActivity) getActivity()), R.color.light_gray));
        }
        return advancedFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume (){
        super.onResume();
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        super.onResume();
    }



}
