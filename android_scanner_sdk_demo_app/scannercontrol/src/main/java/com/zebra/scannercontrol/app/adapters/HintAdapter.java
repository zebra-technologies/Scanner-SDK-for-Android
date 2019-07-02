package com.zebra.scannercontrol.app.adapters;

import android.content.Context;
import android.util.StringBuilderPrinter;
import android.widget.ArrayAdapter;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.helpers.SSASymbologyType;

import java.util.List;
import java.util.Objects;

/**
 * Created by BPallewela on 11/29/2017.
 */

public class HintAdapter extends ArrayAdapter<SSASymbologyType> {

    public HintAdapter(Context theContext, List<SSASymbologyType> objects, int theLayoutResId) {
        super(theContext, theLayoutResId, objects);
    }

    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = super.getCount();
        return count > 0 ? count : count;
    }
}
