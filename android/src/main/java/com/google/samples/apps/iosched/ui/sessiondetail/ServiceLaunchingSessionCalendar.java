package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.content.Intent;

import com.google.samples.apps.iosched.service.SessionAlarmService;
import com.google.samples.apps.iosched.service.SessionCalendarService;
import com.google.samples.apps.iosched.util.UIUtils;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;

/**
 * {@link SessionCalendar} that simply delegates its work to the {@link SessionCalendarService} and
 * {@link SessionAlarmService}.
 *
 * Created by MattDupree on 7/16/15.
 */
public class ServiceLaunchingSessionCalendar implements SessionCalendar {

    private static final String TAG = ServiceLaunchingSessionCalendar.class.getSimpleName();
    private Context mContext;

    public ServiceLaunchingSessionCalendar(Context context) {
        mContext = context;
    }

    @Override
    public void addSession(SessionCalendarEntry sessionCalendarEntry) {
        // Set up intent to add session to Calendar, if it doesn't exist already.
        Intent intent = makeSessionCalendarIntent(SessionCalendarService.ACTION_ADD_SESSION_CALENDAR, sessionCalendarEntry);
        startSessionCalendarService(intent, mContext);
        setupNotification(sessionCalendarEntry, mContext);
    }

    @Override
    public void removeSession(SessionCalendarEntry sessionCalendarEntry) {
        // Set up intent to remove session from Calendar, if exists.
        Intent intent = makeSessionCalendarIntent(SessionCalendarService.ACTION_REMOVE_SESSION_CALENDAR,
                                   sessionCalendarEntry);
        startSessionCalendarService(intent, mContext);
    }

    //----------------------------------------------------------------------------------
    // Helpers
    //----------------------------------------------------------------------------------
    private void setupNotification(SessionCalendarEntry sessionCalendarEntry, Context context) {
        Intent scheduleIntent;

        // Schedule session notification
        if (UIUtils.getCurrentTime() < sessionCalendarEntry.getSessionStart()) {
            LOGD(TAG, "Scheduling notification about session start.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_STARRED_BLOCK,
                    null, context, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START, sessionCalendarEntry.getSessionStart());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END, sessionCalendarEntry.getSessionEnd());
            context.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling notification about session start, too late.");
        }

        // Schedule feedback notification
        if (UIUtils.getCurrentTime() < sessionCalendarEntry.getSessionEnd()) {
            LOGD(TAG, "Scheduling notification about session feedback.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_FEEDBACK_NOTIFICATION,
                    null, context, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_ID, sessionCalendarEntry.getSessionId());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START, sessionCalendarEntry.getSessionStart());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END, sessionCalendarEntry.getSessionEnd());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_TITLE, sessionCalendarEntry.getSessionTitle());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_ROOM, sessionCalendarEntry.getRoomName());
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_SPEAKERS, sessionCalendarEntry.getSessionSpeakers());
            context.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling feedback notification, too late.");
        }
    }

    private void startSessionCalendarService(Intent intent, Context context) {
        intent.setClass(context, SessionCalendarService.class);
        context.startService(intent);
    }

    private Intent makeSessionCalendarIntent(String actionAddSessionCalendar, SessionCalendarEntry sessionCalendarEntry) {
        Intent intent = new Intent(actionAddSessionCalendar, sessionCalendarEntry.getUri());
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                        sessionCalendarEntry.getSessionStart());
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                        sessionCalendarEntry.getSessionEnd());
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_ROOM, sessionCalendarEntry.getRoomName());
        intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, sessionCalendarEntry.getSessionTitle());
        return intent;
    }

}
