package com.google.samples.apps.iosched.util.rxadapter;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by MattDupree on 6/10/15.
 */
public class RxJavaSimple {

    public static void main(String[] args) {

        Observable<String> queryStringObservable = getSearchWidgetQueryStringObservable();
        queryStringObservable.subscribe(new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                //Do something
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private static Observable<String> getSearchWidgetQueryStringObservable() {
        return Observable.just("test");
    }
}
