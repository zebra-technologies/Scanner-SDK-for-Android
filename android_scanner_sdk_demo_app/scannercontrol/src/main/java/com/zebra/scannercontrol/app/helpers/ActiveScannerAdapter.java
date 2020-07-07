package com.zebra.scannercontrol.app.helpers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.zebra.scannercontrol.app.fragments.BarcodeFargment;
import com.zebra.scannercontrol.app.fragments.AdvancedFragment;
import com.zebra.scannercontrol.app.fragments.SettingsFragment;

import static com.zebra.scannercontrol.app.helpers.Constants.DEBUG_TYPE.TYPE_DEBUG;

/**
 * Adapter to give the tabs for Active Scanner
 */
public class ActiveScannerAdapter extends FragmentStatePagerAdapter {
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    String[] tabs = {"Settings","Data View","Advanced"};
    /**
     * Constructor. Handles the initialization.
     * @param fm - Fragment Manager to be used for handling fragments.
     */
    public ActiveScannerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public CharSequence getPageTitle(int position) {

        return tabs[position];
    }

    /**
     * Return the Fragment associated with a specified position.
     * @param position - tab selected
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Constants.logAsMessage(TYPE_DEBUG, getClass().getSimpleName(), "1st Tab Selected");
                return SettingsFragment.newInstance();
            case 1:
                Constants.logAsMessage(TYPE_DEBUG, getClass().getSimpleName(), "2nd Tab Selected");
                return BarcodeFargment.newInstance();
            case 2:
                Constants.logAsMessage(TYPE_DEBUG, getClass().getSimpleName(), "3rd Tab Selected");
                return AdvancedFragment.newInstance();

            default:
                return null;
        }
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return 3;
    }
    @NonNull
    @Override
      public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
