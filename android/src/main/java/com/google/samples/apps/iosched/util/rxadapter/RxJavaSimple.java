package com.google.samples.apps.iosched.util.rxadapter;

import com.google.api.services.drive.model.User;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Created by MattDupree on 6/10/15.
 */
public class RxJavaSimple {

    public static void main(String[] args) {

        //Demoing multiple subscribers
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

        //Demoing schedulers and operators
        String[] ioSessionTitles = {"Going Global With Google Play", "Keynote"};
        Observable.from(ioSessionTitles)
                  .filter(new Func1<String, Boolean>() {

                      @Override
                      public Boolean call(String s) {
                          return s.contains("G");
                      }
                  })
                  .subscribeOn(Schedulers.io()) //"created" thread
                  .observeOn(AndroidSchedulers.mainThread()) //"consumed" thread
                  .subscribe(new Action1<String>() {

                      @Override
                      public void call(String s) {

                          doSomethingWithFilteredData();
                      }
                  });

        //demo observable creation
        final UserFetcher userFetcher = new UserFetcher();
        Observable.create(new Observable.OnSubscribe<List<User>>() {
            @Override
            public void call(Subscriber<? super List<User>> subscriber) {
                try {
                    List<User> users = userFetcher.fetchUsers();
                    subscriber.onNext(users); //analagous to iterator's next()
                    subscriber.onCompleted(); //analgous to iterator's hasNext()
                } catch (IOException e) {
                    subscriber.onError(e); //analgous to iterator throwing exception
                }
            }
        });



    }

    //Just for demo code
    private static void doSomethingWithFilteredData() {
    }

    //Just for demo code
    private static Observable<String> getSearchWidgetQueryStringObservable() {
        return Observable.just("test");
    }

    //Just for demo
    private static class UserFetcher {
        public List<User> fetchUsers() throws IOException {
            return null;
        }
    }
}
