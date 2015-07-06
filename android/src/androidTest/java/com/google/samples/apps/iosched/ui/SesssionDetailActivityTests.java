package com.google.samples.apps.iosched.ui;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.ui.phone.MapActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

/**
 *
 * Created by MattDupree on 7/6/15.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SesssionDetailActivityTests {

    @Rule
    public IntentsTestRule<BrowseSessionsActivity> mActivityRule = new IntentsTestRule<>(BrowseSessionsActivity.class);

    /*
    We start from the BrowseSessionsActivity and click into the particular session that we want to test.
    We do this so that the intent used to launch the SessionDetailActivity has all the right "stuff."
     */
    @Before
    public void setUp() throws Exception {
        viewGoingGlobalWithGooglePlaySessionDetail();
    }

    //----------------------------------------------------------------------------------
    // Tests
    //----------------------------------------------------------------------------------
    @Test
    public void testGoingGlobalWithGooglePlaySessionDetailInfoAppears() {
        onView(withId(R.id.session_title)).check(matches(withText("Going global with Google Play")));
        onView(withId(R.id.session_subtitle)).check(matches(withText("Wed, 1:00–1:45 PM in Room 4")));
        onView(withId(R.id.session_abstract)).check(matches(withText(startsWith("Think your app or game has what it takes"))));
        onView(withText("Distribute")).check(matches(isDisplayed()));
        onView(withText("Android")).check(matches(isDisplayed()));
        onView(withText("Games")).check(matches(isDisplayed()));
        onView(withId(R.id.session_speakers_block)).perform(scrollTo());
        onView(withText(startsWith("Hirotaka manages the overall"))).check(matches(isDisplayed()));
        onView(withText(startsWith("Koh is currently a Business Development"))).check(matches(isDisplayed()));
    }

    @Test
    public void testShouldToggleAddScheduleIcon() {
        onView(withId(R.id.add_schedule_button)).check(matches(not(isChecked())));
        onView(withId(R.id.add_schedule_button)).perform(click());
        onView(isRoot()).perform(pressBack());
        viewGoingGlobalWithGooglePlaySessionDetail();
        onView(withId(R.id.add_schedule_button)).check(matches(isChecked()));
        //Reset the button back to an unchecked state. TODO Normally not necessary if you run tests with clean install on CI server
        onView(withId(R.id.add_schedule_button)).perform(click());
    }

    @Test
    public void shouldLaunchMapOfGoingGlobalWithGooglePlaySessionLocation() {
        onView(withId(R.id.menu_map_room)).perform(click());
        intended(allOf(hasComponent(MapActivity.class.getName()),
                       hasExtra(BaseMapActivity.EXTRA_ROOM, "room4")));
    }

    @Test
    public void shouldLaunchShareGoingGlobalWithGooglePlaySessionInfo() {
        onView(withId(R.id.menu_share)).perform(click());
        String SHARE_TEXT = "Check out 'Going global with Google Play' at #io14 #android  https://www.google.com/events/io/schedule/session/07fd8b0d-80bf-e311-b297-00155d5066d7";
        intended(hasTargetIntent(hasExtra(Intent.EXTRA_TEXT, SHARE_TEXT)));
    }

    @Test
    public void shouldLaunchGooglePlusHashtagSearch() {
        onView(withId(R.id.menu_social_stream)).perform(click());
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://plus.google.com/s/%23io14%20%23android")));
    }

    @Test
    public void shouldLaunchSubmitFeedbackForGoingGlobalWithGooglePlaySession() {
        onView(withText("Submit Feedback")).perform(scrollTo(), click());
        intended(allOf(hasAction(Intent.ACTION_VIEW),
                       hasComponent(SessionFeedbackActivity.class.getName()),
                       hasData("content://com.google.samples.apps.iosched/sessions/07fd8b0d-80bf-e311-b297-00155d5066d7")));
    }

    //----------------------------------------------------------------------------------
    // Helpers
    //----------------------------------------------------------------------------------
    private void viewGoingGlobalWithGooglePlaySessionDetail() {
    /*
    The CollectionView used to display Sessions has an adapter whose getItem() method returns an
    Integer. Unfortunately, because of the way that onData() works, this means that we can
    only select an item in the list by passing in the index of the item that we'd like to
    select in the CollectionView. Index 1 corresponds to the session entitled "Going Global with
    Google Play"
     */
        onData(allOf(is(instanceOf(Integer.class)), is(1)))
                .inAdapterView(withId(R.id.sessions_collection_view))
                /*
                Each item within the adapter actually contains info on two different Sessions. We
                match the session that has the title "Going global with Google Play"
                 */
                .onChildView(withText("Going global with Google Play"))
                .perform(click());
    }

    private static Matcher<Intent> hasTargetIntent(final Matcher<Intent> intentMatcher) {
        return new TypeSafeMatcher<Intent>() {
            @Override
            protected boolean matchesSafely(Intent item) {
                Intent intent = item.getParcelableExtra(Intent.EXTRA_INTENT);
                return intent != null && intentMatcher.matches(intent);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has target Intent: ");
                description.appendDescriptionOf(intentMatcher);
            }
        };
    }
}
