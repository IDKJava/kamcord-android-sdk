package com.kamcord.app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kamcord.app.R;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reset_password, container, false);
        ButterKnife.inject(this, root);
        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void handleResetPasswordFailure(GenericResponse<?> responseWrapper)
    {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.resetPasswordProblem)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @OnClick(R.id.resetPasswordButton)
    public void resetPassword()
    {
        AppServerClient.getInstance().resetPassword(emailEditText.getEditableText().toString(),
                new Callback<GenericResponse<?>>() {
                    @Override
                    public void success(GenericResponse<?> responseWrapper, Response response) {
                        if( responseWrapper != null && responseWrapper.status != null
                                && responseWrapper.status.equals(StatusCode.OK) )
                        {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.checkYourEmail)
                                    .setNeutralButton(android.R.string.ok, null)
                                    .show();
                        }
                        else {
                            handleResetPasswordFailure(responseWrapper);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handleResetPasswordFailure(null);
                    }
                });
    }
}
