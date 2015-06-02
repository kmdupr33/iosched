package com.google.samples.apps.iosched.ui.sessiondetail;

import android.database.Cursor;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.provider.ScheduleContract;

import rx.Subscription;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Get's the data we need for the SessionDetailActivity
 *
 * Created by MattDupree on 6/1/15.
 */
public class SessionDetailDataLoader {

    private final ConnectableObservable<Cursor> mSessionsDataObservable;
    private final ConnectableObservable<Cursor> mSpeakerDataObservable;
    private final ConnectableObservable<Cursor> mFeedbackDataObservable;
    private final ConnectableObservable<Cursor> mTagMetadataObservable;
    private CompositeSubscription mCompositeSubscription;

    public SessionDetailDataLoader(
            ConnectableObservable<Cursor> sessionsDataObservable,
            ConnectableObservable<Cursor> speakerDataObservable,
            ConnectableObservable<Cursor> feedbackDataObservable,
            ConnectableObservable<Cursor> tagMetadataObservable) {

        mSessionsDataObservable = sessionsDataObservable;
        mSpeakerDataObservable = speakerDataObservable;
        mFeedbackDataObservable = feedbackDataObservable;
        mTagMetadataObservable = tagMetadataObservable;
    }

    public void loadData(final SessionDetailDataLoadListener sessionDetailDataLoadedListener) {
        mCompositeSubscription = new CompositeSubscription();
        mSessionsDataObservable.subscribe(new Action1<Cursor>() {

            @Override
            public void call(Cursor cursor) {
                sessionDetailDataLoadedListener.onSessionQueryComplete(cursor);
            }
        });
        Subscription subscription = mSessionsDataObservable.connect();
        mCompositeSubscription.add(subscription);

        mSpeakerDataObservable.subscribe(new Action1<Cursor>() {

            @Override
            public void call(Cursor cursor) {
                sessionDetailDataLoadedListener.onSpeakersQueryComplete(cursor);
            }
        });
        subscription = mSpeakerDataObservable.connect();
        mCompositeSubscription.add(subscription);

        mFeedbackDataObservable.subscribe(new Action1<Cursor>() {

            @Override
            public void call(Cursor cursor) {
                sessionDetailDataLoadedListener.onFeedbackQueryComplete(cursor);
            }
        });
        subscription = mFeedbackDataObservable.connect();
        mCompositeSubscription.add(subscription);

        mTagMetadataObservable.subscribe(new Action1<Cursor>() {

            @Override
            public void call(Cursor cursor) {
                sessionDetailDataLoadedListener.onTagMetadataQueryComplete(cursor);
            }
        });
        subscription = mTagMetadataObservable.connect();
        mCompositeSubscription.add(subscription);
    }

    /**
     * {@link ScheduleContract.Sessions} query parameters.
     */
    public interface SessionsQuery {
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

    interface FeedbackQuery {
        int _TOKEN = 0x4;

        String[] PROJECTION = {
                ScheduleContract.Feedback.SESSION_ID
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

    /**
     * Created by MattDupree on 6/1/15.
     */
    public static interface SessionDetailDataLoadListener {

        void onTagMetadataQueryComplete(Cursor cursor);

        void onSessionQueryComplete(Cursor cursor);

        void onFeedbackQueryComplete(Cursor cursor);

        void onSpeakersQueryComplete(Cursor cursor);
    }
}
