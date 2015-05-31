/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.ui.sessiondetail;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.bitmap.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.plus.PlusOneButton;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.model.TagMetadata;
import com.google.samples.apps.iosched.provider.ScheduleContract;
import com.google.samples.apps.iosched.repositories.AccountRepository;
import com.google.samples.apps.iosched.service.SessionCalendarServiceStarter;
import com.google.samples.apps.iosched.ui.BaseActivity;
import com.google.samples.apps.iosched.ui.MyScheduleActivity;
import com.google.samples.apps.iosched.ui.widget.CheckableFrameLayout;
import com.google.samples.apps.iosched.ui.widget.MessageCardView;
import com.google.samples.apps.iosched.ui.widget.ObservableScrollView;
import com.google.samples.apps.iosched.util.AnalyticsManager;
import com.google.samples.apps.iosched.util.BeamUtils;
import com.google.samples.apps.iosched.util.ColorUtils;
import com.google.samples.apps.iosched.util.ImageLoader;
import com.google.samples.apps.iosched.util.SessionsHelper;
import com.google.samples.apps.iosched.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that shows detail information for a session, including session title, abstract,
 * time information, speaker photos and bios, etc.
 */
public class SessionDetailActivity extends BaseActivity implements
        ObservableScrollView.Callbacks {

    private static final int[] SECTION_HEADER_RES_IDS = {
            R.id.session_links_header,
            R.id.session_speakers_header,
            R.id.session_requirements_header,
            R.id.related_videos_header,
    };
    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    public static final String TRANSITION_NAME_PHOTO = "photo";
    private SessionDetailPresenter mSessionDetailPresenter;

    private Handler mHandler = new Handler();

    private MenuItem mSocialStreamMenuItem;
    private MenuItem mShareMenuItem;

    private View mScrollViewChild;
    private TextView mTitle;
    private TextView mSubtitle;
    private PlusOneButton mPlusOneButton;

    private ObservableScrollView mScrollView;
    CheckableFrameLayout mAddScheduleButton;

    private TextView mAbstract;
    private LinearLayout mTags;
    private ViewGroup mTagsContainer;
    private TextView mRequirements;
    private View mHeaderBox;
    private View mDetailsContainer;

    private ImageLoader mSpeakersImageLoader, mNoPlaceholderImageLoader;
    private List<Runnable> mDeferredUiOperations = new ArrayList<>();

    private int mPhotoHeightPixels;
    private int mHeaderHeightPixels;
    private int mAddScheduleButtonHeightPixels;

    private boolean mHasPhoto;
    private View mPhotoViewContainer;
    ImageView mPhotoView;

    private Runnable mTimeHintUpdaterRunnable = null;

    private TextView mSubmitFeedbackView;
    private float mMaxHeaderElevation;
    private float mFABElevation;

    private View mRequirementsBlock;
    private ViewGroup mSpeakersGroup;
    private TextView mTimeHintView;
    private ViewGroup mLinkContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.tryTranslateHttpIntent(this);
        BeamUtils.tryUpdateIntentFromBeam(this);
        boolean shouldBeFloatingWindow = shouldBeFloatingWindow();
        if (shouldBeFloatingWindow) {
            setupFloatingWindow();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(shouldBeFloatingWindow
                                          ? R.drawable.ic_ab_close : R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });

        if (savedInstanceState == null) {
            Uri sessionUri = getIntent().getData();
            BeamUtils.setBeamSessionUri(this, sessionUri);
        }

        if (mSpeakersImageLoader == null) {
            mSpeakersImageLoader = new ImageLoader(this, R.drawable.person_image_empty);
        }
        if (mNoPlaceholderImageLoader == null) {
            mNoPlaceholderImageLoader = new ImageLoader(this);
        }

        mSessionDetailPresenter = new SessionDetailPresenter(this,
                                                             new ColorUtils(), new AccountRepository(),
                                                             getResources(),
                                                             new SessionCalendarServiceStarter(this));
        mSessionDetailPresenter.mSessionUri = getIntent().getData();

        if (mSessionDetailPresenter.mSessionUri == null) {
            return;
        }

        mSessionDetailPresenter.mSessionId = ScheduleContract.Sessions
                .getSessionId(mSessionDetailPresenter.mSessionUri);

        mFABElevation = getResources().getDimensionPixelSize(R.dimen.fab_elevation);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.session_detail_max_header_elevation);

        mHandler = new Handler();

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }

        mScrollViewChild = findViewById(R.id.scroll_view_child);
        mScrollViewChild.setVisibility(View.INVISIBLE);

        mTimeHintView = (TextView) findViewById(R.id.time_hint);

        mDetailsContainer = findViewById(R.id.details_container);
        mHeaderBox = findViewById(R.id.header_session);
        mTitle = (TextView) findViewById(R.id.session_title);
        mSubtitle = (TextView) findViewById(R.id.session_subtitle);
        mPhotoViewContainer = findViewById(R.id.session_photo_container);
        mPhotoView = (ImageView) findViewById(R.id.session_photo);

        mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
        mAbstract = (TextView) findViewById(R.id.session_abstract);
        mRequirements = (TextView) findViewById(R.id.session_requirements);
        mTags = (LinearLayout) findViewById(R.id.session_tags);
        mTagsContainer = (ViewGroup) findViewById(R.id.session_tags_container);

        mAddScheduleButton = (CheckableFrameLayout) findViewById(R.id.add_schedule_button);
        mAddScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean starred = !mSessionDetailPresenter.mStarred;
                mSessionDetailPresenter.onSessionStarred();
                //TODO Move this to presenter
                SessionsHelper helper = new SessionsHelper(SessionDetailActivity.this);
                showStarred(starred, true);
                helper.setSessionStarred(mSessionDetailPresenter.mSessionUri, starred,
                                         mSessionDetailPresenter.mTitleString);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mAddScheduleButton.announceForAccessibility(starred ?
                            getString(R.string.session_details_a11y_session_added) :
                            getString(R.string.session_details_a11y_session_removed));
                }

                /* [ANALYTICS:EVENT]
                 * TRIGGER:   Add or remove a session from My Schedule.
                 * CATEGORY:  'Session'
                 * ACTION:    'Starred' or 'Unstarred'
                 * LABEL:     Session title/subtitle.
                 * [/ANALYTICS]
                 */
                AnalyticsManager.sendEvent(
                        "Session", starred ? "Starred" : "Unstarred",
                        mSessionDetailPresenter.mTitleString, 0L);
            }
        });

        ViewCompat.setTransitionName(mPhotoView, TRANSITION_NAME_PHOTO);

        LoaderManager manager = getLoaderManager();
        mSessionDetailPresenter.startLoad(manager);
    }

    @Override
    public Intent getParentActivityIntent() {
        // TODO(mangini): make this Activity navigate up to the right screen depending on how it was launched
        return new Intent(this, MyScheduleActivity.class);
    }

    private void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.session_details_floating_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.session_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.4f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    private boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            // isFloatingWindow flag is not defined in theme
            return false;
        }
        return (floatingWindowFlag.data != 0);
    }

    void recomputePhotoAndScrollingMetrics() {
        mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = 0;
        if (mHasPhoto) {
            mPhotoHeightPixels = (int) (mPhotoView.getWidth() / PHOTO_ASPECT_RATIO);
            mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);
        }

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScrollView == null) {
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mAddScheduleButtonHeightPixels = mAddScheduleButton.getHeight();
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);
        mAddScheduleButton.setTranslationY(newTop + mHeaderHeightPixels
                - mAddScheduleButtonHeightPixels / 2);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(UIUtils.getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);
        ViewCompat.setElevation(mAddScheduleButton, gapFillProgress * mMaxHeaderElevation
                + mFABElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSessionDetailPresenter.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSessionDetailPresenter.onStop();
    }

    private void setTextSelectable(TextView tv) {
        if (tv != null && !tv.isTextSelectable()) {
            tv.setTextIsSelectable(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTimeHintUpdaterRunnable != null) {
            mHandler.removeCallbacks(mTimeHintUpdaterRunnable);
        }
    }

    void showWatchNowCard() {
        final MessageCardView messageCardView = (MessageCardView) findViewById(R.id.live_now_card);
        messageCardView.show();
        messageCardView.setListener(mSessionDetailPresenter);
    }

    void showGiveFeedbackCard() {
        final MessageCardView messageCardView = (MessageCardView) findViewById(R.id.give_feedback_card);
        messageCardView.show();
        messageCardView.setListener(mSessionDetailPresenter);
    }

    void enableSocialStreamMenuItemDeferred() {
        mDeferredUiOperations.add(new Runnable() {

            @Override
            public void run() {

                mSocialStreamMenuItem.setVisible(true);
            }
        });
        tryExecuteDeferredUiOperations();
    }

    void showStarredDeferred(final boolean starred, final boolean allowAnimate) {
        mDeferredUiOperations.add(new Runnable() {

            @Override
            public void run() {

                showStarred(starred, allowAnimate);
            }
        });
        tryExecuteDeferredUiOperations();
    }

    private void showStarred(boolean starred, boolean allowAnimate) {

        mSessionDetailPresenter.mStarred = starred;

        mAddScheduleButton.setChecked(mSessionDetailPresenter.mStarred, allowAnimate);

        ImageView iconView = (ImageView) mAddScheduleButton.findViewById(R.id.add_schedule_icon);
        getLUtils().setOrAnimatePlusCheckIcon(iconView, starred, allowAnimate);
        mAddScheduleButton.setContentDescription(getString(starred
                                                                   ? R.string.remove_from_schedule_desc
                                                                   : R.string.add_to_schedule_desc));
    }

    void setupShareMenuItemDeferred(final String hashTag, final String url) {
        mDeferredUiOperations.add(new Runnable() {

            @Override
            public void run() {

                new SessionsHelper(SessionDetailActivity.this)
                        .tryConfigureShareMenuItem(mShareMenuItem,
                                                   R.string.share_template,
                                                   mSessionDetailPresenter.mTitleString, hashTag,
                                                   url);
            }
        });
        tryExecuteDeferredUiOperations();
    }

    private void tryExecuteDeferredUiOperations() {
        if (mSocialStreamMenuItem != null) {
            for (Runnable r : mDeferredUiOperations) {
                r.run();
            }
            mDeferredUiOperations.clear();
        }
    }

    void updateEmptyView(boolean speakersCursor, boolean sessionCursor, boolean hasSummaryContent) {
        findViewById(android.R.id.empty).setVisibility(
                (speakersCursor && sessionCursor && !hasSummaryContent)
                        ? View.VISIBLE
                        : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.session_detail, menu);
        mSocialStreamMenuItem = menu.findItem(R.id.menu_social_stream);
        mShareMenuItem = menu.findItem(R.id.menu_share);
        tryExecuteDeferredUiOperations();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return mSessionDetailPresenter.onOptionsItemSelected(item);
    }

    public void renderSessionColor(int sessionColor) {
        mHeaderBox.setBackgroundColor(sessionColor);
        mPhotoViewContainer.setBackgroundColor(UIUtils.scaleSessionColorToDefaultBG(
                sessionColor));
        getLUtils().setStatusBarColor(UIUtils.scaleColor(
                sessionColor, 0.8f, false));

        for (int resId : SessionDetailActivity.SECTION_HEADER_RES_IDS) {
            ((TextView) findViewById(resId)).setTextColor(
                    sessionColor);
        }

    }

    public void renderTitle(String titleString, long start, long end,
                            String roomName, boolean liveStreamUrl) {
        mTitle.setText(titleString);
        SessionSubtitlePresenter sessionSubtitlePresenter = new SessionSubtitlePresenter(mSubtitle);
        sessionSubtitlePresenter.presentSubtitle(start, end, roomName, liveStreamUrl);
    }

    public void renderSessionAbstract(String sessionAbstract) {
        UIUtils.setTextMaybeHtml(mAbstract, sessionAbstract);
        mAbstract.setVisibility(View.VISIBLE);

    }

    public void hideAbstract() {
        mAbstract.setVisibility(View.GONE);

    }

    public void renderRequirements(String sessionRequirements) {
        UIUtils.setTextMaybeHtml(mRequirements, sessionRequirements);
        mRequirementsBlock.setVisibility(View.VISIBLE);

    }

    public void hideRequirementsBlock() {
        mRequirementsBlock.setVisibility(View.GONE);

    }

    public void onSessionQueryComplete() {
        // Build requirements section
        mRequirementsBlock = findViewById(R.id.session_requirements_block);
        mLinkContainer = (ViewGroup) findViewById(R.id.links_container);

        // Build related videos section
        final ViewGroup relatedVideosBlock = (ViewGroup) findViewById(R.id.related_videos_block);
        relatedVideosBlock.setVisibility(View.GONE);
    }

    public void enableScrolling() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                onScrollChanged(0, 0); // trigger scroll handling
                mScrollViewChild.setVisibility(View.VISIBLE);
                //mAbstract.setTextIsSelectable(true);
            }
        });

    }


    public void hideFeedbackView() {
        final MessageCardView giveFeedbackCardView = (MessageCardView) findViewById(R.id.give_feedback_card);
        if (giveFeedbackCardView != null) {
            giveFeedbackCardView.setVisibility(View.GONE);
        }
        if (mSubmitFeedbackView != null) {
            mSubmitFeedbackView.setVisibility(View.GONE);
        }

    }



    public void onSpeakersQueryCompleted() {
        mSpeakersGroup = (ViewGroup) findViewById(R.id.session_speakers_block);

    }

    public SpeakerViewTranslator makeSpeakerViewTranslator() {

        View speakerView = getLayoutInflater()
                .inflate(R.layout.speaker_detail, mSpeakersGroup, false);

        return new SpeakerViewTranslator(speakerView, mSpeakersImageLoader);

    }

    public void addSpeakerViewToSpeakersGroup(
            SpeakerViewTranslator speakerViewHolder) {
        mSpeakersGroup.addView(speakerViewHolder.rootView);

    }


    public void hideTags() {
        mTagsContainer.setVisibility(View.GONE);

    }

    public void renderTags(List<TagMetadata.Tag> tags) {
        mTagsContainer.setVisibility(View.VISIBLE);
        mTags.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (final TagMetadata.Tag tag : tags) {
            TextView chipView = (TextView) inflater.inflate(
                    R.layout.include_session_tag_chip, mTags, false);
            SessionTagViewTranslator sessionTagViewTranslator = new SessionTagViewTranslator(chipView);
            final TagPresenter tagPresenter = new TagPresenter(sessionTagViewTranslator, this);
            tagPresenter.presentTag(tag);
            chipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tagPresenter.onTagClicked(tag);
                }
            });
        }
    }

    public void renderSpeakers(Cursor cursor) {
        // Remove all existing speakers (everything but first child, which is the header)
        for (int i = mSpeakersGroup.getChildCount() - 1; i >= 1; i--) {
            mSpeakersGroup.removeViewAt(i);
        }

        cursor.moveToPosition(-1); // move to just before first record
        while (cursor.moveToNext()) {

            SpeakerViewTranslator speakerViewHolder = makeSpeakerViewTranslator();
            //TODO There's a circular dependency between the Presenter and the ViewTranslator. Introducing a seperate responder object does not resolve this since the responder may have to update the ui based on user interaction. Revisit to find a way of breaking dependnecy.
            SpeakerDetailPresenter presenter = new SpeakerDetailPresenter(speakerViewHolder, this, cursor);
            speakerViewHolder.setPresenter(presenter);
        }

        mSpeakersGroup.setVisibility(cursor.getCount() > 0 ? View.VISIBLE : View.GONE);
    }

    public void addChipViewToTags(SessionTagViewTranslator sessionTagViewTranslator) {
        mTags.addView(sessionTagViewTranslator.mChipView);
    }

    public void showPlusOneButton(String url) {
        mPlusOneButton.initialize(url, 0);
        mPlusOneButton.setVisibility(View.VISIBLE);
    }

    public void hidePlusOneButton() {
        mPlusOneButton.setVisibility(View.GONE);
    }

    public void renderTimeHint(String timeHint) {
        mTimeHintView.setVisibility(View.VISIBLE);
        mTimeHintView.setText(timeHint);
    }

    public void hideTimeHint() {
        mTimeHintView.setVisibility(View.GONE);
    }

    public void renderLinks(List<Pair<Integer, Object>> links, String titleString) {
        // Compile list of links (I/O live link, submit feedback, and normal links)
        mLinkContainer.removeAllViews();

        // Render links
        if (links.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < links.size(); i++) {
                final Pair<Integer, Object> link = links.get(i);

                // Create link view
                TextView linkView = (TextView) inflater.inflate(R.layout.list_item_session_link,
                                                                mLinkContainer, false);
                if (link.first == R.string.session_feedback_submitlink) {
                    mSubmitFeedbackView = linkView;
                }

                SessionLinkPresenter sessionLinkPresenter = new SessionLinkPresenter(linkView, this);
                sessionLinkPresenter.presentLink(link, titleString);
                linkView.setOnClickListener(sessionLinkPresenter);
            }

            findViewById(R.id.session_links_header).setVisibility(View.VISIBLE);
            findViewById(R.id.links_container).setVisibility(View.VISIBLE);

        }
    }

    public void hideLinks() {
        findViewById(R.id.session_links_header).setVisibility(View.GONE);
        findViewById(R.id.links_container).setVisibility(View.GONE);
    }

    void addLinkViewToLinksSection(TextView linkView) {
        int columns = getResources().getInteger(R.integer.links_columns);
        // Place it inside a container
        if (columns == 1) {
            mLinkContainer.addView(linkView);
        } else {
            // create a new link row
            LinearLayout currentLinkRowView = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.include_link_row, mLinkContainer, false);
            if (mLinkContainer.getChildCount() % columns == 0) {
                currentLinkRowView.setWeightSum(columns);
                mLinkContainer.addView(currentLinkRowView);
            }

            ((LinearLayout.LayoutParams) linkView.getLayoutParams()).width = 0;
            ((LinearLayout.LayoutParams) linkView.getLayoutParams()).weight = 1;
            currentLinkRowView.addView(linkView);
        }
    }

    public void renderSessionPhoto(String photo) {
        mHasPhoto = true;
        mNoPlaceholderImageLoader.loadImage(photo, mPhotoView,
                                            new RequestListener<String>() {

                                                @Override
                                                public void onException(Exception e, String url,
                                                                        Target target) {

                                                    mHasPhoto = false;
                                                    recomputePhotoAndScrollingMetrics();
                                                }

                                                @Override
                                                public void onImageReady(String url,
                                                                         Target target,
                                                                         boolean b,
                                                                         boolean b2) {

                                                    // Trigger image transition
                                                    recomputePhotoAndScrollingMetrics();
                                                }
                                            });
    }

    public void setAddScheduleButtonEnabled(boolean shouldShowAddScheduleButton) {
        mAddScheduleButton.setVisibility(shouldShowAddScheduleButton ? View.VISIBLE : View.INVISIBLE);
    }
}
