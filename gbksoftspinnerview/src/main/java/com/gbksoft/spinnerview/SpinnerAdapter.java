package com.gbksoft.spinnerview;

import android.widget.BaseAdapter;

public abstract class SpinnerAdapter extends BaseAdapter {

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract String getItemString(int position);
}
