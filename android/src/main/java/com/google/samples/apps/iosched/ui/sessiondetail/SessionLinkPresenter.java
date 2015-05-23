package com.google.samples.apps.iosched.ui.sessiondetail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.google.samples.apps.iosched.util.AnalyticsManager;

/**
 * Created by MattDupree on 5/23/15.
 */
public class SessionLinkPresenter implements View.OnClickListener {

    private TextView mLinkView;
    private SessionDetailActivity mSessionDetailActivity;
    private Pair<Integer, Object> mLink;
    private String mTitleString;

    public SessionLinkPresenter(TextView linkView,
                                SessionDetailActivity sessionDetailActivity) {

        mLinkView = linkView;
        mSessionDetailActivity = sessionDetailActivity;
    }

    public void presentLink(Pair<Integer, Object> link, String titleString) {

        mLink = link;
        mTitleString = titleString;
        mLinkView.setText(mSessionDetailActivity.getString(link.first));
        mSessionDetailActivity.addLinkViewToLinksSection(mLinkView);
    }

    @Override
    public void onClick(View v) {

        fireLinkEvent(mLink.first);
        Intent intent = null;
        if (mLink.second instanceof Intent) {
            intent = (Intent) mLink.second;
        } else if (mLink.second instanceof String) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) mLink.second))
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        try {
            mSessionDetailActivity.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    /*
 * Event structure:
 * Category -> "Session Details"
 * Action -> Link Text
 * Label -> Session's Title
 * Value -> 0.
 */
    void fireLinkEvent(int actionId) {
    /* [ANALYTICS:EVENT]
     * TRIGGER:   Click on a link on the Session Details page.
     * CATEGORY:  'Session'
     * ACTION:    The link's name ("Watch Live", "Follow us on Google+", etc)
     * LABEL:     The session's title/subtitle.
     * [/ANALYTICS]
     */
        AnalyticsManager.sendEvent("Session", mSessionDetailActivity.getString(actionId),
                                   mTitleString, 0L);
    }
}
