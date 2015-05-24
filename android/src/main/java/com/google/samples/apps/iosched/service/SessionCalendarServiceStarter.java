package com.google.samples.apps.iosched.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailPresenter;

/**
 * Created by MattDupree on 5/24/15.
 */
public class SessionCalendarServiceStarter {

    private Context mContext;

    public SessionCalendarServiceStarter(Context context) {

        mContext = context;
    }

    public void startAddSessionService(Uri sessionUri, long sessionStart, long sessionEnd,
                                       String roomName, String titleString) {

        // Set up intent to add session to Calendar, if it doesn't exist already.
        Intent intent = new Intent(SessionCalendarService.ACTION_ADD_SESSION_CALENDAR,
                            sessionUri, mContext, SessionCalendarService.class);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                        sessionStart);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                        sessionEnd);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_ROOM,
                        roomName);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, titleString);
        mContext.startService(intent);
    }

    public void startRemoveSessionService(Uri sessionUri, long sessionStart,
                                          long sessionEnd, String titleString) {

        Intent intent = new Intent(SessionCalendarService.ACTION_REMOVE_SESSION_CALENDAR,
                            sessionUri);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                        sessionStart);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                        sessionEnd);
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, titleString);
        mContext.startService(intent);
    }
}
