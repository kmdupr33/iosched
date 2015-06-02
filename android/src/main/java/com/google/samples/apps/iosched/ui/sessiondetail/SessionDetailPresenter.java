package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.repositories.AccountRepository;
import com.google.samples.apps.iosched.ui.SessionFeedbackActivity;
import com.google.samples.apps.iosched.ui.SessionLivestreamActivity;
import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailDataLoader.SessionsQuery;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.ColorUtils;
import com.google.samples.apps.iosched.util.TimeUtils;
import com.google.samples.apps.iosched.util.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;


public class SessionDetailPresenter implements SessionDetailDataLoader.SessionDetailDataLoadListener {

    private static final int TIME_HINT_UPDATE_INTERVAL = 10000; // 10 sec
    private static final String TAG = SessionDetailPresenter.class.getSimpleName();
    static final int TAG_METADATA_TOKEN = 0x5;
    private final SessionDetailActivity mSessionDetailActivity;
    private AccountRepository mAccountRepository;
    private ColorUtils mColorUtils;
    private Resources mResources;
    private String mSessionId;
    private Uri mSessionUri;
    private long mSessionStart;
    private long mSessionEnd;
    private String mTitleString;
    private boolean mInitStarred;

    private TagMetadata mTagMetadata;

    private String mUrl;
    private String mTagsString;

    private boolean mIsKeynote;
    private String mLiveStreamUrl;
    private boolean mHasLivestream;

    private Runnable mTimeHintUpdaterRunnable;
    private Handler mHandler = new Handler();
    private boolean mAlreadyGaveFeedback;
    private boolean mSpeakersCursor;
    private boolean mSessionCursor;
    private boolean mHasSummaryContent;
    private boolean mDismissedWatchLivestreamCard;

    public SessionDetailPresenter(SessionDetailActivity sessionDetailActivity,
                                  AccountRepository accountRepository,
                                  ColorUtils colorUtils,
                                  Resources resources) {
        mSessionDetailActivity = sessionDetailActivity;
        mAccountRepository = accountRepository;
        mColorUtils = colorUtils;
        mResources = resources;
    }

    public void present(Uri sessionUri,
                        String sessionId,
                        SessionDetailDataLoader sessionDetailDataLoader) {
        mSessionUri = sessionUri;
        mSessionId = sessionId;
        sessionDetailDataLoader.loadData(this);
    }

    void onDismissedWatchLivesteamCard() {
        mDismissedWatchLivestreamCard = true;
    }

    void presentSessionStarred(boolean starred) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String accessibilityString;
            if (starred) {
                accessibilityString = mSessionDetailActivity
                        .getString(R.string.session_details_a11y_session_added);
            } else {
                accessibilityString = mSessionDetailActivity
                        .getString(R.string.session_details_a11y_session_removed);
            }

