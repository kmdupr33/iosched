package com.google.samples.apps.iosched.util.rxadapter;

import android.support.v7.widget.SearchView;

/**
 *
 * Created by MattDupree on 6/8/15.
 */
public class DelegatingOnQueryTextListener implements SearchView.OnQueryTextListener {

    private OnQueryTextChangedListener mOnQueryTextChangedListener;
    private OnQueryTextSubmitListener mOnQueryTextSubmitListener;

    public void setOnQueryTextSubmitListener(
            OnQueryTextSubmitListener onQueryTextSubmitListener) {
        mOnQueryTextSubmitListener = onQueryTextSubmitListener;
    }

    public void setOnQueryTextChangedListener(OnQueryTextChangedListener onQueryTextChangedListener) {
        mOnQueryTextChangedListener = onQueryTextChangedListener;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (mOnQueryTextSubmitListener != null) {
            mOnQueryTextSubmitListener.onQueryTextSubmit(s);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (mOnQueryTextChangedListener != null) {
            mOnQueryTextChangedListener.onQueryTextChange(s);
        }
        return true;
    }

    public interface OnQueryTextChangedListener {
        void onQueryTextChange(String s);
    }

    public interface OnQueryTextSubmitListener {
        void onQueryTextSubmit(String s);
    }
}
