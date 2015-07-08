package com.google.samples.apps.iosched.ui.sessiondetail;

import com.google.samples.apps.iosched.io.model.Speaker;
import com.google.samples.apps.iosched.model.TagMetadata;

import java.util.List;

import rx.Observable;

/**
 * Created by MattDupree on 7/7/15.
 */
public interface SessionDetailDataLoader {

    Observable<List<Speaker>> getSpeakersObservable();

    Observable<SessionDetail> getSessionDetailObservable();

    Observable<List<TagMetadata.Tag>> getTagsObservable();

    Observable<Boolean> getFeedbackObservable();

    void load();
}