            mSessionDetailActivity.renderToggleSessionAddButtonAccessibility(accessibilityString);
        }

        mSessionDetailActivity.showStarred(starred, true);

                /* [ANALYTICS:EVENT]
                 * TRIGGER:   Add or remove a session from My Schedule.
                 * CATEGORY:  'Session'
                 * ACTION:    'Starred' or 'Unstarred'
                 * LABEL:     Session title/subtitle.
                 * [/ANALYTICS]
                 */
        //TODO Kill this static method
        AnalyticsManager.sendEvent(
                "Session", starred ? "Starred" : "Unstarred",
                mTitleString, 0L);

    }

    @Override
    public void onTagMetadataQueryComplete(Cursor cursor) {
        mTagMetadata = new TagMetadata(cursor);
        cursor.close();
        tryRenderTags();
    }

    /**
     * Handle {@link SessionsQuery} {@link Cursor}.
     * @param cursor contains seesionquery data
     */
    @Override
    public void onSessionQueryComplete(Cursor cursor) {

        mSessionCursor = true;
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
            sessionColor = mResources.getColor(R.color.default_session_color);
        } else {
            // make sure it's opaque
            sessionColor = mColorUtils.setColorAlpha(sessionColor, 255);
        }

        mSessionDetailActivity.renderSessionColor(sessionColor);


        mLiveStreamUrl = cursor.getString(SessionsQuery.LIVESTREAM_URL);
        mHasLivestream = !TextUtils.isEmpty(mLiveStreamUrl);

        // Format the time this session occupies
        mSessionStart = cursor.getLong(SessionsQuery.START);
        mSessionEnd = cursor.getLong(SessionsQuery.END);
        final String roomName = cursor.getString(SessionsQuery.ROOM_NAME);

        mSessionDetailActivity
                .renderTitle(mTitleString, mSessionStart, mSessionEnd, roomName, mHasLivestream);

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

        final String hashTag = cursor.getString(SessionsQuery.HASHTAG);
        if (!TextUtils.isEmpty(hashTag)) {
            mSessionDetailActivity.enableSocialStreamMenuItemDeferred();
        }

        final boolean inMySchedule = cursor.getInt(SessionsQuery.IN_MY_SCHEDULE) != 0;

        mSessionDetailActivity.setupShareMenuItemDeferred(mTitleString, hashTag, mUrl);

        // Handle Keynote as a special case, where the user cannot remove it
        // from the schedule (it is auto added to schedule on sync)
        mTagsString = cursor.getString(SessionsQuery.TAGS);
        mIsKeynote = mTagsString.contains(Config.Tags.SPECIAL_KEYNOTE);
        boolean shouldShowAddScheduleButton = mAccountRepository
                .hasActiveAccount(mSessionDetailActivity) && !mIsKeynote;
        mSessionDetailActivity.setAddScheduleButtonEnabled(shouldShowAddScheduleButton);

        tryRenderTags();

        if (!mIsKeynote) {
            mSessionDetailActivity.showStarredDeferred(mInitStarred = inMySchedule, false);
        }

        final String sessionAbstract = cursor.getString(SessionsQuery.ABSTRACT);
        if (!TextUtils.isEmpty(sessionAbstract)) {
            mSessionDetailActivity.renderSessionAbstract(sessionAbstract);
            mHasSummaryContent = true;
        } else {
            mSessionDetailActivity.hideAbstract();
        }

        updatePlusOneButton();

        final String sessionRequirements = cursor.getString(SessionsQuery.REQUIREMENTS);
        if (!TextUtils.isEmpty(sessionRequirements)) {
            mSessionDetailActivity.renderRequirements(sessionRequirements);
            mHasSummaryContent = true;
        } else {
            mSessionDetailActivity.hideRequirementsBlock();
        }

        // Build links section
        buildLinksSection(cursor);

        mSessionDetailActivity.updateEmptyView(mSpeakersCursor, mSessionCursor, mHasSummaryContent);

        updateTimeBasedUi(mSessionDetailActivity);
        mSessionDetailActivity.enableScrolling();

        //TODO Technical detail. Should be moved to adapter (i.e., SessionDetailActivity)
        mTimeHintUpdaterRunnable = new Runnable() {

            @Override
            public void run() {

                updateTimeBasedUi(mSessionDetailActivity);
                mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mTimeHintUpdaterRunnable, TIME_HINT_UPDATE_INTERVAL);
    }

    @Override
    public void onFeedbackQueryComplete(Cursor cursor) {
        // Is there existing feedback for this session?
        mAlreadyGaveFeedback = cursor.getCount() > 0;

        if (mAlreadyGaveFeedback) {
            mSessionDetailActivity.hideFeedbackView();
        }
        LOGD(TAG,
             "User " + (mAlreadyGaveFeedback ? "already gave" : "has not given") + " feedback for session.");
        cursor.close();
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

    @Override
    public void onSpeakersQueryComplete(Cursor cursor) {
        mSpeakersCursor = true;
        mSessionDetailActivity.onSpeakersQueryCompleted();
        mSessionDetailActivity.renderSpeakers(cursor);
        mSessionDetailActivity.updateEmptyView(mSpeakersCursor, mSessionCursor, mHasSummaryContent);
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

    void updatePlusOneButton() {
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
                && !SessionDetailResponder.sDismissedFeedbackCard.contains(mSessionId)) {
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

    //TODO Apparently, this method is used by Responder and Presenter. Consider how to avoid duplicating defintion.
    private Intent getFeedbackIntent() {
        return new Intent(Intent.ACTION_VIEW, mSessionUri, mSessionDetailActivity,
                SessionFeedbackActivity.class);
    }
}