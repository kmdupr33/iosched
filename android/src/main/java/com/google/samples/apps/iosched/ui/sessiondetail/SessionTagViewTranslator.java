package com.google.samples.apps.iosched.ui.sessiondetail;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.widget.TextView;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.model.TagMetadata;

/**
 *
 * Created by MattDupree on 5/23/15.
 */
class SessionTagViewTranslator {

    final TextView mChipView;
    private final int mTagColorDotSize;

    public SessionTagViewTranslator(TextView chipView) {

        mChipView = chipView;
        mTagColorDotSize = mChipView.getContext().getResources().getDimensionPixelSize(
                R.dimen.tag_color_dot_size);
    }

    public void setTagText(String name) {

        mChipView.setText(name);
    }

    public void addTagColorDot(TagMetadata.Tag tag) {

        ShapeDrawable colorDrawable = new ShapeDrawable(new OvalShape());
        colorDrawable.setIntrinsicWidth(mTagColorDotSize);
        colorDrawable.setIntrinsicHeight(mTagColorDotSize);
        colorDrawable.getPaint().setStyle(Paint.Style.FILL);
        colorDrawable.getPaint().setColor(tag.getColor());
        mChipView.setCompoundDrawablesWithIntrinsicBounds(colorDrawable,
                                                          null, null, null);
    }
}
