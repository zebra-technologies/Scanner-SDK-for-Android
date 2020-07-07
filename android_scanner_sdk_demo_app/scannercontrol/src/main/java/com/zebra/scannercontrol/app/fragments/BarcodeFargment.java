package com.zebra.scannercontrol.app.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.ActiveScannerActivity;
import com.zebra.scannercontrol.app.adapters.BarcodeListAdapter;
import com.zebra.scannercontrol.app.helpers.Barcode;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeFargment extends Fragment{
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdvancedFragment.
     */
    BarcodeListAdapter barcodeAdapter;
    ListView barcodesList;
    ArrayList<Barcode> barcodes;
    public static BarcodeFargment newInstance() {
        return new BarcodeFargment();
    }

    public BarcodeFargment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_barcode_fargment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        barcodes= ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).getBarcodeData( ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).getScannerID());
        barcodeAdapter = new BarcodeListAdapter(getActivity(),barcodes);

        barcodesList = (ListView) getActivity().findViewById(R.id.barcodesList);
        barcodesList.setAdapter(barcodeAdapter);
        Button btnClear = (Button) getActivity().findViewById(R.id.btnClearList);

        if(barcodes==null || barcodes.size()<=0) {
            btnClear.setEnabled(false);
        }
        if(barcodes.size()>0){
            btnClear.setEnabled(true);
        }

        barcodesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ((ActiveScannerActivity) getActivity()).updateBarcodeCount();
    }


    public void showBarCode(byte[] barcodeData, int barcodeType, int scannerID) {
        barcodes.add(new Barcode(barcodeData, barcodeType, scannerID));
        barcodeAdapter.add(new Barcode(barcodeData, barcodeType, scannerID));
        barcodeAdapter.notifyDataSetChanged();
        scrollListViewToBottom();
    }

    public void clearList(){
        barcodes.clear();
        barcodeAdapter.clear();
        barcodesList.setAdapter(barcodeAdapter);
        ((ActiveScannerActivity) Objects.requireNonNull(getActivity())).clearBarcodeData();
    }

    private void scrollListViewToBottom() {
        barcodesList.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                barcodesList.setSelection(barcodeAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
