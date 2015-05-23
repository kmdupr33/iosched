package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailPresenter.SpeakersQuery;
import com.google.samples.apps.iosched.util.UIUtils;

/**
 * Presenters the view that displays info about a speaker
 * Created by MattDupree on 5/23/15.
 */
public class SpeakerDetailPresenter {

    private final SpeakerViewTranslator mSpeakerViewTranslator;
    private final SessionDetailActivity mSessionDetailActivity;
    private Cursor mCursor;

    public SpeakerDetailPresenter(SpeakerViewTranslator speakerViewTranslator,
                                  SessionDetailActivity sessionDetailActivity, Cursor cursor) {


        mSpeakerViewTranslator = speakerViewTranslator;
        mSessionDetailActivity = sessionDetailActivity;
        mCursor = cursor;
    }

    public void presentSpeaker() {
        final String speakerName = mCursor.getString(SpeakersQuery.SPEAKER_NAME);
        if (TextUtils.isEmpty(speakerName)) {
            return;
        }

        final String speakerImageUrl = mCursor.getString(SpeakersQuery.SPEAKER_IMAGE_URL);
        final String speakerCompany = mCursor.getString(SpeakersQuery.SPEAKER_COMPANY);
        final String speakerAbstract = mCursor.getString(SpeakersQuery.SPEAKER_ABSTRACT);

        String speakerHeader = speakerName;
        if (!TextUtils.isEmpty(speakerCompany)) {
            speakerHeader += ", " + speakerCompany;
        }

        mSpeakerViewTranslator.renderSpeakerHeader(speakerHeader);
        mSpeakerViewTranslator.renderSpeakerAbstract(speakerAbstract);

        if (!TextUtils.isEmpty(speakerImageUrl)) {
            mSpeakerViewTranslator.renderSpeakerImage(speakerImageUrl);
        } else {
            mSpeakerViewTranslator.hideSpeakerImage();
        }

        mSessionDetailActivity.addSpeakerViewToSpeakersGroup(mSpeakerViewTranslator);
    }

    public void onSpeakerWithUrlClicked(String speakerImageUrl) {
        Intent speakerProfileIntent = new Intent(Intent.ACTION_VIEW,
                                                 Uri.parse(speakerImageUrl));
        //noinspection deprecation
        speakerProfileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        UIUtils.preferPackageForIntent(mSessionDetailActivity,
                                       speakerProfileIntent,
                                       UIUtils.GOOGLE_PLUS_PACKAGE_NAME);
        mSessionDetailActivity.startActivity(speakerProfileIntent);

    }
}
