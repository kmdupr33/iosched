package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.widget.TextView;

import com.google.samples.apps.iosched.util.UIUtils;

/**
 *
 * Created by MattDupree on 5/31/15.
 */
public class SessionSubtitlePresenter {

    private final Context mContext;
    private TextView mSubtitle;

    public SessionSubtitlePresenter(TextView subtitle) {
        mSubtitle = subtitle;
        mContext = mSubtitle.getContext();
    }

    public void presentSubtitle(long start, long end, String roomName,
                                boolean liveStreamUrl) {
        String subtitle = UIUtils.formatSessionSubtitle(start, end, roomName,
                                                        new StringBuilder(), mContext);
        if (liveStreamUrl) {
            subtitle += " " + UIUtils.getLiveBadgeText(mContext, start,
                                                       end);
        }
        mSubtitle.setText(subtitle);
    }
}
