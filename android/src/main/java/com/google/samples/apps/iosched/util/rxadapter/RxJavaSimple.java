package com.google.samples.apps.iosched.util.rxadapter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

/**
 * Created by MattDupree on 6/10/15.
 */
public class RxJavaSimple {

    public static void main(String[] args) {

        ConnectableObservable<String> stringConnectableObservable
                                                = getSearchWidgetQueryStringObservable().publish();
        stringConnectableObservable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                //TODO Do something
            }
        });
        stringConnectableObservable.subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                //TODO Something else
            }
        });
        stringConnectableObservable.connect();

        String[] ioSessionTitles = {"Going Global With Google Play", "Keynote"};
        Observable.from(ioSessionTitles)
                  .filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                return s.contains("G");
            }
        })
                  .subscribe(new Action1<String>() {
                      @Override
                      public void call(String s) {
                          doSomethingWithFilteredData();
                      }
                  });
    }

    private static void doSomethingWithFilteredData() {


    }

    private static Observable<String> getSearchWidgetQueryStringObservable() {
        return Observable.just("test");
    }
}
