package com.google.samples.apps.iosched.util.rxadapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by MattDupree on 6/8/15.
 */
public class DelegatingOnQueryTextListener implements SearchView.OnQueryTextListener {

    private final List<OnQueryTextListenerAdapter> mOnQueryTextListeners;

    public DelegatingOnQueryTextListener(
            @Nullable List<OnQueryTextListenerAdapter> onQueryTextListeners) {
        if (onQueryTextListeners != null) {
            mOnQueryTextListeners = onQueryTextListeners;
        } else {
            mOnQueryTextListeners = new ArrayList<>();
        }
    }

    public void addOnQueryTextListener(OnQueryTextListenerAdapter onQueryTextListener) {
        mOnQueryTextListeners.add(onQueryTextListener);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        for (OnQueryTextListenerAdapter listener : mOnQueryTextListeners) {
            listener.onQueryTextSubmit(s);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        for (OnQueryTextListenerAdapter listener : mOnQueryTextListeners) {
            listener.onQueryTextChange(s);
        }
        return true;
    }

    public static class OnQueryTextListenerAdapter {

        public void onQueryTextSubmit(String s) {
        }

        public void onQueryTextChange(String s) {
        }
    }
}
