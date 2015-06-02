package com.google.samples.apps.iosched.ui.sessiondetail;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by MattDupree on 5/31/15.
 */
public class InitLoaderOnSubscribe<D> implements Observable.OnSubscribe<D> {

    private final int mLoaderId;
    protected LoaderManager mLoaderManager;
    private Loader<D> mLoader;

    public InitLoaderOnSubscribe(int loaderId, LoaderManager loaderManager, Loader<D> loader) {
        mLoaderId = loaderId;
        mLoaderManager = loaderManager;
        mLoader = loader;
    }

    @Override
    public void call(final Subscriber<? super D> subscriber) {
        mLoaderManager.initLoader(mLoaderId, null,
                                  new LoaderManager.LoaderCallbacks<D>() {
                                      @Override
                                      public Loader<D> onCreateLoader(int id, Bundle args) {
                                          return mLoader;
                                      }

                                      @Override
                                      public void onLoadFinished(Loader<D> loader,
                                                                 D data) {
                                          subscriber.onNext(data);
                                      }

                                      @Override
                                      public void onLoaderReset(Loader<D> loader) {
                                          //TODO Make sure we don't need to explose this callback
                                      }
                                  });
    }
}
