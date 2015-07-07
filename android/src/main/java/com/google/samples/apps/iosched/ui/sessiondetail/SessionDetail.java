package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.Config;

import java.util.List;

/**
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetail {
    private int mColor;
    private String mLiveStreamUrl;
    private long mSessionStart;
    private long mSessionEnd;
    private String mRoomName;
    private String mSpeakers;
    private String mPhotoUrl;
    private String mSessionUrl;
    private String mHashtag;
    private String mRoomId;
    private boolean mInMySchedule;
    private String mTagsString;
    private String mSessionAbstract;
    private String mRequirements;
    private List<String> mLinks;

    public int getColor() {
        return mColor;
    }

    public String getLiveStreamUrl() {
        return mLiveStreamUrl;
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

    public String getSpeakers() {
        return mSpeakers;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public String getHashtag() {
        return mHashtag;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public boolean isInMySchedule() {
        return mInMySchedule;
    }

    public String getTagsString() {
        return mTagsString;
    }

    public String getSessionAbstract() {
        return mSessionAbstract;
    }

    public String getSessionUrl() {
        return mSessionUrl;
    }

    public String getRequirements() {
        return mRequirements;
    }

    public boolean hasLiveStream() {
        return false;
    }

    public List<String> getLinks() {
        return mLinks;
    }


    public boolean isLiveStreamAvailableNow() {
        long currentTimeMillis = System.currentTimeMillis();
        return hasLiveStream()
                && currentTimeMillis > mSessionStart
                && currentTimeMillis <= mSessionEnd;
    }

    public boolean canGiveFeedbackNow() {
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis >= (mSessionEnd -
                Config.FEEDBACK_MILLIS_BEFORE_SESSION_END);
    }


    public boolean isKeynote() {
        return mTagsString.contains(Config.Tags.SPECIAL_KEYNOTE);
    }
}
