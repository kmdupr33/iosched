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

    void renderSessionDetails(SessionDetail loadedSessionDetail);
}
