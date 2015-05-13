package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.utils.AccountManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginFragment extends Fragment {

    @InjectView(R.id.usernameEditText) EditText userNameEditText;
    @InjectView(R.id.passwordEditText) EditText passwordEditText;
    @InjectView(R.id.loginButton) Button loginButton;
    @InjectView(R.id.forgotPasswordTextView) TextView forgotPasswordTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.inject(this, root);
        return root;
    }

    @Override
    public void onDestroyView()
    {
        ButterKnife.reset(this);
    }

    @OnClick(R.id.loginButton)
    public void login()
    {
        String username = userNameEditText.getEditableText().toString();
        String password = passwordEditText.getEditableText().toString();
        AppServerClient.getInstance().login(username, password, loginCallback);
    }

    @OnClick(R.id.forgotPasswordTextView)
    public void pushForgotPasswordFragment()
    {
        // TODO: push a forgot password fragment.
    }

    private void handleLoginFailure(GenericResponse<Account> accountWrapper)
    {
        AccountManager.clearStoredAccount();
        // TODO: show the user something about failing to log in here.
    }

    Callback<GenericResponse<Account>> loginCallback = new Callback<GenericResponse<Account>>()
    {
        @Override
        public void success(GenericResponse<Account> accountWrapper, Response response)
        {
            if( accountWrapper != null
                    && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK)
                    && accountWrapper.response != null )
            {
                AccountManager.setStoredAccount(accountWrapper.response);
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                startActivity(intent);
            }
            else
            {
                handleLoginFailure(accountWrapper);
            }
        }

        @Override
        public void failure(RetrofitError error)
        {
            handleLoginFailure(null);
        }
    };
}
