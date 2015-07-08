package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.samples.apps.iosched.ui.SessionFeedbackActivity;
import com.google.samples.apps.iosched.ui.SessionLivestreamActivity;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.BaseSubscriber;

import java.util.HashSet;

/**
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetailResponder {

    private SessionDetailDataLoader mSessionDetailDataLoader;
    private SessionDetailPresenter mSessionDetailPresenter;
    private SessionDetailView mSessionDetailView;

    private Runnable mTimeHintUpdaterRunnable = null;
    private static final int TIME_HINT_UPDATE_INTERVAL = 10000; // 10 sec

    private boolean mDismissedWatchLivestreamCard;

    // this set stores the session IDs for which the user has dismissed the
    // "give feedback" card. This information is kept for the duration of the app's execution
    // so that if they say "No, thanks", we don't show the card again for that session while
    // the app is still executing.
    private static HashSet<String> sDismissedFeedbackCard = new HashSet<>();;
    private SessionDetail mSessionDetail;
    private Handler mHandler;


    public SessionDetailResponder(SessionDetailDataLoader sessionDetailDataLoader,
                                  SessionDetailPresenter sessionDetailPresenter,
                                  SessionDetailView sessionDetailView, Handler handler) {
        mSessionDetailDataLoader = sessionDetailDataLoader;
        mSessionDetailPresenter = sessionDetailPresenter;
        mSessionDetailView = sessionDetailView;
        mHandler = handler;
    }

    public void onViewCreated() {
        mSessionDetailDataLoader.addSessionDetailLoadedSubscriber(
                new BaseSubscriber<SessionDetail>() {
                    @Override
                    public void onNext(SessionDetail sessionDetail) {
                        mSessionDetail = sessionDetail;
                        mSessionDetailPresenter.updateTimeBasedUi(sessionDetail,
                                                                  mDismissedWatchLivestreamCard,
                                                                  sDismissedFeedbackCard);
                        startUpdatingTimeHint();
                    }
                });
    }

    public void onResume() {
        mSessionDetailView.updatePlusOneButton(mSessionDetail.getSessionUrl(),
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

    private void startUpdatingTimeHint() {
        mTimeHintUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                mSessionDetailPresenter.updateTimeBasedUi(mSessionDetail,
                                                          mDismissedWatchLivestreamCard,
                                                          sDismissedFeedbackCard);
                mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
    }
}
