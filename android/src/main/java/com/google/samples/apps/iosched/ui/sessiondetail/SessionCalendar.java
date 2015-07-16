package com.google.samples.apps.iosched.ui.sessiondetail;

import android.net.Uri;

/**
 * Created by MattDupree on 7/16/15.
 */
public interface SessionCalendar {
    void addSession(SessionCalendarEntry sessionCalendarEntry);

    void removeSession(SessionCalendarEntry sessionCalendarEntry);

    class SessionCalendarEntry {
        private Uri mUri;
        private long mSessionStart;
        private long mSessionEnd;
        private String mRoomName;
        private String mSessionTitle;
        private String mSessionId;
        private String mSessionSpeakers;

        public static SessionCalendarEntry from(SessionDetail sessionDetail, Uri sessionUri) {
            return null;
        }

        public Uri getUri() {
            return mUri;
        }

        public long getSessionStart() {
            return mSessionStart;
        }

        public long getSessionEnd() {
            return mSessionEnd;
        }

        public String getRoomName() {
            return mRoomName;
        }

        public String getSessionTitle() {
            return mSessionTitle;
        }

        public String getSessionId() {
            return mSessionId;
        }

        public String getSessionSpeakers() {
            return mSessionSpeakers;
        }
    }
}
