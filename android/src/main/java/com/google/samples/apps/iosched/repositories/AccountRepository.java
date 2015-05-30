package com.google.samples.apps.iosched.repositories;

import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailActivity;
import com.google.samples.apps.iosched.util.AccountUtils;

/**
 * Created by MattDupree on 5/24/15.
 */
public class AccountRepository {

    public boolean hasActiveAccount(SessionDetailActivity sessionDetailActivity) {
        return AccountUtils.hasActiveAccount(sessionDetailActivity);
    }
}
