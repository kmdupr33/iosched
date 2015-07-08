package com.google.samples.apps.iosched.ui.sessiondetail;

import dagger.Module;

/**
 *
 * Created by MattDupree on 7/8/15.
 */
@Module(injects = SessionDetailActivity.class)
public class SessionDetailModule {

    private SessionDetailActivity mSessionDetailActivity;

    public SessionDetailModule(SessionDetailActivity sessionDetailActivity) {
        mSessionDetailActivity = sessionDetailActivity;
    }
}
