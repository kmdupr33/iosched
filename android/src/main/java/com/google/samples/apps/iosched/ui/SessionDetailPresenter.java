package com.google.samples.apps.iosched.ui;

import android.content.Intent;
import android.net.Uri;

import com.google.samples.apps.iosched.service.SessionCalendarService;
import com.google.samples.apps.iosched.util.UIUtils;

public class SessionDetailPresenter {

    private final SessionDetailActivity mSessionDetailActivity;
    String mSessionId;
    Uri mSessionUri;
    long mSessionStart;
    long mSessionEnd;
    String mTitleString;
    boolean mStarred;
    boolean mInitStarred;

    public SessionDetailPresenter(SessionDetailActivity mSessionDetailActivity) {

        this.mSessionDetailActivity = mSessionDetailActivity;
    }

    public void onStop() {

        if (mInitStarred != mStarred) {
            if (UIUtils.getCurrentTime(mSessionDetailActivity) < mSessionStart) {
                // Update Calendar event through the Calendar API on Android 4.0 or new versions.
                Intent intent = null;
                if (mStarred) {
                    // Set up intent to add session to Calendar, if it doesn't exist already.
                    intent = new Intent(SessionCalendarService.ACTION_ADD_SESSION_CALENDAR,
                                        mSessionUri);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                                    mSessionStart);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                                    mSessionEnd);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_ROOM,
                                    mSessionDetailActivity.getRoomName());
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitleString);
                } else {
                    // Set up intent to remove session from Calendar, if exists.
                    intent = new Intent(SessionCalendarService.ACTION_REMOVE_SESSION_CALENDAR,
                                        mSessionUri);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                                    mSessionStart);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                                    mSessionEnd);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitleString);
                }
                intent.setClass(mSessionDetailActivity, SessionCalendarService.class);
                mSessionDetailActivity.startService(intent);

                if (mStarred) {
                    mSessionDetailActivity.setupNotification();
                }
            }
        }
    }
}