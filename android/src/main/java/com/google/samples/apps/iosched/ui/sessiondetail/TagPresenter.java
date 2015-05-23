package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.Intent;

import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.ui.BrowseSessionsActivity;

/**
 * Created by MattDupree on 5/23/15.
 */
public class TagPresenter {

    private final SessionTagViewTranslator mSessionTagViewTranslator;
    private final SessionDetailActivity mSessionDetailActivity;

    public TagPresenter(SessionTagViewTranslator sessionTagViewTranslator,
                        SessionDetailActivity sessionDetailActivity) {

        mSessionTagViewTranslator = sessionTagViewTranslator;
        mSessionDetailActivity = sessionDetailActivity;
    }

    public void presentTag(TagMetadata.Tag tag) {

        mSessionTagViewTranslator.setTagText(tag.getName());

        if (Config.Tags.CATEGORY_TOPIC.equals(tag.getCategory())) {

            mSessionTagViewTranslator.addTagColorDot(tag);
        }

        mSessionDetailActivity.addChipViewToTags(mSessionTagViewTranslator);
    }

    public void onTagClicked(TagMetadata.Tag tag) {
        mSessionDetailActivity.finish();
        Intent intent = new Intent(mSessionDetailActivity,
                                   BrowseSessionsActivity.class)
                .putExtra(BrowseSessionsActivity.EXTRA_FILTER_TAG, tag.getId())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mSessionDetailActivity.startActivity(intent);
    }
}
