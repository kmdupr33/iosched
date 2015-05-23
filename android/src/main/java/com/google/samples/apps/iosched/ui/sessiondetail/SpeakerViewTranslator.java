package com.google.samples.apps.iosched.ui.sessiondetail;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.util.ImageLoader;
import com.google.samples.apps.iosched.util.UIUtils;

/**
 *
 * Created by MattDupree on 5/23/15.
 */
public class SpeakerViewTranslator {


    final TextView mSpeakerHeaderView;
    final ImageView mSpeakerImageView;
    final TextView mSpeakerAbstractView;
    View rootView;
    private ImageLoader mSpeakersImageLoader;
    private SpeakerDetailPresenter mPresenter;

    public SpeakerViewTranslator(View speakerView, ImageLoader speakersImageLoader) {

        rootView = speakerView;
        mSpeakersImageLoader = speakersImageLoader;

        mSpeakerHeaderView = (TextView) speakerView
                .findViewById(R.id.speaker_header);
        mSpeakerImageView = (ImageView) speakerView
                .findViewById(R.id.speaker_image);
        mSpeakerAbstractView = (TextView) speakerView
                .findViewById(R.id.speaker_abstract);
    }

    public void renderSpeakerHeader(String speakerHeader) {

        mSpeakerHeaderView.setText(speakerHeader);
        mSpeakerImageView.setContentDescription(
                rootView.getContext().getString(R.string.speaker_googleplus_profile,
                                                speakerHeader));
    }

    public void renderSpeakerAbstract(String speakerAbstract) {

        UIUtils.setTextMaybeHtml(mSpeakerAbstractView, speakerAbstract);
    }

    public void renderSpeakerImage(final String speakerImageUrl) {

        mSpeakersImageLoader.loadImage(speakerImageUrl, mSpeakerImageView);
        mSpeakerImageView.setEnabled(true);
        mSpeakerImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mPresenter.onSpeakerWithUrlClicked(speakerImageUrl);
            }
        });
    }

    public void setPresenter(SpeakerDetailPresenter presenter) {

        mPresenter = presenter;
        mPresenter.presentSpeaker();
    }

    public void hideSpeakerImage() {

        mSpeakerImageView.setEnabled(false);
        mSpeakerImageView.setOnClickListener(null);
    }
}
