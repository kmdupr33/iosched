package com.google.samples.apps.iosched.framework;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import com.google.samples.apps.iosched.explore.ExploreIOFragment;
import com.google.samples.apps.iosched.explore.ExploreModel;
import com.google.samples.apps.iosched.util.ThrottledContentObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by mattdupree on 6/15/16.
 */
public class PresenterFragmentImplCharacterization {

    PresenterFragmentImpl mPresenterFragSpy;

    @Mock
    FragmentManager mFragmentManager;

    UpdatableView mUpdatableView;

    Model mModel;


    @Before
    public void setUp() throws Exception {
        mPresenterFragSpy = spy(new PresenterFragmentImpl());
        mUpdatableView = mock(ExploreIOFragment.class);
        MockitoAnnotations.initMocks(this);
        mModel = mock(ExploreModel.class);
        when(mFragmentManager.findFragmentById(0)).thenReturn((Fragment) mUpdatableView);
    }

    @Test
    public void characterizeOnAttachIfRegisteredContentObservers() throws Exception {
        final ExploreModel.ExploreQueryEnum sessions = ExploreModel.ExploreQueryEnum.SESSIONS;
        final QueryEnum[] queriesToRun = {sessions};
        characterizeOnAttach(new Actor() {
            @Override
            public void act(Activity activity) {
                mPresenterFragSpy.registerContentObserverOnUri(Uri.EMPTY, queriesToRun);
                mPresenterFragSpy.onAttach(activity);
            }
        }, new Asserter() {
            @Override
            public void doAssert(ContentResolver contentResolver) {
                // This characterization isn't perfect because this method gets called with specific arguments, but it will cover the changes we are planning on making
                verify(contentResolver).registerContentObserver(eq(Uri.EMPTY), eq(true), notNull(ThrottledContentObserver.class));
            }
        });
    }

    @Test
    public void characterizeOnAttachIfNoRegisteredContentObservers() throws Exception {
        characterizeOnAttach(new Actor() {
            @Override
            public void act(Activity activity) {
                mPresenterFragSpy.onAttach(activity);
            }
        }, new Asserter() {
            @Override
            public void doAssert(ContentResolver contentResolver) {
                verifyZeroInteractions(contentResolver);
            }
        });
    }

    @Test
    public void characterizeOnDetachIfRegisterContentObserver() throws Exception {

        characterizeOnDetach(new Actor() {
            @Override
            public void act(Activity activity) {
                mPresenterFragSpy.registerContentObserverOnUri(Uri.EMPTY, new QueryEnum[]{ExploreModel.ExploreQueryEnum.SESSIONS});
                mPresenterFragSpy.onDetach();
            }
        }, new Asserter() {
            @Override
            public void doAssert(ContentResolver contentResolver) {
                assertNull(mPresenterFragSpy.getUpdatableView());
                assertNull(mPresenterFragSpy.getModel());
                verify(contentResolver).unregisterContentObserver(any(ContentObserver.class));
            }
        });
    }

    @Test
    public void characterizeOnDetachIfNoRegisteredContentObserver() throws Exception {
        characterizeOnAttach(new Actor() {
            @Override
            public void act(Activity activity) {
                mPresenterFragSpy.onDetach();
            }
        }, new Asserter() {
            @Override
            public void doAssert(ContentResolver contentResolver) {
                assertNull(mPresenterFragSpy.getUpdatableView());
                assertNull(mPresenterFragSpy.getModel());
            }
        });
    }

    @Test
    public void testOnActivityCreatedIfNoInitialQueriesToLoad() throws Exception {

        mPresenterFragSpy.configure(mFragmentManager, 0, mModel, new QueryEnum[]{}, new UserActionEnum[]{});

        mPresenterFragSpy.onActivityCreated(mock(Bundle.class));
        assertNotNull(mPresenterFragSpy.getLoaderIdlingResource());
        //noinspection unchecked
        verify(mUpdatableView).displayData(anyObject(), any(QueryEnum.class));
    }

    @Test
    public void testOnActivityCreatedIfInitialQueriesToLoad() throws Exception {

        final ExploreModel.ExploreQueryEnum sessions = ExploreModel.ExploreQueryEnum.SESSIONS;
        mPresenterFragSpy.configure(mFragmentManager, 0, mModel, new QueryEnum[]{sessions}, new UserActionEnum[]{});

        final LoaderManager loaderManager = mock(LoaderManager.class);
        when(mPresenterFragSpy.getLoaderManager()).thenReturn(loaderManager);

        mPresenterFragSpy.onActivityCreated(mock(Bundle.class));

        assertNotNull(mPresenterFragSpy.getLoaderIdlingResource());
        verify(loaderManager).initLoader(eq(sessions.getId()), isNull(Bundle.class), notNull(LoaderManager.LoaderCallbacks.class));
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

    private void characterizeOnAttach(Actor actor, Asserter asserter) {
        // Arrange
        mPresenterFragSpy.configure(mFragmentManager, 0, mModel, new QueryEnum[]{}, new UserActionEnum[]{});


        final Activity activity = mock(Activity.class);
        final ContentResolver contentResolver = mock(ContentResolver.class);
        when(activity.getContentResolver()).thenReturn(contentResolver);

        // Act
        actor.act(activity);

        // Assert
        asserter.doAssert(contentResolver);
    }

    private void characterizeOnDetach(Actor actor, Asserter asserter) {
        mPresenterFragSpy.configure(mFragmentManager, 0, mModel, new QueryEnum[]{}, new UserActionEnum[]{});

        final Activity activity = mock(Activity.class);
        final ContentResolver contentResolver = mock(ContentResolver.class);

        when(activity.getContentResolver()).thenReturn(contentResolver);
        when(mPresenterFragSpy.getActivity()).thenReturn(activity);

        actor.act(activity);

        asserter.doAssert(contentResolver);
    }

    private interface Actor {
        void act(Activity activity);
    }

    private interface Asserter {
        void doAssert(ContentResolver contentResolver);
    }
}