package com.google.samples.apps.iosched.ui.sessiondetail;

import rx.Subscriber;

/**
 * Created by MattDupree on 5/31/15.
 */
public class AddedToSchedulSubscriber extends Subscriber<Boolean> {

    private SessionDetailActivity mSessionDetailActivity;

    public AddedToSchedulSubscriber(SessionDetailActivity sessionDetailActivity) {
        mSessionDetailActivity = sessionDetailActivity;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Boolean isSessionAdded) {

    }
}
