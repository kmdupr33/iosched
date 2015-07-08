package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.text.TextUtils;

import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.SessionColorResolver;
import com.google.samples.apps.iosched.util.BaseSubscriber;
import com.google.samples.apps.iosched.util.TimeUtils;
import com.google.samples.apps.iosched.util.UserAccount;

import org.w3c.dom.NameList;

import java.util.ArrayList;
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
    private SessionDetailDataLoader mSessionDetailDataLoader;
    private SessionColorResolver mColorResolver;
    private Context mContext;
    private UserAccount mUserAccount;

    //Flags
    private boolean mSessionDetailLoaded;
    private boolean mSpeakersLoaded;
    private boolean mHasSummaryContent;
    private String mTagsString;
    private TagMetadata mTagMetadata;

    public SessionDetailPresenter(SessionDetailView sessionDetailView,
                                  SessionDetailDataLoader sessionDetailDataLoader,
                                  SessionColorResolver colorResolver,
                                  Context context) {
        mSessionDetailView = sessionDetailView;
        mSessionDetailDataLoader = sessionDetailDataLoader;
        mColorResolver = colorResolver;
        mContext = context;
    }

    public void present() {
        mSessionDetailDataLoader.getSpeakersObservable().subscribe(new BaseSubscriber<List<Speaker>>() {
            @Override
            public void onNext(List<Speaker> speakers) {
                onSpeakersLoaded(speakers);
            }
        });
        mSessionDetailDataLoader.getSessionDetailObservable().subscribe(new BaseSubscriber<SessionDetail>() {
            @Override
            public void onNext(SessionDetail sessionDetail) {
                onSessionDetailLoaded(sessionDetail);
            }
        });
        mSessionDetailDataLoader.getTagsObservable().subscribe(new BaseSubscriber<List<TagMetadata.Tag>>() {
            @Override
            public void onNext(List<TagMetadata.Tag> tags) {
                onTagsLoaded(tags);
            }
        });
        mSessionDetailDataLoader.getFeedbackObservable().subscribe(new BaseSubscriber<Boolean>() {
            @Override
            public void onNext(Boolean alreadyGaveFeedback) {
                onFeedbackLoaded(alreadyGaveFeedback);
            }
        });
        mSessionDetailDataLoader.load();
    }

    public void updateTimeBasedUi(SessionDetail sessionDetail, boolean dismissedWatchLivestreamCard,
                                  boolean alreadyGaveFeedback, boolean initStarred,
                                  NameList sDismissedFeedbackCard, String sessionId) {

        long currentTimeMillis = System.currentTimeMillis();

        if (sessionDetail.isLiveStreamAvailableNow() && !dismissedWatchLivestreamCard) {
            mSessionDetailView.showWatchNowCard();
        } else if (!alreadyGaveFeedback && initStarred && sessionDetail.canGiveFeedbackNow()
                && !sDismissedFeedbackCard.contains(sessionId)) {
            // show the "give feedback" card
            mSessionDetailView.showGiveFeedbackCard();
        }

        String timeHint = "";
        long sessionStart = sessionDetail.getSessionStart();
        long countdownMillis = sessionStart - currentTimeMillis;

        if (TimeUtils.hasConferenceEnded(mContext)) {
            // no time hint to display
            timeHint = "";
        } else if (currentTimeMillis >= sessionDetail.getSessionEnd()) {
            timeHint = mContext.getString(R.string.time_hint_session_ended);
        } else if (currentTimeMillis >= sessionStart) {
            long minutesAgo = (currentTimeMillis - sessionStart) / 60000;
            if (minutesAgo > 1) {
                timeHint = mContext.getString(R.string.time_hint_started_min, minutesAgo);
            } else {
                timeHint = mContext.getString(R.string.time_hint_started_just);
            }
        } else if (countdownMillis > 0 && countdownMillis < Config.HINT_TIME_BEFORE_SESSION) {
            long millisUntil = sessionStart - currentTimeMillis;
            long minutesUntil = millisUntil / 60000 + (millisUntil % 1000 > 0 ? 1 : 0);
            if (minutesUntil > 1) {
                timeHint = mContext.getString(R.string.time_hint_about_to_start_min, minutesUntil);
            } else {
                timeHint = mContext.getString(R.string.time_hint_about_to_start_shortly, minutesUntil);
            }
        }

        mSessionDetailView.renderTimeHint(timeHint);
    }

    //----------------------------------------------------------------------------------
    // Helpers
    //----------------------------------------------------------------------------------
    private void onSpeakersLoaded(List<Speaker> speakers) {
        mSessionDetailView.renderSessionSpeakers(speakers);
    }

    private void onSessionDetailLoaded(SessionDetail sessionDetail) {
        mSessionDetailLoaded = true;

        int sessionColor = mColorResolver.resolveSessionColor(sessionDetail.getColor());
        mSessionDetailView.setSessionColor(sessionColor);

        mSessionDetailView.renderSessionTitles(sessionDetail);

        String photoUrl = sessionDetail.getPhotoUrl();
        mSessionDetailView.renderSessionPhoto(photoUrl);

        String hashTag = sessionDetail.getHashtag();
        if (!TextUtils.isEmpty(hashTag)) {
            mSessionDetailView.enableSocialStreamMenuItem();
        }

        mTagsString = sessionDetail.getTagsString();
        tryRenderTags();
        // Handle Keynote as a special case, where the user cannot remove it
        // from the schedule (it is auto added to schedule on sync)
        boolean isKeynote = sessionDetail.isKeynote();
        mSessionDetailView.setAddScheduleButtonVisible(mUserAccount.isActive() && !isKeynote);

        final boolean inMySchedule = sessionDetail.isInMySchedule();
        if (!isKeynote) {
            mSessionDetailView.showStarred(inMySchedule, false);
        }

        final String sessionAbstract = sessionDetail.getSessionAbstract();
        mSessionDetailView.renderSessionAbstract(sessionAbstract);

        mSessionDetailView.updatePlusOneButton(sessionDetail.getSessionUrl(), isKeynote);

        final String sessionRequirements = sessionDetail.getRequirements();
        mSessionDetailView.renderRequirements(sessionRequirements);

        mSessionDetailView.hideRelatedVideos();

        buildLinksSection(sessionDetail);

        mSessionDetailView.setEmptyViewVisible(mSessionDetailLoaded && mSpeakersLoaded && !mHasSummaryContent);
    }

    private void buildLinksSection(SessionDetail sessionDetail) {
        long currentTimeMillis = System.currentTimeMillis();
        long sessionEnd = sessionDetail.getSessionEnd();
        if (sessionDetail.isLiveStreamAvailableNow()) {
            mSessionDetailView.showLiveStreamLink();
        }

        // Add session feedback link, if appropriate
        boolean alreadyGaveFeedback = false;
        if (!alreadyGaveFeedback && currentTimeMillis > sessionEnd
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

    private void tryRenderTags() {
        if (mTagMetadata == null || mTagsString == null) {
            return;
        }
        String[] tagIds = mTagsString.split(",");
        List<TagMetadata.Tag> tags = new ArrayList<>();
        for (String tagId : tagIds) {
            if (Config.Tags.SESSIONS.equals(tagId) ||
                    Config.Tags.SPECIAL_KEYNOTE.equals(tagId)) {
                continue;
            }

            TagMetadata.Tag tag = mTagMetadata.getTag(tagId);
            if (tag == null) {
                continue;
            }

            tags.add(tag);
        }
        mSessionDetailView.renderSessionTags(tags);
    }

    private void onTagsLoaded(List<TagMetadata.Tag> tags) {
        mSessionDetailView.renderSessionTags(tags);
    }

    private void onFeedbackLoaded(boolean alreadyGaveSessionFeedback) {
        if (alreadyGaveSessionFeedback) {
            mSessionDetailView.hideSessionCardView();
            mSessionDetailView.hideSubmitFeedbackButton();
        }
        LOGD(TAG, "User " + (alreadyGaveSessionFeedback ? "already gave" : "has not given") + " feedback for session.");
    }
}
