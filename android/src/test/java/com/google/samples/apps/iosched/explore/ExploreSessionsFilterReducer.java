package com.google.samples.apps.iosched.explore;

import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.explore.ExploreSessionsActivity;
import com.google.samples.apps.iosched.explore.TagFilterHolder;
import com.google.samples.apps.iosched.model.TagMetadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertFalse;

/**
 * Created by mattdupree on 6/9/16.
 */
@RunWith(JUnit4.class)
public class ExploreSessionsFilterReducer {
    @Test
    public void shouldToggleFilter() {
        //Arrange
        ExploreSessionsActivity.Reducer reducer = new ExploreSessionsActivity.Reducer();
        TagFilterHolder tagFilterHolder = new TagFilterHolder();
        tagFilterHolder.add("AudienceGrowth", Config.Tags.CATEGORY_THEME);
        final TagMetadata.Tag tag = new TagMetadata.Tag("AudienceGrowth", "name", Config.Tags.CATEGORY_THEME, 1,
                "some stuff", 1);
        final ExploreSessionsActivity.Click clickAction = new ExploreSessionsActivity.Click(tag);

        //Act
        final TagFilterHolder holder = reducer.reduce(tagFilterHolder, clickAction);

        //Assert
        assertFalse(holder.contains("AudienceGrowth"));
    }
}
