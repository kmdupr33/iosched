package com.google.samples.apps.iosched.ui.sessiondetail;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by MattDupree on 7/7/15.
 */
public class SessionDetailResponder {

    private Observable<SessionDetail> mSessionDetailObservable;
    private SessionDetailPresenter mSessionDetailPresenter;

    public SessionDetailResponder(Observable<SessionDetail> sessionDetailObservable, SessionDetailPresenter sessionDetailPresenter) {
        mSessionDetailObservable = sessionDetailObservable;
        mSessionDetailPresenter = sessionDetailPresenter;
    }

    public void onViewCreated() {
        mSessionDetailObservable.subscribe(new Action1<SessionDetail>() {
            @Override
            public void call(SessionDetail sessionDetail) {
                mSessionDetailPresenter.updateTimeBasedUi(sessionDetail, dismissedWatchLivestreamCard, alreadyGaveFeedback, initStarred, sDismissedFeedbackCard, sessionId);
                startUpdatingTimeHint();
            }
        });
    }

    public void onResume() {
        updatePlusOneButton(sessionDetail.getSessionUrl(), sessionDetail.isKeynote());
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
        }
        // Refresh whether or not feedback has been submitted
        getLoaderManager().restartLoader(FeedbackQuery._TOKEN, null, this);
    }

    public void startUpdatingTimeHint() {
        mTimeHintUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeBasedUi();
                mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
    }
}
