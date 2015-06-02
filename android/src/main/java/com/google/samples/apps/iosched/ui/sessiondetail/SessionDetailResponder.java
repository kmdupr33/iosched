package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.service.SessionAlarmService;
import com.google.samples.apps.iosched.service.SessionCalendarServiceStarter;
import com.google.samples.apps.iosched.ui.widget.MessageCardView;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.SessionsHelper;
import com.google.samples.apps.iosched.util.UIUtils;

import java.util.HashSet;

import rx.Observable;
import rx.functions.Action1;

import static com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailDataLoader.FeedbackQuery;
import static com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailDataLoader.SessionsQuery;
import static com.google.samples.apps.iosched.util.LogUtils.LOGD;

/**
 *
 * Created by MattDupree on 5/31/15.
 */
public class SessionDetailResponder implements MessageCardView.OnMessageCardButtonClicked {

    private static final String TAG = SessionDetailResponder.class.getSimpleName();
    private boolean mStarred;
    private SessionDetailPresenter mSessionDetailPresenter;
    private final SessionsHelper mSessionsHelper;
    private Uri mSessionUri;
    private String mTitleString;

    // this set stores the session IDs for which the user has dismissed the
    // "give feedback" card. This information is kept for the duration of the app's execution
    // so that if they say "No, thanks", we don't show the card again for that session while
    // the app is still executing.
    static HashSet<String> sDismissedFeedbackCard = new HashSet<>();

    //TODO This is a bad name for this dependency. It should be described in this object's vocabulary, a vocabulary that's devoid of implentation-laden terms like Service ("Service" as in android service)
    private SessionCalendarServiceStarter mSessionCalendarServiceStarter;
    private boolean mInitStarred;
    private String mRoomId;
    private Context mSessionDetailActivity;
    private String mHashTag;
    private String mUrl;
    private long mSessionStart;
    private long mSessionEnd;
    private String mRoomName;
    private String mSessionId;
    private boolean mSpeakers;

    public SessionDetailResponder(SessionDetailPresenter sessionDetailPresenter,
                                  SessionsHelper sessionsHelper,
                                  SessionCalendarServiceStarter sessionCalendarServiceStarter) {

        mSessionDetailPresenter = sessionDetailPresenter;
        mSessionsHelper = sessionsHelper;
        mSessionCalendarServiceStarter = sessionCalendarServiceStarter;
    }

