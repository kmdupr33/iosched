package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.res.Resources;
import android.text.TextUtils;

import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.SessionColorResolver;
import com.google.samples.apps.iosched.util.TimeUtils;
import com.google.samples.apps.iosched.util.UIUtils;
import com.google.samples.apps.iosched.util.UserAccount;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;

/**
 *
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetailPresenter {

    private static final String TAG = SessionDetailPresenter.class.getSimpleName();

    //Dependencies
    private SessionDetailView mSessionDetailView;
    private SessionColorResolver mColorResolver;
    private Resources mResources;
    private UserAccount mUserAccount;

    //Flags
    private String mTagsString;
    private boolean mAlreadyGaveFeedback;
    private boolean mIsUsingLocalTimeZone;

    public SessionDetailPresenter(SessionDetailView sessionDetailView,
                                  SessionColorResolver colorResolver,
                                  Resources resources,
                                  boolean isUsingLocalTimeZone) {
        mSessionDetailView = sessionDetailView;
        mColorResolver = colorResolver;
        mResources = resources;
        mIsUsingLocalTimeZone = isUsingLocalTimeZone;
    }

    public void updateTimeBasedUi(SessionDetail sessionDetail, boolean dismissedWatchLivestreamCard,
                                  HashSet<String> sDismissedFeedbackCard, String sessionId) {

        long currentTimeMillis = System.currentTimeMillis();

        if (sessionDetail.isLiveStreamAvailableNow() && !dismissedWatchLivestreamCard) {
            mSessionDetailView.showWatchNowCard();
        } else if (!mAlreadyGaveFeedback && sessionDetail.isStarred() && sessionDetail.canGiveFeedbackNow()
                && !sDismissedFeedbackCard.contains(sessionId)) {
            // show the "give feedback" card
            mSessionDetailView.showGiveFeedbackCard();
        }

        String timeHint = "";
        long sessionStart = sessionDetail.getSessionStart();
        long countdownMillis = sessionStart - currentTimeMillis;

        if (TimeUtils.hasConferenceEnded()) {
            // no time hint to display
            timeHint = "";
        } else if (currentTimeMillis >= sessionDetail.getSessionEnd()) {
            timeHint = mResources.getString(R.string.time_hint_session_ended);
        } else if (currentTimeMillis >= sessionStart) {
            long minutesAgo = (currentTimeMillis - sessionStart) / 60000;
            if (minutesAgo > 1) {
                timeHint = mResources.getString(R.string.time_hint_started_min, minutesAgo);
            } else {
                timeHint = mResources.getString(R.string.time_hint_started_just);
            }
        } else if (countdownMillis > 0 && countdownMillis < Config.HINT_TIME_BEFORE_SESSION) {
            long millisUntil = sessionStart - currentTimeMillis;
            long minutesUntil = millisUntil / 60000 + (millisUntil % 1000 > 0 ? 1 : 0);
            if (minutesUntil > 1) {
                timeHint = mResources.getString(R.string.time_hint_about_to_start_min, minutesUntil);
            } else {
                timeHint = mResources.getString(R.string.time_hint_about_to_start_shortly, minutesUntil);
            }
        }

        mSessionDetailView.renderTimeHint(timeHint);
    }

    public void presentSpeakers(List<Speaker> speakers) {
        mSessionDetailView.renderSessionSpeakers(speakers);
    }

    public void presentSessionPhoto(String photoUrl) {
        mSessionDetailView.renderSessionPhoto(photoUrl);
    }

    public void presentSessionAbstract(String sessionAbstract) {
        mSessionDetailView.renderSessionAbstract(sessionAbstract);
    }

    public void presentPlusOneButton(String sessionUrl, boolean isKeynote) {
        if (!TextUtils.isEmpty(sessionUrl) && !isKeynote) {
            mSessionDetailView.showPlusOneButton(sessionUrl);
        } else {
            mSessionDetailView.hidePlusOneButton();
        }
    }

    public void presentRequirements(String requirements) {
        mSessionDetailView.renderRequirements(requirements);
    }

    public void presentSessionTitles(SessionDetail sessionDetail) {
        // Format the time this session occupies
        long sessionStart = sessionDetail.getSessionStart();
        long sessionEnd = sessionDetail.getSessionEnd();
        String roomName = sessionDetail.getRoomName();
        String subtitle = UIUtils.formatSessionSubtitle(
                sessionStart, sessionEnd, roomName, new StringBuilder(), mIsUsingLocalTimeZone);
        if (sessionDetail.hasLiveStream()) {
            subtitle += " " + UIUtils.getLiveBadgeText(mResources, sessionStart, sessionEnd);
        }
        mSessionDetailView.setSessionTitle(sessionDetail.getSessionTitle());
        mSessionDetailView.setSessionSubtitle(subtitle);
    }

    public boolean presentSessionStarred(SessionDetail sessionDetail) {

        // Handle Keynote as a special case, where the user cannot remove it
        // from the schedule (it is auto added to schedule on sync)
        boolean isKeynote = sessionDetail.isKeynote();
        mSessionDetailView.setAddScheduleButtonVisible(mUserAccount.isActive() && !isKeynote);

        final boolean inMySchedule = sessionDetail.isInMySchedule();
        if (!isKeynote) {
            mSessionDetailView.showStarred(inMySchedule, false);
        }
        return isKeynote;
    }

    public void presentSessionColor(SessionDetail sessionDetail) {
        int sessionColor = mColorResolver.resolveSessionColor(sessionDetail.getColor());
        mSessionDetailView.setSessionColor(sessionColor);
    }

    public void buildLinksSection(SessionDetail sessionDetail) {
        long currentTimeMillis = System.currentTimeMillis();
        long sessionEnd = sessionDetail.getSessionEnd();
        if (sessionDetail.isLiveStreamAvailableNow()) {
            mSessionDetailView.showLiveStreamLink();
        }

        // Add session feedback link, if appropriate
        if (!mAlreadyGaveFeedback && currentTimeMillis > sessionEnd
                - Config.FEEDBACK_MILLIS_BEFORE_SESSION_END) {
            mSessionDetailView.showFeedbackLink();
        }

        for (String link : sessionDetail.getLinks()) {
            if (TextUtils.isEmpty(link)) {
                continue;
            }
            mSessionDetailView.showSessionLink(link);
        }
    }

    public void presentTags(TagMetadata tagMetadata, String tagsString) {
        mTagsString = tagsString;
        String[] tagIds = mTagsString.split(",");
        List<TagMetadata.Tag> tags = new ArrayList<>();
        for (String tagId : tagIds) {
            if (Config.Tags.SESSIONS.equals(tagId) ||
                    Config.Tags.SPECIAL_KEYNOTE.equals(tagId)) {
                continue;
            }
            TagMetadata.Tag tag = tagMetadata.getTag(tagId);
            if (tag == null) {
                continue;
            }
            tags.add(tag);
        }
        mSessionDetailView.renderSessionTags(tagMetadata, tags);
    }

    public void presentSocialStreamMenuItem(String hashTag) {
        if (!TextUtils.isEmpty(hashTag)) {
            mSessionDetailView.enableSocialStreamMenuItem();
        }
    }

    public void presentFeedback(boolean alreadyGaveSessionFeedback) {
        mAlreadyGaveFeedback = alreadyGaveSessionFeedback;
        if (alreadyGaveSessionFeedback) {
            mSessionDetailView.hideSessionCardView();
            mSessionDetailView.hideSubmitFeedbackButton();
        }
        LOGD(TAG, "User " + (alreadyGaveSessionFeedback ? "already gave" : "has not given")
                + " feedback for session.");
    }
}
