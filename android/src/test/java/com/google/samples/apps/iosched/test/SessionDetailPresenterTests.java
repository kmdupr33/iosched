package com.google.samples.apps.iosched.test;

import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;

import com.google.samples.apps.iosched.repositories.AccountRepository;
import com.google.samples.apps.iosched.service.SessionCalendarServiceStarter;
import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailActivity;
import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailPresenter;
import com.google.samples.apps.iosched.ui.sessiondetail.SessionDetailPresenter.SessionsQuery;
import com.google.samples.apps.iosched.util.ColorUtils;
import com.google.samples.apps.iosched.util.ImageLoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * Created by MattDupree on 5/24/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionDetailPresenterTests {

    @Mock
    SessionDetailActivity mSessionDetailActivity;

    @Mock
    ImageLoader mImageLoader;

    @Mock
    Cursor mCursor;

    @Mock
    Loader<Cursor> mLoader;

    @Mock
    ColorUtils mColorUtils;

    @Mock
    AccountRepository mAccountRepository;

    @Mock
    Resources mResources;

    @Mock
    SessionCalendarServiceStarter mSessionCalendarServiceStarter;

    @Test
    public void shouldLaunchAddSessionToCalendarService() {

        when(mCursor.getInt(SessionsQuery.IN_MY_SCHEDULE)).thenReturn(0);
        when(mCursor.getLong(SessionsQuery.START)).thenReturn(System.currentTimeMillis() + 10000);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(
                mSessionDetailActivity, mImageLoader, mColorUtils, mAccountRepository, mResources,
                mSessionCalendarServiceStarter);

        sessionDetailPresenter.onLoadFinished(mLoader, mCursor);
        sessionDetailPresenter.onSessionStarred();
        sessionDetailPresenter.onStop();

        verify(mSessionCalendarServiceStarter).startAddSessionService(any(Uri.class), anyLong(),
                                                                      anyLong(), anyString(),
                                                                      anyString());
    }

    @Test
    public void shouldLaunchRemoveSessionFromCalendarService() {
        when(mCursor.getInt(SessionsQuery.IN_MY_SCHEDULE)).thenReturn(1);
        when(mCursor.getLong(SessionsQuery.START)).thenReturn(System.currentTimeMillis() + 10000);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(
                mSessionDetailActivity, mImageLoader, mColorUtils, mAccountRepository, mResources,
                mSessionCalendarServiceStarter);

        sessionDetailPresenter.onLoadFinished(mLoader, mCursor);
        sessionDetailPresenter.onStop();

        verify(mSessionCalendarServiceStarter).startRemoveSessionService(any(Uri.class), anyLong(),
                                                                         anyLong(), anyString());
    }

    @Test
    public void shouldDisplayFeedbackCard() {

        when(mCursor.getInt(SessionsQuery.IN_MY_SCHEDULE)).thenReturn(1);
        when(mCursor.getLong(SessionsQuery.START)).thenReturn(System.currentTimeMillis() - 1000);
        SessionDetailPresenter sessionDetailPresenter = new SessionDetailPresenter(
                mSessionDetailActivity,
                mImageLoader, mColorUtils, mAccountRepository, mResources,
                mSessionCalendarServiceStarter);

        sessionDetailPresenter.onLoadFinished(mLoader, mCursor);

        verify(mSessionDetailActivity, never()).hideFeedbackView();
    }


    @Before
    public void mockCursorReturnValues() {
        when(mCursor.getString(SessionsQuery.TAGS)).thenReturn("");
        when(mCursor.moveToFirst()).thenReturn(true);
        when(mLoader.getId()).thenReturn(SessionsQuery._TOKEN);
    }
}
