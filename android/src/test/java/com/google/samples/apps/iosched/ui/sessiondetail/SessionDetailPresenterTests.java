package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Created by MattDupree on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionDetailPresenterTests {

    @Mock
    SessionDetailView mSessionDetailView;

    SessionDetailDataLoader mSessionDetailDataLoader;

    @Test
    public void shouldPresentSessionInfo() {
        //Arrange
        List<Speaker> speakers = new ArrayList<>();
        List<TagMetadata.Tag> tags = new ArrayList<>();
        SessionDetail sessionDetail = new SessionDetail();
        mSessionDetailDataLoader = new MockSessionDetailDataLoader(sessionDetail, speakers, tags);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(mSessionDetailView,
                                                                                   mSessionDetailDataLoader);

        //Act
        sessionDetailPresenter.present();

        //Assert
        verify(mSessionDetailView).renderSessionSpeakers(speakers);
        verify(mSessionDetailView).renderSessionTags(tags);
        verify(mSessionDetailView).renderSessionDetails(sessionDetail);
    }

    private static class MockSessionDetailDataLoader implements SessionDetailDataLoader {
        public MockSessionDetailDataLoader(SessionDetail sessionDetail, List<Speaker> speakers, List<TagMetadata.Tag> tags) {
        }
    }
}
