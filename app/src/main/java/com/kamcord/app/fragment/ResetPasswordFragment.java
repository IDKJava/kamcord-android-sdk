package com.kamcord.app.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kamcord.app.R;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.utils.KeyboardUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pplunkett on 5/25/15.
 */
public class ResetPasswordFragment extends Fragment {

    @InjectView(R.id.emailEditText)
    EditText emailEditText;
    @InjectView(R.id.resetPasswordButton)
    Button resetPasswordButton;

    private boolean viewsAreValid = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reset_password, container, false);
        ButterKnife.inject(this, root);
        viewsAreValid = true;
        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        viewsAreValid = false;
        ButterKnife.reset(this);
    }

    private void handleResetPasswordFailure(GenericResponse<?> responseWrapper)
    {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.resetPasswordProblem)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @OnFocusChange(R.id.emailEditText)
    public void editTextFocusChange(View v, boolean hasFocus) {
        if (isResumed()) {
            KeyboardUtils.setSoftKeyboardVisibility(v, getActivity(), hasFocus);
        }
    }

    @OnClick(R.id.resetPasswordButton)
    public void resetPassword()
    {
        AppServerClient.getInstance().resetPassword(emailEditText.getEditableText().toString(),
                new Callback<GenericResponse<?>>() {
                    @Override
                    public void success(GenericResponse<?> responseWrapper, Response response) {
                        if( !viewsAreValid ) {
                            return;
                        }
                        if( responseWrapper != null && responseWrapper.status != null
                                && responseWrapper.status.equals(StatusCode.OK) )
                        {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.checkYourEmail)
                                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which) {
                                            getActivity().getSupportFragmentManager().popBackStack();
                                        }
                                    })
                                    .show();


                        }
                        else {
                            handleResetPasswordFailure(responseWrapper);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if( !viewsAreValid ) {
                            return;
                        }
                        handleResetPasswordFailure(null);
                    }
                });
    }
}
