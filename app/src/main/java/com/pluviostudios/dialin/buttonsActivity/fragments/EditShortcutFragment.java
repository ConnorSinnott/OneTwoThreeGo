package com.pluviostudios.dialin.buttonsActivity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.defaultActions.ActionLaunchApplication;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by spectre on 9/16/16.
 */
public class EditShortcutFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditShortcutFragment";
    public static final String EXTRA_ACTION_ID = "extra_action_id";
    public static final String EXTRA_ACTION_ARGUMENTS = "extra_action_arguments";

    private View mRoot;
    private TextView mNoConfigText;
    private View mEditActionListItem;
    private ImageView mListItemActionImageView;
    private TextView mListItemActionTextView;
    private Button mButtonOk;
    private Button mButtonCancel;

    protected Action mAction;

    public static EditShortcutFragment buildEditFragment() {
        EditShortcutFragment newFragment = new EditShortcutFragment();
        return newFragment;
    }

    public static EditShortcutFragment buildEditFragment(Action action) {

        EditShortcutFragment editFragment = buildEditFragment();

        int actionId = action.getActionId();
        ArrayList<String> actionArguments = action.getActionParameters();

        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ACTION_ID, actionId);
        extras.putStringArrayList(EXTRA_ACTION_ARGUMENTS, actionArguments);

        editFragment.setArguments(extras);

        return editFragment;

    }

    protected void initialize() {
        mNoConfigText = (TextView) mRoot.findViewById(R.id.fragment_edit_action_no_config);
        mEditActionListItem = mRoot.findViewById(R.id.fragment_edit_action_list_item);
        mListItemActionImageView = (ImageView) mRoot.findViewById(R.id.list_item_action_image);
        mListItemActionTextView = (TextView) mRoot.findViewById(R.id.list_item_action_text_view);
        mButtonOk = (Button) mRoot.findViewById(R.id.fragment_edit_action_ok);
        mButtonCancel = (Button) mRoot.findViewById(R.id.fragment_edit_action_cancel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_edit_action, container, false);
        initialize();

        Bundle extras = null;
        if (savedInstanceState != null) {
            extras = savedInstanceState;
        } else {
            if (getArguments() != null) {
                extras = getArguments();
            }
        }

        mAction = new ActionLaunchApplication();

        if (extras != null) {

            // Add arguments if they exist
            if (extras.containsKey(EXTRA_ACTION_ARGUMENTS)) {
                ArrayList<String> actionArguments = extras.getStringArrayList(EXTRA_ACTION_ARGUMENTS);
                mAction.setParameters(actionArguments);
            }

        }

        // Update the view that shows the action name and its icon
        updateCurrentAction();

        mEditActionListItem.setOnClickListener(this);
        mButtonOk.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        return mRoot;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAction != null) {
            outState.putInt(EXTRA_ACTION_ID, mAction.getActionId());
        }
        super.onSaveInstanceState(outState);
    }

    protected void updateCurrentAction() {

        // If the action does not have a configuration fragment, than delete the currently displayed fragment, it does not belong there anymore
        if (!mAction.hasConfigurationFragment()) {
            clearConfigFragment();
        } else {

            // Otherwise, there should be a fragment being displayed, so clear up the frame
            mNoConfigText.setVisibility(View.GONE);

            // Get the currently displayed configuration fragment if it exists
            ConfigurationFragment currentlyDisplayedFragment = (ConfigurationFragment) getChildFragmentManager().findFragmentById(R.id.fragment_edit_action_configure_frame);

            // If there is currently a configuration fragment being displayed, determine if it belongs to the action
            if (currentlyDisplayedFragment != null) {

                // Check to see if the fragment being displayed belongs to the action, or if it exists because the user was looking at a different action
                if (currentlyDisplayedFragment.getParentActionId() != mAction.getActionId()) {
                    displayNewConfigFragment();
                } else {
                    mAction.setConfigurationFragment(currentlyDisplayedFragment);
                }

            } else {
                displayNewConfigFragment();
            }

        }

    }

    protected void displayNewConfigFragment() {

        // If there is a different fragment being displayed, create the new configuration fragment
        ConfigurationFragment newConfigurationFragment = mAction.getConfigurationFragment();

        if (getArguments() != null) {
            if (getArguments().containsKey(EXTRA_ACTION_ID) && mAction.getActionId() == getArguments().getInt(EXTRA_ACTION_ID)) {
                if (getArguments().containsKey(EXTRA_ACTION_ARGUMENTS)) {
                    newConfigurationFragment.setActionParameters(getArguments().getStringArrayList(EXTRA_ACTION_ARGUMENTS));
                }
            }
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_edit_action_configure_frame, newConfigurationFragment)
                .commit();

    }

    protected void clearConfigFragment() {

        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_edit_action_configure_frame);

        if (currentFragment != null) {
            mNoConfigText.setVisibility(View.VISIBLE);
            getChildFragmentManager().beginTransaction().remove(currentFragment).commit();
            getChildFragmentManager().popBackStack();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_edit_action_ok:
                finishConfigure();
                break;

            case R.id.fragment_edit_action_cancel:
                EventBus.getDefault().post(new EditActionFragmentEvents.Outgoing.OnCancel());
                break;

        }

    }

    protected void finishConfigure() {

        // On OK return the configured fragment using OnActionConfigured
        mAction.saveParameters();

        EventBus.getDefault().post(new EditActionFragmentEvents.Outgoing.OnConfigured(mAction));

    }

}
