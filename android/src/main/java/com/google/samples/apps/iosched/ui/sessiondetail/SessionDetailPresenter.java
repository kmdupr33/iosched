package com.google.samples.apps.iosched.ui.sessiondetail;

import android.app.LoaderManager;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.request.bitmap.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.provider.ScheduleContract;
import com.google.samples.apps.iosched.service.SessionAlarmService;
import com.google.samples.apps.iosched.service.SessionCalendarService;
import com.google.samples.apps.iosched.ui.SessionFeedbackActivity;
import com.google.samples.apps.iosched.ui.SessionLivestreamActivity;
import com.google.samples.apps.iosched.ui.widget.MessageCardView;
import com.google.samples.apps.iosched.util.AccountUtils;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.ImageLoader;
import com.google.samples.apps.iosched.util.SessionsHelper;
import com.google.samples.apps.iosched.util.TimeUtils;
import com.google.samples.apps.iosched.util.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;

public class SessionDetailPresenter implements LoaderManager.LoaderCallbacks<Cursor>,MessageCardView.OnMessageCardButtonClicked {

    // this set stores the session IDs for which the user has dismissed the
    // "give feedback" card. This information is kept for the duration of the app's execution
    // so that if they say "No, thanks", we don't show the card again for that session while
    // the app is still executing.
    private static HashSet<String> sDismissedFeedbackCard = new HashSet<>();

    private static final int TIME_HINT_UPDATE_INTERVAL = 10000; // 10 sec
    private static final String TAG = SessionDetailPresenter.class.getSimpleName();
    private static final int TAG_METADATA_TOKEN = 0x5;

    private final SessionDetailActivity mSessionDetailActivity;
    String mSessionId;
    Uri mSessionUri;
    long mSessionStart;
    long mSessionEnd;
    String mTitleString;
    boolean mStarred;
    boolean mInitStarred;

    private TagMetadata mTagMetadata;

    private String mHashTag;
    private String mUrl;
    private String mRoomId;
    private String mRoomName;
    private String mTagsString;

    // A comma-separated list of speakers to be passed to Android Wear
    private String mSpeakers;
    private boolean mIsKeynote;
    private String mLiveStreamUrl;
    private boolean mHasLivestream;
    private StringBuilder mBuffer = new StringBuilder();

    private ImageLoader mNoPlaceholderImageLoader;
    private Runnable mTimeHintUpdaterRunnable;
    private Handler mHandler = new Handler();
    private boolean mAlreadyGaveFeedback;
    private boolean mDismissedWatchLivestreamCard;

    public SessionDetailPresenter(SessionDetailActivity mSessionDetailActivity,
                                  ImageLoader noPlaceholderImageLoader) {

        this.mSessionDetailActivity = mSessionDetailActivity;
        mNoPlaceholderImageLoader = noPlaceholderImageLoader;
    }

