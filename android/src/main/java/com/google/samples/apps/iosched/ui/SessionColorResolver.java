package com.google.samples.apps.iosched.ui;

import android.content.res.Resources;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.util.UIUtils;

/**
 * Created by MattDupree on 7/7/15.
 */
public class SessionColorResolver {

    private Resources mResources;

    public SessionColorResolver(Resources resources) {
        mResources = resources;
    }

    public int resolveSessionColor(int sessionColor) {
        if (sessionColor == 0) {
            // no color -- use default
            sessionColor = mResources.getColor(R.color.default_session_color);
        } else {
            // make sure it's opaque
            sessionColor = UIUtils.setColorAlpha(sessionColor, 255);
        }
        return sessionColor;
    }
}
