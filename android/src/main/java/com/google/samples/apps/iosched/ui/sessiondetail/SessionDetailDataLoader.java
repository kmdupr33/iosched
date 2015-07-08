package com.google.samples.apps.iosched.ui.sessiondetail;

import android.net.Uri;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by MattDupree on 7/7/15.
 */
public interface SessionDetailDataLoader {

    Subscription addSpeakersLoadedSubscriber(Subscriber<List<Speaker>> subscriber);

    Subscription addSessionDetailLoadedSubscriber(Subscriber<SessionDetail> subscriber);

    Subscription addTagsLoadedSubscriber(Subscriber<List<TagMetadata.Tag>> subscriber);

    Subscription addFeedbackLoadedSubscriber(Subscriber<Boolean> subscriber);

    void load();

    String getSessionId();

    void reloadFeedback();

    Uri getSessionUri();
}
