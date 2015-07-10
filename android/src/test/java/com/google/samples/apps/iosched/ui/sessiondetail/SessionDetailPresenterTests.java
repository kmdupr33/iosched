package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Context;
import android.net.Uri;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.SessionColorResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static org.mockito.Matchers.anyString;
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
    private SessionDetailPresenter mSessionDetailPresenter;

    @Before
    public void initPresenterWithMocks() {
        mSessionDetailPresenter = new SessionDetailPresenter(mSessionDetailView,
                                                             mSessionColorResolver,
                                                             mContext);
    }

    @Test
    public void shouldPresentSpeakers() {
        //Arrange
        List<Speaker> speakers = new ArrayList<>();

        //Act
        mSessionDetailPresenter.presentSpeakers(speakers);

        //Assert
        verify(mSessionDetailView).renderSessionSpeakers(speakers);
    }

    @Test
    public void shouldPresentSessionColor() {
        SessionDetail sessionDetail = new SessionDetail(new SessionDetail.Builder());

        mSessionDetailPresenter.presentSessionColor(sessionDetail);

        when(mSessionColorResolver.resolveSessionColor(sessionDetail.getColor())).thenReturn(DUMMY_COLOR);
        verify(mSessionColorResolver.resolveSessionColor(sessionDetail.getColor()));
        verify(mSessionDetailView).setSessionColor(DUMMY_COLOR);
    }

    @Test
    public void shouldPresentSessionTitles() {
        SessionDetail.Builder builder = new SessionDetail.Builder();
        String sessionTitle = "Going global with Google Play";
        SessionDetail sessionDetail = new SessionDetail(builder);

        mSessionDetailPresenter.presentSessionTitles(sessionDetail, mContext);

        verify(mSessionDetailView).setSessionTitle(sessionTitle);
        //TODO Test that subtitle creation logic works elsewhere
        verify(mSessionDetailView).setSessionSubtitle(anyString());
    }

    @Test
    public void shouldHideAddScheduleButtonBecauseSessionIsKeynote() {
        SessionDetail.Builder builder = new SessionDetail.Builder();
        builder.setTagsString("keynote");
        SessionDetail sessionDetail = new SessionDetail(builder);
        mSessionDetailPresenter.presentSessionStarred(sessionDetail);

        verify(mSessionDetailView).setAddScheduleButtonVisible(false);
    }

    @Test
    public void shouldHideAddScheduleButtonBecauseUserAccountIsNotActive() {
        //Arrange
        SessionDetail sessionDetail = new SessionDetail(new SessionDetail.Builder());
        //Act
        mSessionDetailPresenter.presentSessionStarred(sessionDetail);
        //Assert
        verify(mSessionDetailView).setAddScheduleButtonVisible(false);
    }

    @Test
    public void shouldShowSessionStarred() {
        //Arrange
        SessionDetail.Builder builder = new SessionDetail.Builder();
        builder.setInMySchedule(true);
        SessionDetail sessionDetail = new SessionDetail(builder);
        //Act
        mSessionDetailPresenter.presentSessionStarred(sessionDetail);
        //Assert
        verify(mSessionDetailView).showStarred(true, false);
    }

    @Test
    public void shouldEnableSocialStreamMenuItem() {
        //Act
        mSessionDetailPresenter.presentSocialStreamMenuItem("#nonEmptyHashTag");

        verify(mSessionDetailView).enableSocialStreamMenuItem();
    }

    @Test
    public void shouldPresentTags() {
        TagMetadata tagMetadata = new TagMetadata();

        mSessionDetailPresenter.presentTags(tagMetadata, "#android #is #awesome");
        List<TagMetadata.Tag> tags = new ArrayList<>(3);
//        verify(mSessionDetailView).renderSessionTags(tagMetadata, );
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