    public void onStop() {

        if (mInitStarred != mStarred) {
            if (UIUtils.getCurrentTime(mSessionDetailActivity) < mSessionStart) {
                // Update Calendar event through the Calendar API on Android 4.0 or new versions.
                Intent intent;
                if (mStarred) {
                    // Set up intent to add session to Calendar, if it doesn't exist already.
                    intent = new Intent(SessionCalendarService.ACTION_ADD_SESSION_CALENDAR,
                                        mSessionUri);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                                    mSessionStart);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                                    mSessionEnd);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_ROOM,
                                    mRoomName);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitleString);
                } else {
                    // Set up intent to remove session from Calendar, if exists.
                    intent = new Intent(SessionCalendarService.ACTION_REMOVE_SESSION_CALENDAR,
                                        mSessionUri);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                                    mSessionStart);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                                    mSessionEnd);
                    intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitleString);
                }
                intent.setClass(mSessionDetailActivity, SessionCalendarService.class);
                mSessionDetailActivity.startService(intent);

                if (mStarred) {
                    setupNotification(mSessionDetailActivity, mSessionDetailActivity);
                }
            }
        }
    }

    private void setupNotification(SessionDetailActivity sessionDetailActivity,
                                   ContextWrapper contextWrapper) {
        Intent scheduleIntent;

        // Schedule session notification
        if (UIUtils.getCurrentTime(sessionDetailActivity) < mSessionStart) {
            LOGD(TAG, "Scheduling notification about session start.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_STARRED_BLOCK,
                    null, sessionDetailActivity, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START,
                                    mSessionStart);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END,
                                    mSessionEnd);
            contextWrapper.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling notification about session start, too late.");
        }

        // Schedule feedback notification
        if (UIUtils.getCurrentTime(sessionDetailActivity) < mSessionEnd) {
            LOGD(TAG, "Scheduling notification about session feedback.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_FEEDBACK_NOTIFICATION,
                    null, sessionDetailActivity, SessionAlarmService.class);
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
            contextWrapper.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling feedback notification, too late.");
        }
    }

    /**
     * Handle {@link SessionsQuery} {@link Cursor}.
     * @param cursor contains seesionquery data
     */
    void onSessionQueryComplete(Cursor cursor) {

        mSessionDetailActivity.onSessionQueryComplete();
        if (!cursor.moveToFirst()) {
            // TODO: Remove this in favor of a callbacks interface that the activity
            // can implement.
            mSessionDetailActivity.finish();
            return;
        }

        mTitleString = cursor.getString(SessionsQuery.TITLE);
        int sessionColor = cursor.getInt(SessionsQuery.COLOR);

        if (sessionColor == 0) {
            // no color -- use default
            sessionColor = mSessionDetailActivity.getResources().getColor(R.color.default_session_color);
        } else {
            // make sure it's opaque
            sessionColor = UIUtils.setColorAlpha(
                    sessionColor, 255);
        }

        mSessionDetailActivity.renderSessionColor(sessionColor);


        mLiveStreamUrl = cursor.getString(SessionsQuery.LIVESTREAM_URL);
        mHasLivestream = !TextUtils.isEmpty(mLiveStreamUrl);

        // Format the time this session occupies
        mSessionStart = cursor.getLong(SessionsQuery.START);
        mSessionEnd = cursor.getLong(SessionsQuery.END);
        mRoomName = cursor.getString(SessionsQuery.ROOM_NAME);
        mSpeakers = cursor.getString(SessionsQuery.SPEAKER_NAMES);
        String subtitle = UIUtils.formatSessionSubtitle(
                mSessionStart, mSessionEnd, mRoomName, mBuffer,
                mSessionDetailActivity);
        if (mHasLivestream) {
            subtitle += " " + UIUtils.getLiveBadgeText(mSessionDetailActivity, mSessionStart,
                                                       mSessionEnd);
        }

        mSessionDetailActivity.renderTitle(mTitleString, subtitle);

        String photo = cursor.getString(SessionsQuery.PHOTO_URL);
        if (!TextUtils.isEmpty(photo)) {
            mSessionDetailActivity.renderSessionPhoto(photo);
        } else {
            mSessionDetailActivity.recomputePhotoAndScrollingMetrics();
        }

        mUrl = cursor.getString(SessionsQuery.URL);
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = "";
        }

        mHashTag = cursor.getString(SessionsQuery.HASHTAG);
        if (!TextUtils.isEmpty(mHashTag)) {
            mSessionDetailActivity.enableSocialStreamMenuItemDeferred();
        }

        mRoomId = cursor.getString(SessionsQuery.ROOM_ID);

        final boolean inMySchedule = cursor.getInt(SessionsQuery.IN_MY_SCHEDULE) != 0;

        mSessionDetailActivity.setupShareMenuItemDeferred(mHashTag, mUrl);

        // Handle Keynote as a special case, where the user cannot remove it
        // from the schedule (it is auto added to schedule on sync)
        mTagsString = cursor.getString(SessionsQuery.TAGS);
        mIsKeynote = mTagsString.contains(Config.Tags.SPECIAL_KEYNOTE);
        mSessionDetailActivity.mAddScheduleButton.setVisibility(
                (AccountUtils.hasActiveAccount(
                        mSessionDetailActivity) && !mIsKeynote)
                        ? View.VISIBLE : View.INVISIBLE);

        tryRenderTags();

        if (!mIsKeynote) {
            mSessionDetailActivity.showStarredDeferred(mInitStarred = inMySchedule, false);
        }

        final String sessionAbstract = cursor.getString(SessionsQuery.ABSTRACT);
        if (!TextUtils.isEmpty(sessionAbstract)) {
            mSessionDetailActivity.renderSessionAbstract(sessionAbstract);
        } else {
            mSessionDetailActivity.hideAbstract();
        }

        updatePlusOneButton();

        final String sessionRequirements = cursor.getString(SessionsQuery.REQUIREMENTS);
        if (!TextUtils.isEmpty(sessionRequirements)) {
            mSessionDetailActivity.renderRequirements(sessionRequirements);
        } else {
            mSessionDetailActivity.hideRequirementsBlock();
        }

        // Build links section
        buildLinksSection(cursor);

        mSessionDetailActivity.updateEmptyView();

        updateTimeBasedUi(mSessionDetailActivity);
        mSessionDetailActivity.enableScrolling();

        mTimeHintUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeBasedUi(mSessionDetailActivity);
                mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
    }

    private void buildLinksSection(Cursor cursor) {
        // Build links section
        // the Object can be either a string URL or an Intent
        List<Pair<Integer, Object>> links = new ArrayList<>();

        long currentTimeMillis = UIUtils.getCurrentTime(mSessionDetailActivity);
        if (mHasLivestream
                && currentTimeMillis > mSessionStart
                && currentTimeMillis <= mSessionEnd) {
            links.add(new Pair<Integer, Object>(
                    R.string.session_link_livestream,
                    getWatchLiveIntent()));
        }

        // Add session feedback link, if appropriate
        if (!mAlreadyGaveFeedback && currentTimeMillis > mSessionEnd
                - Config.FEEDBACK_MILLIS_BEFORE_SESSION_END) {
            links.add(new Pair<Integer, Object>(
                    R.string.session_feedback_submitlink,
                    getFeedbackIntent()
            ));
        }

        for (int i = 0; i < SessionsQuery.LINKS_INDICES.length; i++) {
            final String linkUrl = cursor.getString(SessionsQuery.LINKS_INDICES[i]);
            if (TextUtils.isEmpty(linkUrl)) {
                continue;
            }

            //noinspection deprecation
            links.add(new Pair<Integer, Object>(
                    SessionsQuery.LINKS_TITLES[i],
                    new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            ));
        }
        if (links.size() == 0) {
            mSessionDetailActivity.hideLinks();
        }
        mSessionDetailActivity.renderLinks(links, mTitleString);
    }

    void onFeedbackQueryComplete(Cursor cursor) {
        // Is there existing feedback for this session?
        mAlreadyGaveFeedback = cursor.getCount() > 0;

        if (mAlreadyGaveFeedback) {
            mSessionDetailActivity.hideFeedbackView();
        }
        LOGD(TAG,
             "User " + (mAlreadyGaveFeedback ? "already gave" : "has not given") + " feedback for session.");
        cursor.close();
    }

    void startLoad(LoaderManager manager) {

        manager.initLoader(SessionsQuery._TOKEN, null, this);
        manager.initLoader(SpeakersQuery._TOKEN, null, this);
        manager.initLoader(TAG_METADATA_TOKEN, null, this);
    }

    void onResume() {

        updatePlusOneButton();
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.postDelayed(
                    mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
        }

        // Refresh whether or not feedback has been submitted
        mSessionDetailActivity.getLoaderManager().restartLoader(FeedbackQuery._TOKEN, null,
                                                  this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        CursorLoader loader = null;
        if (id == SessionsQuery._TOKEN){
            loader = new CursorLoader(
                    mSessionDetailActivity, mSessionUri, SessionsQuery.PROJECTION, null,
                    null, null);
        } else if (id == SpeakersQuery._TOKEN  && mSessionUri != null){
            Uri speakersUri = ScheduleContract.Sessions.buildSpeakersDirUri(
                    mSessionId);
            loader = new CursorLoader(
                    mSessionDetailActivity, speakersUri, SpeakersQuery.PROJECTION, null,
                    null, ScheduleContract.Speakers.DEFAULT_SORT);
        } else if (id == FeedbackQuery._TOKEN) {
            Uri feedbackUri = ScheduleContract.Feedback.buildFeedbackUri(
                    mSessionId);
            loader = new CursorLoader(
                    mSessionDetailActivity, feedbackUri, FeedbackQuery.PROJECTION, null,
                    null, null);
        } else if (id == TAG_METADATA_TOKEN) {
            loader = TagMetadata.createCursorLoader(mSessionDetailActivity);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == SessionsQuery._TOKEN) {
            onSessionQueryComplete(cursor);
        } else if (loader.getId() == SpeakersQuery._TOKEN) {
            onSpeakersQueryComplete(cursor);
        } else if (loader.getId() == FeedbackQuery._TOKEN) {
            onFeedbackQueryComplete(cursor);
        } else if (loader.getId() == TAG_METADATA_TOKEN) {
            mTagMetadata = new TagMetadata(cursor);
            cursor.close();
            tryRenderTags();
        } else {
            cursor.close();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {}

    private void onSpeakersQueryComplete(Cursor cursor) {

        mSessionDetailActivity.onSpeakdersQueryCompleted();
        mSessionDetailActivity.renderSpeakers(cursor);
        mSessionDetailActivity.updateEmptyView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        SessionsHelper helper = new SessionsHelper(mSessionDetailActivity);
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
                helper.startMapActivity(mRoomId);
                return true;

            case R.id.menu_share:
                // On ICS+ devices, we normally won't reach this as ShareActionProvider will handle
                // sharing.
                helper.shareSession(mSessionDetailActivity, R.string.share_template,
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

    private void tryRenderTags() {
        if (mTagMetadata == null || mTagsString == null) {
            return;
        }

        if (TextUtils.isEmpty(mTagsString)) {
            mSessionDetailActivity.hideTags();
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

        if (tags.size() == 0) {
            mSessionDetailActivity.hideTags();
            return;
        }
        Collections.sort(tags, TagMetadata.TAG_DISPLAY_ORDER_COMPARATOR);
        mSessionDetailActivity.renderTags(tags);
    }

    private void updatePlusOneButton() {
        if (!TextUtils.isEmpty(mUrl) && !mIsKeynote) {
            mSessionDetailActivity.showPlusOneButton(mUrl);
        } else {
            mSessionDetailActivity.hidePlusOneButton();
        }
    }

    private void updateTimeBasedUi(SessionDetailActivity sessionDetailActivity) {
        long currentTimeMillis = UIUtils.getCurrentTime(sessionDetailActivity);
        boolean canShowLivestream = mHasLivestream;

        if (canShowLivestream && !mDismissedWatchLivestreamCard
                && currentTimeMillis > mSessionStart
                && currentTimeMillis <= mSessionEnd) {
            // show the "watch now" card
            mSessionDetailActivity.showWatchNowCard();
        } else if (!mAlreadyGaveFeedback && mInitStarred && currentTimeMillis >= (mSessionEnd -
                Config.FEEDBACK_MILLIS_BEFORE_SESSION_END)
                && !sDismissedFeedbackCard.contains(mSessionId)) {
            // show the "give feedback" card
            sessionDetailActivity.showGiveFeedbackCard();
        }

        String timeHint = "";
        long countdownMillis = mSessionStart - currentTimeMillis;

        if (TimeUtils.hasConferenceEnded(sessionDetailActivity)) {
            // no time hint to display
            timeHint = "";
        } else if (currentTimeMillis >= mSessionEnd) {
            timeHint = mSessionDetailActivity.getString(R.string.time_hint_session_ended);
        } else if (currentTimeMillis >= mSessionStart) {
            long minutesAgo = (currentTimeMillis - mSessionStart) / 60000;
            if (minutesAgo > 1) {
                timeHint = mSessionDetailActivity.getString(R.string.time_hint_started_min, minutesAgo);
            } else {
                timeHint = mSessionDetailActivity.getString(R.string.time_hint_started_just);
            }
        } else if (countdownMillis > 0 && countdownMillis < Config.HINT_TIME_BEFORE_SESSION) {
            long millisUntil = mSessionStart - currentTimeMillis;
            long minutesUntil = millisUntil / 60000 + (millisUntil % 1000 > 0 ? 1 : 0);
            if (minutesUntil > 1) {
                timeHint = mSessionDetailActivity.getString(R.string.time_hint_about_to_start_min, minutesUntil);
            } else {
                timeHint = mSessionDetailActivity
                        .getString(R.string.time_hint_about_to_start_shortly, minutesUntil);
            }
        }

        if (!TextUtils.isEmpty(timeHint)) {
            mSessionDetailActivity.renderTimeHint(timeHint);
        } else {
            mSessionDetailActivity.hideTimeHint();
        }
    }


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

    private void onFeedbackCardClicked(MessageCardView messageCardView, String tag) {

        if ("WATCH_NOW".equals(tag)) {
            Intent intent = getWatchLiveIntent();
            mSessionDetailActivity.startActivity(intent);
        } else {
            mDismissedWatchLivestreamCard = true;
            messageCardView.dismiss();
        }
    }

    private Intent getWatchLiveIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                YouTubeIntents.canResolvePlayVideoIntent(mSessionDetailActivity)) {
            String youtubeVideoId = SessionLivestreamActivity.getVideoIdFromUrl(
                    mLiveStreamUrl);
            return YouTubeIntents.createPlayVideoIntentWithOptions(
                    mSessionDetailActivity, youtubeVideoId, true, false);
        }
        return new Intent(Intent.ACTION_VIEW, mSessionUri).setClass(mSessionDetailActivity,
                SessionLivestreamActivity.class);
    }

    private Intent getFeedbackIntent() {
        return new Intent(Intent.ACTION_VIEW, mSessionUri, mSessionDetailActivity,
                SessionFeedbackActivity.class);
    }

    /**
     * {@link ScheduleContract.Sessions} query parameters.
     */
    interface SessionsQuery {
        int _TOKEN = 0x1;

        String[] PROJECTION = {
                ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
                ScheduleContract.Sessions.SESSION_LEVEL,
                ScheduleContract.Sessions.SESSION_TITLE,
                ScheduleContract.Sessions.SESSION_ABSTRACT,
                ScheduleContract.Sessions.SESSION_REQUIREMENTS,
                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE,
                ScheduleContract.Sessions.SESSION_HASHTAG,
                ScheduleContract.Sessions.SESSION_URL,
                ScheduleContract.Sessions.SESSION_YOUTUBE_URL,
                ScheduleContract.Sessions.SESSION_PDF_URL,
                ScheduleContract.Sessions.SESSION_NOTES_URL,
                ScheduleContract.Sessions.SESSION_LIVESTREAM_URL,
                ScheduleContract.Sessions.SESSION_MODERATOR_URL,
                ScheduleContract.Sessions.ROOM_ID,
                ScheduleContract.Rooms.ROOM_NAME,
                ScheduleContract.Sessions.SESSION_COLOR,
                ScheduleContract.Sessions.SESSION_PHOTO_URL,
                ScheduleContract.Sessions.SESSION_RELATED_CONTENT,
                ScheduleContract.Sessions.SESSION_TAGS,
                ScheduleContract.Sessions.SESSION_SPEAKER_NAMES
        };

        int START = 0;
        int END = 1;
        @SuppressWarnings("unused")
        int LEVEL = 2;
        int TITLE = 3;
        int ABSTRACT = 4;
        int REQUIREMENTS = 5;
        int IN_MY_SCHEDULE = 6;
        int HASHTAG = 7;
        int URL = 8;
        int YOUTUBE_URL = 9;
        int PDF_URL = 10;
        int NOTES_URL = 11;
        int LIVESTREAM_URL = 12;
        int MODERATOR_URL = 13;
        int ROOM_ID = 14;
        int ROOM_NAME = 15;
        int COLOR = 16;
        int PHOTO_URL = 17;
        @SuppressWarnings("unused")
        int RELATED_CONTENT = 18;
        int TAGS = 19;
        int SPEAKER_NAMES = 20;

        int[] LINKS_INDICES = {
                YOUTUBE_URL,
                MODERATOR_URL,
                PDF_URL,
                NOTES_URL,
        };

        int[] LINKS_TITLES = {
                R.string.session_link_youtube,
                R.string.session_link_moderator,
                R.string.session_link_pdf,
                R.string.session_link_notes,
        };
    }

    interface SpeakersQuery {
        int _TOKEN = 0x3;

        String[] PROJECTION = {
                ScheduleContract.Speakers.SPEAKER_NAME,
                ScheduleContract.Speakers.SPEAKER_IMAGE_URL,
                ScheduleContract.Speakers.SPEAKER_COMPANY,
                ScheduleContract.Speakers.SPEAKER_ABSTRACT,
                ScheduleContract.Speakers.SPEAKER_URL,
        };

        int SPEAKER_NAME = 0;
        int SPEAKER_IMAGE_URL = 1;
        int SPEAKER_COMPANY = 2;
        int SPEAKER_ABSTRACT = 3;
        int SPEAKER_URL = 4;
    }

    private interface FeedbackQuery {
        int _TOKEN = 0x4;

        String[] PROJECTION = {
                ScheduleContract.Feedback.SESSION_ID
        };
    }
}