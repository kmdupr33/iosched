package com.google.samples.apps.iosched.framework;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;

import com.google.samples.apps.iosched.explore.ExploreIOFragment;
import com.google.samples.apps.iosched.explore.ExploreModel;
import com.google.samples.apps.iosched.util.ThrottledContentObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mattdupree on 6/15/16.
 */
public class PresenterFragmentImplCharacterization {

    PresenterFragmentImpl mPresenterFragment;

    PresenterFragmentImpl mPresenterFragSpy;

    @Mock
    FragmentManager mFragmentManager;

    @Mock
    Model mModel;


    @Before
    public void setUp() throws Exception {
        mPresenterFragment = new PresenterFragmentImpl();
        mPresenterFragSpy = spy(mPresenterFragment);
        MockitoAnnotations.initMocks(this);
        when(mFragmentManager.findFragmentById(0)).thenReturn(mock(ExploreIOFragment.class));
    }

    @Test
    public void characterizeOnAttach() throws Exception {


        // Arrange
        mPresenterFragment.configure(mFragmentManager, 0, mModel, new QueryEnum[]{}, new UserActionEnum[]{});

        final ExploreModel.ExploreQueryEnum sessions = ExploreModel.ExploreQueryEnum.SESSIONS;
        final QueryEnum[] queriesToRun = {sessions};

        final Activity activity = mock(Activity.class);
        final ContentResolver contentResolver = mock(ContentResolver.class);
        when(activity.getContentResolver()).thenReturn(contentResolver);

        // Act
        // TODO There should be a version of this test where we don't call registerContentObserver and we verify that the content resolver mock is not touched
        mPresenterFragment.registerContentObserverOnUri(Uri.EMPTY, queriesToRun);
        // Normally, mocks returning mocks is a sign that the design of the SUT smells, but we're writing characterization tests here.
        mPresenterFragment.onAttach(activity);

        // Assert
        // TODO This characterization isn't perfect because this method gets called with specific arguments, need to refactor to support the characterization.
        verify(contentResolver).registerContentObserver(any(Uri.class), anyBoolean(), any(ThrottledContentObserver.class));

    }

    @Test
    public void characterizeOnDetach() throws Exception {

        mPresenterFragSpy.configure(mFragmentManager, 0, mModel, new QueryEnum[]{}, new UserActionEnum[]{});

        final Activity activity = mock(Activity.class);
        final ContentResolver contentResolver = mock(ContentResolver.class);

        when(activity.getContentResolver()).thenReturn(contentResolver);
        when(mPresenterFragSpy.getActivity()).thenReturn(activity);

        // TODO There should be a version of this test where we don't call registerContentObserver and we verify that the content resolver mock is not touched
        mPresenterFragSpy.registerContentObserverOnUri(Uri.EMPTY, new QueryEnum[]{ExploreModel.ExploreQueryEnum.SESSIONS});
        mPresenterFragSpy.onDetach();

        assertNull(mPresenterFragment.mUpdatableView);
        assertNull(mPresenterFragment.mModel);
        verify(contentResolver).unregisterContentObserver(any(ContentObserver.class));

    }

    @Test
    public void testCleanUp() throws Exception {

    }

    @Test
    public void testOnActivityCreated() throws Exception {

    }

    @Test
    public void testOnCreateLoader() throws Exception {

    }

    @Test
    public void testCreateLoader() throws Exception {

    }

    @Test
    public void testOnLoadFinished() throws Exception {

    }

    @Test
    public void testProcessData() throws Exception {

    }

    @Test
    public void testOnLoaderReset() throws Exception {

    }

    @Test
    public void testOnUserAction() throws Exception {

    }

    @Test
    public void testRegisterContentObserverOnUri() throws Exception {

    }
}