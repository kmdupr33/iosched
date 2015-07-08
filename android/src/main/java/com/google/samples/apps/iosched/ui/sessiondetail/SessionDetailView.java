package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;

import java.util.List;

/**
 * Created by MattDupree on 7/7/15.
 */
public interface SessionDetailView {
    void renderSessionSpeakers(List<Speaker> speakers);

    void renderSessionTags(List<TagMetadata.Tag> loadedSessionTags);

    void setSessionColor(int sessionColor);

    void setEmptyViewVisible(boolean showEmptyView);

    void renderSessionTitles(SessionDetail sessionDetail);

    void renderSessionPhoto(String photoUrl);

    void showWatchNowCard();

    void showGiveFeedbackCard();

    void enableSocialStreamMenuItem();

    void setAddScheduleButtonVisible(boolean visible);

    void showStarred(boolean inMySchedule, boolean animate);

    void renderSessionAbstract(String sessionAbstract);

    void updatePlusOneButton(String sessionUrl, boolean isKeynote);

    void renderRequirements(String sessionRequirements);

    void hideRelatedVideos();

    void showLiveStreamLink();

    void showFeedbackLink();

    void showSessionLink(String link);

    void updateTimeBasedUi(boolean dismissedWatchLivestreamCard,
                           boolean alreadyGaveFeedback, boolean initStarred,
                           String sessionId, SessionDetail sessionDetail);

    void renderTimeHint(String timeHint);

    void hideSessionCardView();

    void hideSubmitFeedbackButton();

    void hideWatchNowView();

    void hideFeedbackCard();
}
