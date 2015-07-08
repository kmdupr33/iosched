package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.net.Uri;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.SessionColorResolver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by MattDupree on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionDetailPresenterTests {

    private static final int DUMMY_COLOR = -1;
    @Mock
    SessionDetailView mSessionDetailView;

    SessionDetailDataLoader mSessionDetailDataLoader;

    @Mock
    SessionColorResolver mSessionColorResolver;

    @Mock
    Context mContext;

    @Test
    public void shouldPresentSpeakers() {
        //Arrange
        List<Speaker> speakers = new ArrayList<>();
        List<TagMetadata.Tag> tags = new ArrayList<>();
        SessionDetail.Builder builder = new SessionDetail.Builder();
        builder.setTagsString("");
        SessionDetail sessionDetail = new SessionDetail(builder);
        mSessionDetailDataLoader = new MockSessionDetailDataLoader(sessionDetail, speakers, tags, false);

        when(mSessionColorResolver.resolveSessionColor(anyInt())).thenReturn(DUMMY_COLOR);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(mSessionDetailView,
                                                                                   mSessionDetailDataLoader,
                                                                                   mSessionColorResolver,
                                                                                   mContext);

        //Act
        sessionDetailPresenter.present();

        //Assert
        verify(mSessionDetailView).renderSessionSpeakers(speakers);
        verify(mSessionDetailView).renderSessionTags(tags);

        verify(mSessionDetailView).setSessionColor(DUMMY_COLOR);
        verify(mSessionDetailView).renderSessionTitles(sessionDetail);
        verify(mSessionDetailView).renderSessionPhoto(null);

        /*
        This will always be called because testOption.returnDefaultValues has been set to true
        and because the Presenter uses TextUtils.isEmpty() in its implementation.
         */
        verify(mSessionDetailView).enableSocialStreamMenuItem();

        verify(mSessionDetailView).renderSessionTags(tags);
        verify(mSessionDetailView).setAddScheduleButtonVisible(false);
        verify(mSessionDetailView).renderRequirements(null);

        verify(mSessionDetailView, never()).hideFeedbackCard();
        verify(mSessionDetailView, never()).hideSubmitFeedbackButton();
    }

    private static class MockSessionDetailDataLoader implements SessionDetailDataLoader {

        private final SessionDetail mSessionDetail;
        private final List<Speaker> mSpeakers;
        private final List<TagMetadata.Tag> mTags;
        private boolean mGaveFeedback;

        public MockSessionDetailDataLoader(SessionDetail sessionDetail, List<Speaker> speakers,
                                           List<TagMetadata.Tag> tags, boolean gaveFeedback) {
            mSessionDetail = sessionDetail;
            mSpeakers = speakers;
            mTags = tags;
            mGaveFeedback = gaveFeedback;
        }

        @Override
        public Subscription addSpeakersLoadedSubscriber(Subscriber<List<Speaker>> subscriber) {
            return Observable.just(mSpeakers).subscribe(subscriber);
        }

        @Override
        public Subscription addSessionDetailLoadedSubscriber(Subscriber<SessionDetail> subscriber) {
            return Observable.just(mSessionDetail).subscribe(subscriber);
        }

        @Override
        public Subscription addTagsLoadedSubscriber(Subscriber<List<TagMetadata.Tag>> subscriber) {
            return Observable.just(mTags).subscribe(subscriber);
        }

        @Override
        public Subscription addFeedbackLoadedSubscriber(Subscriber<Boolean> subscriber) {
            return Observable.just(mGaveFeedback).subscribe(subscriber);
        }

        @Override
        public void load() {

        }

        @Override
        public String getSessionId() {

            return null;
        }

        @Override
        public void reloadFeedback() {

        }

        @Override
        public Uri getSessionUri() {

            return null;
        }
    }

    private class TestContainer {

    }
}