    public void respond(String sessionId,
                        Observable<Cursor> sessionQueryObservable) {

        mSessionId = sessionId;
        sessionQueryObservable.subscribe(new CursorAction1());
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_map_room:
                /* [ANALYTICS:EVENT]
                 * TRIGGER:   Click on the Map action on the Session Details page.
                 * CATEGORY:  'Session'
                 * ACTION:    'Map'
                 * LABEL:     session title/subtitle
                 * [/ANALYTICS]
                 */
                AnalyticsManager
                        .sendEvent("Session", "Map", mTitleString, 0L);
                mSessionsHelper.startMapActivity(mRoomId);
                return true;

            case R.id.menu_share:
                // On ICS+ devices, we normally won't reach this as ShareActionProvider will handle
                // sharing.
                mSessionsHelper.shareSession(mSessionDetailActivity, R.string.share_template,
                                    mTitleString,
                                    mHashTag, mUrl);
                return true;

            case R.id.menu_social_stream:
                if (!TextUtils.isEmpty(mHashTag)) {
                    /* [ANALYTICS:EVENT]
                     * TRIGGER:   Click on the Social Stream action on the Session Details page.
                     * CATEGORY:  'Session'
                     * ACTION:    'Stream'
                     * LABEL:     session title/subtitle
                     * [/ANALYTICS]
                     */
                    AnalyticsManager.sendEvent("Session", "Stream",
                                               mTitleString, 0L);
                    UIUtils.showHashtagStream(mSessionDetailActivity, mHashTag);
                }
                return true;
        }
        return false;
    }

    public void onToggleSessionInSchedule() {
        mStarred = !mStarred;
        mSessionDetailPresenter.presentSessionStarred(mStarred);
        mSessionsHelper.setSessionStarred(mSessionUri, mStarred, mTitleString);
    }

    //TODO Move to responder. Question: How will we get the session data needed to launch the service?
    /*
        Technically, it seems like there should be an object that gets the data, an object that presents the data,
            and an object that responds to user's input on the data. The latter two objects need to be notified whenever
            there's new data available. How can we achieve this?

            RxJava seems like a natural way to do this. There is, however, a problem: how do we handle
            cleaning up subscribers when the activity is recreated? Also, how do we handle new data coming in?
            We can't just call onComplete() after a load because that'll unsubscribe our subscribers from
            receiving further updates. If we don't call onComplete(), how will the subscribers get cleaned up
            when the activity is destroyed? Answer: Don't call onComplete(). Unsubscribe by forwarding an onStop()
            event to the subscribers.
     */
    public void onStop() {

        if (mInitStarred != mStarred) {
            if (System.currentTimeMillis() < mSessionStart) {
                // Update Calendar event through the Calendar API on Android 4.0 or new versions.
                if (mStarred) {
                    mSessionCalendarServiceStarter.startAddSessionService(mSessionUri,
                                                                          mSessionStart,
                                                                          mSessionEnd, mRoomName,
                                                                          mTitleString);
                } else {
                    mSessionCalendarServiceStarter.startRemoveSessionService(mSessionUri, mSessionStart, mSessionEnd, mTitleString);
                }
                if (mStarred) {
                    setupNotification(mSessionDetailActivity);
                }
            }
        }
    }

    public void onResume() {
        mSessionDetailPresenter.updatePlusOneButton();
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.postDelayed(
                    mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
        }

        // Refresh whether or not feedback has been submitted
        mSessionDetailActivity.getLoaderManager().restartLoader(FeedbackQuery._TOKEN, null,
                                                                this);

    }

    //TODO Move to responder
    @Override
    public void onMessageCardButtonClicked(MessageCardView messageCardView, String tag) {
        switch (messageCardView.getId()) {
            case R.id.give_feedback_card:
                onFeedbackCardClicked(messageCardView, tag);
                break;
            case R.id.live_now_card:
                onLiveNowCardClicked(messageCardView, tag);
                break;
        }
    }

    //TODO Move to responder
    private void onFeedbackCardClicked(MessageCardView messageCardView, String tag) {

        if ("WATCH_NOW".equals(tag)) {
            Intent intent = getWatchLiveIntent();
            mSessionDetailActivity.startActivity(intent);
        } else {
            mDismissedWatchLivestreamCard = true;
            messageCardView.dismiss();
        }
    }

    //TODO Move to responder
    private void onLiveNowCardClicked(MessageCardView messageCardView, String tag) {

        if ("GIVE_FEEDBACK".equals(tag)) {
            /* [ANALYTICS:EVENT]
             * TRIGGER:   Click on the Send Feedback action on the Session Details page.
             * CATEGORY:  'Session'
             * ACTION:    'Feedback'
             * LABEL:     session title/subtitle
             * [/ANALYTICS]
             */
            AnalyticsManager.sendEvent("Session", "Feedback",
                                       mTitleString, 0L);
            Intent intent = getFeedbackIntent();
            mSessionDetailActivity.startActivity(intent);
        } else {
            sDismissedFeedbackCard.add(mSessionId);
            messageCardView.dismiss();
        }
    }

    private void setupNotification(Context context) {
        Intent scheduleIntent;

        // Schedule session notification
        if (System.currentTimeMillis() < mSessionStart) {
            LOGD(TAG, "Scheduling notification about session start.");
            //TODO Technical detail. Should be moved to adapter
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_STARRED_BLOCK,
                    null, context, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START,
                                    mSessionStart);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END,
                                    mSessionEnd);
            context.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling notification about session start, too late.");
        }

        // Schedule feedback notification
        if (UIUtils.getCurrentTime(context) < mSessionEnd) {
            LOGD(TAG, "Scheduling notification about session feedback.");
            //TODO Technical detail. Should be moved to adapter
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_FEEDBACK_NOTIFICATION,
                    null, context, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_ID,
                                    mSessionId);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START,
                                    mSessionStart);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END,
                                    mSessionEnd);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_TITLE,
                                    mTitleString);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_ROOM, mRoomName);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_SPEAKERS, mSpeakers);
            context.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling feedback notification, too late.");
        }
    }

    private class CursorAction1 implements Action1<Cursor> {

        @Override
        public void call(Cursor cursor) {
            mInitStarred = cursor.getInt(SessionsQuery.IN_MY_SCHEDULE) != 0;
            mHashTag = cursor.getString(SessionsQuery.HASHTAG);
            mRoomId = cursor.getString(SessionsQuery.ROOM_ID);
            mRoomName = cursor.getString(SessionsQuery.ROOM_NAME);
            mSessionStart = cursor.getLong(SessionsQuery.START);
            mSessionEnd = cursor.getLong(SessionsQuery.END);
            mTitleString = cursor.getString(SessionsQuery.TITLE);
        }
    }
}
