package com.google.samples.apps.iosched.ui.sessiondetail;

import android.net.Uri;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by MattDupree on 7/8/15.
 */
public class AndroidLoaderSessionDetailDataLoader implements SessionDetailDataLoader {

    @Override
    public Subscription addSpeakersLoadedSubscriber(Subscriber<List<Speaker>> subscriber) {
        return null;
    }

    @Override
    public Subscription addSessionDetailLoadedSubscriber(Subscriber<SessionDetail> subscriber) {

        return null;
    }

    @Override
    public Subscription addTagsLoadedSubscriber(Subscriber<List<TagMetadata.Tag>> subscriber) {

        return null;
    }

    @Override
    public Subscription addFeedbackLoadedSubscriber(Subscriber<Boolean> subscriber) {

        return null;
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
