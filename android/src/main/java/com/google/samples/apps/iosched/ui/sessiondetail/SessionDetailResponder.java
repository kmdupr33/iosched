package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.samples.apps.iosched.ui.SessionFeedbackActivity;
import com.google.samples.apps.iosched.ui.SessionLivestreamActivity;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.UIUtils;

import java.util.HashSet;

/**
 * Responds to user initiated events.
 *
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetailResponder {

    private SessionDetailPresenter mSessionDetailPresenter;
    private SessionDetailView mSessionDetailView;

    private Runnable mTimeHintUpdaterRunnable = null;
    private static final int TIME_HINT_UPDATE_INTERVAL = 10000; // 10 sec

    private boolean mDismissedWatchLivestreamCard;

    // this set stores the session IDs for which the user has dismissed the
    // "give feedback" card. This information is kept for the duration of the app's execution
    // so that if they say "No, thanks", we don't show the card again for that session while
    // the app is still executing.
    private static HashSet<String> sDismissedFeedbackCard = new HashSet<>();
    private SessionDetail mSessionDetail;
    private SessionCalendar mSessionCalendar;
    private Handler mHandler;
    private boolean mSpeakersLoaded;
    private boolean mHasSummaryContent;
    private boolean mStarred;
    private SessionDetailDataLoader mSessionDetailDataLoader;


    public SessionDetailResponder(SessionDetailPresenter sessionDetailPresenter,
                                  SessionDetailView sessionDetailView,
                                  SessionDetailDataLoader sessionDetailDataLoader,
                                  SessionCalendar sessionCalendar,
                                  Handler handler) {
        mSessionDetailPresenter = sessionDetailPresenter;
        mSessionDetailView = sessionDetailView;
        mSessionCalendar = sessionCalendar;
        mHandler = handler;
        mSessionDetailDataLoader = sessionDetailDataLoader;
    }

    public void onViewCreated() {
    }

    public void onSessionDetailLoaded(SessionDetail sessionDetail) {
        mSessionDetail = sessionDetail;
        mSessionDetailPresenter.updateTimeBasedUi(sessionDetail, mDismissedWatchLivestreamCard,
                                                  sDismissedFeedbackCard, mSessionDetailDataLoader.getSessionId());
        startUpdatingTimeHint();
        mSessionDetailPresenter.presentSessionColor(sessionDetail);
        mSessionDetailPresenter.presentSessionAbstract(sessionDetail.getSessionAbstract());
        mSessionDetailPresenter.presentRequirements(sessionDetail.getRequirements());
        mSessionDetailPresenter.presentSessionPhoto(sessionDetail.getPhotoUrl());
        mSessionDetailPresenter.presentSessionStarred(sessionDetail);
        mSessionDetailPresenter.presentSessionTitles(sessionDetail);
        mSessionDetailView.hideRelatedVideos();
        mSessionDetailPresenter.buildLinksSection(sessionDetail);
        mSessionDetailView.setEmptyViewVisible(isSessionDetailLoaded()
                                                       && mSpeakersLoaded
                                                       && !mHasSummaryContent);
    }

    public void onResume() {
        mSessionDetailPresenter.presentPlusOneButton(mSessionDetail.getSessionUrl(),
                                                     mSessionDetail.isKeynote());
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
        }
        // Refresh whether or not feedback has been submitted
        mSessionDetailDataLoader.reloadFeedback();
    }

    public void onWatchNowCardClicked(String tag, Context context) {
        if ("WATCH_NOW".equals(tag)) {
            Intent intent = getWatchLiveIntent(context);
            context.startActivity(intent);
        } else {
            mDismissedWatchLivestreamCard = true;
            mSessionDetailView.hideWatchNowView();
        }
    }

    public void onFeedbackCardClicked(String tag, Context context) {
        if ("GIVE_FEEDBACK".equals(tag)) {
                    /* [ANALYTICS:EVENT]
                     * TRIGGER:   Click on the Send Feedback action on the Session Details page.
                     * CATEGORY:  'Session'
                     * ACTION:    'Feedback'
                     * LABEL:     session title/subtitle
                     * [/ANALYTICS]
                     */
            AnalyticsManager.sendEvent("Session", "Feedback", mSessionDetail.getSessionTitle(), 0L);
            Intent intent = getFeedbackIntent(context);
            context.startActivity(intent);
        } else {
            sDismissedFeedbackCard.add(mSessionDetailDataLoader.getSessionId());
            mSessionDetailView.hideFeedbackCard();
        }
    }

    private Intent getWatchLiveIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                YouTubeIntents.canResolvePlayVideoIntent(context)) {
            String liveStreamUrl = mSessionDetail.getLiveStreamUrl();
            String youtubeVideoId = SessionLivestreamActivity.getVideoIdFromUrl(liveStreamUrl);
            return YouTubeIntents.createPlayVideoIntentWithOptions(
                    context, youtubeVideoId, true, false);
        }
        return new Intent(Intent.ACTION_VIEW, mSessionDetailDataLoader.getSessionUri()).setClass(context,
                                                                    SessionLivestreamActivity.class);
    }

    private Intent getFeedbackIntent(Context context) {
        return new Intent(Intent.ACTION_VIEW, mSessionDetailDataLoader.getSessionUri(), context,
                          SessionFeedbackActivity.class);
    }

    public void onPause() {
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.removeCallbacks(mTimeHintUpdaterRunnable);
        }
    }

    //----------------------------------------------------------------------------------
    // Helpers
    //----------------------------------------------------------------------------------
    private boolean isSessionDetailLoaded() {
        return mSessionDetail != null;
    }

    private void startUpdatingTimeHint() {
        mTimeHintUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                mSessionDetailPresenter.updateTimeBasedUi(mSessionDetail,
                                                          mDismissedWatchLivestreamCard,
                                                          sDismissedFeedbackCard, mSessionDetailDataLoader.getSessionId());
                mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
    }

    public void onStop() {
        if (mSessionDetail.isStarred() != mStarred) {
            long sessionStart = mSessionDetail.getSessionStart();
            if (UIUtils.getCurrentTime() < sessionStart) {
                Uri sessionUri = mSessionDetailDataLoader.getSessionUri();
                SessionCalendar.SessionCalendarEntry sessionCalendarEntry = SessionCalendar.SessionCalendarEntry.from(mSessionDetail, sessionUri);
                if (mStarred) {
                    mSessionCalendar.addSession(sessionCalendarEntry);
                } else {
                    mSessionCalendar.removeSession(sessionCalendarEntry);
                }
            }
        }
    }
}
