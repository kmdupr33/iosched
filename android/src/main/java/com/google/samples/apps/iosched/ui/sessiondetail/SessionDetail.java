package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.Config;

import java.util.List;

/**
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetail {

    public static class Builder {
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
        private String mSessionTitle;

        public Builder setColor(int color) {
            mColor = color;
            return this;
        }

        public Builder setLiveStreamUrl(String liveStreamUrl) {

            mLiveStreamUrl = liveStreamUrl;
            return this;
        }

        public Builder setSessionStart(long sessionStart) {

            mSessionStart = sessionStart;
            return this;
        }

        public Builder setSessionEnd(long sessionEnd) {

            mSessionEnd = sessionEnd;
            return this;
        }

        public Builder setRoomName(String roomName) {

            mRoomName = roomName;
            return this;
        }

        public Builder setSpeakers(String speakers) {

            mSpeakers = speakers;
            return this;
        }

        public Builder setPhotoUrl(String photoUrl) {

            mPhotoUrl = photoUrl;
            return this;
        }

        public Builder setSessionUrl(String sessionUrl) {

            mSessionUrl = sessionUrl;
            return this;
        }

        public Builder setHashtag(String hashtag) {

            mHashtag = hashtag;
            return this;
        }

        public Builder setRoomId(String roomId) {

            mRoomId = roomId;
            return this;
        }

        public Builder setInMySchedule(boolean inMySchedule) {

            mInMySchedule = inMySchedule;
            return this;
        }

        public Builder setTagsString(String tagsString) {

            mTagsString = tagsString;
            return this;
        }

        public Builder setSessionAbstract(String sessionAbstract) {

            mSessionAbstract = sessionAbstract;
            return this;
        }

        public Builder setRequirements(String requirements) {

            mRequirements = requirements;
            return this;
        }

        public Builder setLinks(List<String> links) {

            mLinks = links;
            return this;
        }

        public Builder setSessionTitle(String sessionTitle) {

            mSessionTitle = sessionTitle;
            return this;
        }
    }

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
    private String mSessionTitle;

    public SessionDetail(Builder builder) {
        mColor = builder.mColor;
        mLiveStreamUrl = builder.mLiveStreamUrl;
        mSessionStart = builder.mSessionStart;
        mSessionEnd = builder.mSessionEnd;
        mRoomName = builder.mRoomName;
        mSpeakers = builder.mSpeakers;
        mPhotoUrl = builder.mPhotoUrl;
        mSessionUrl = builder.mSessionUrl;
        mHashtag = builder.mHashtag;
        mRoomId = builder.mRoomId;
        mInMySchedule = builder.mInMySchedule;
        mTagsString = builder.mTagsString;
        mSessionAbstract = builder.mSessionAbstract;
        mRequirements = builder.mRequirements;
        mLinks = builder.mLinks;
        mSessionTitle = builder.mSessionTitle;
    }

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

    public String getSessionTitle() {

        return mSessionTitle;
    }
}
