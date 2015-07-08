package com.google.samples.apps.iosched.util;

import rx.Subscriber;

/**
 * Created by MattDupree on 7/7/15.
 */
public class BaseSubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        throw new IllegalStateException(e);
    }

    @Override
    public void onNext(T t) {

    }
}
