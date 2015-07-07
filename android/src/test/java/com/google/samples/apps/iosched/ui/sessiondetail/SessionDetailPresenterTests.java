package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.SessionColorResolver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
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

    @Test
    public void shouldPresentSessionInfo() {
        //Arrange
        List<Speaker> speakers = new ArrayList<>();
        List<TagMetadata.Tag> tags = new ArrayList<>();
        SessionDetail sessionDetail = new SessionDetail();
        mSessionDetailDataLoader = new MockSessionDetailDataLoader(sessionDetail, speakers, tags);

        when(mSessionColorResolver.resolveSessionColor(anyInt())).thenReturn(DUMMY_COLOR);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(mSessionDetailView,
                                                                                   mSessionDetailDataLoader,
                                                                                   mSessionColorResolver);

        //Act
        sessionDetailPresenter.present();

        //Assert
        verify(mSessionDetailView).renderSessionSpeakers(speakers);
        verify(mSessionDetailView).renderSessionTags(tags);

        verify(mSessionDetailView).setSessionColor(DUMMY_COLOR);

    }

    private static class MockSessionDetailDataLoader implements SessionDetailDataLoader {
        public MockSessionDetailDataLoader(SessionDetail sessionDetail, List<Speaker> speakers, List<TagMetadata.Tag> tags) {
        }

        @Override
        public void getSpeakersObservable(SpeakersLoadedListener speakersLoadedListener) {

        }

        @Override
        public void loadSessionDetail(SessionDetailLoadedListener loadedListener) {

        }

        @Override
        public void getTagsObservable(TagsLoadedListener tagsLoadedListener) {

        }

        @Override
        public void getFeedbackObservable(FeedbackLoadedListener feedbackLoadedListener) {

        }
    }
}
