package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.UserErrorCode;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.StringUtils;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateProfileFragment extends Fragment {

    @InjectView(R.id.usernameEditText) EditText usernameEditText;
    @InjectView(R.id.passwordEditText) EditText passwordEditText;
    @InjectView(R.id.emailEditText) EditText emailEditText;
    @InjectView(R.id.createProfileButton) Button createProfileButton;
    @InjectView(R.id.termsAndPolicyTextView) TextView termsAndPolicyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_profile, container, false);

        ButterKnife.inject(this, root);
        initializeEditTexts();
        initializeTermsAndPolicyString();

        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void initializeEditTexts()
    {
        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && isAdded()) {
                    String username = usernameEditText.getEditableText().toString();
                    if (username.isEmpty()) {
                        usernameEditText.setError(getResources().getString(R.string.youMustEnterUsername));
                    } else {
                        usernameEditText.setError(null);
                        AppServerClient.getInstance().validateUsername(username, validateUsernameCallback);
                    }
                }
            }
        });
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if( !hasFocus && isAdded()) {
                    String email = emailEditText.getEditableText().toString();
                    if (email.isEmpty()) {
                        emailEditText.setError(getResources().getString(R.string.youMustEnterEmail));
                    } else {
                        emailEditText.setError(null);
                        AppServerClient.getInstance().validateEmail(email, validateEmailCallback);
                    }
                }
            }
        });
    }

    private void initializeTermsAndPolicyString()
    {
        String termsAndPolicyText = termsAndPolicyTextView.getText().toString();
        String termsText = getResources().getString(R.string.termsOfService);
        String policyText = getResources().getString(R.string.privacyPolicy);

        SpannableStringBuilder linkedSpan = StringUtils.linkify(getActivity(),
                termsAndPolicyText,
                new String[]{termsText, policyText},
                new String[]{"https://www.kamcord.com/tos/", "https://www.kamcord.com/privacy/"});

        termsAndPolicyTextView.setText(linkedSpan);
    }

    @OnClick(R.id.createProfileButton)
    public void createProfile()
    {
        String username = usernameEditText.getEditableText().toString();
        String email = emailEditText.getEditableText().toString();
        String password = passwordEditText.getEditableText().toString();
        AppServerClient.getInstance().createProfile(username, email, password, createProfileCallback);
    }

    private void handleLoginFailure(GenericResponse<Account> accountWrapper)
    {
        AccountManager.clearStoredAccount();
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.createProfileErrorMessage)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    private final HashMap<UserErrorCode, Integer> errorCodeStringMap = new HashMap<UserErrorCode, Integer>()
        {{
            put(UserErrorCode.INVALID_CHARACTERS, R.string.invalidCharacters);
            put(UserErrorCode.USERNAME_MISSING, R.string.youMustEnterUsername);
            put(UserErrorCode.USERNAME_TAKEN, R.string.usernameTaken);
            put(UserErrorCode.USERNAME_SHORT, R.string.usernameTooShort);
            put(UserErrorCode.USERNAME_LONG, R.string.usernameTooLong);
            put(UserErrorCode.EMAIL_INVALID, R.string.invalidEmail);
            put(UserErrorCode.EMAIL_LONG, R.string.emailTooLong);
            put(UserErrorCode.EMAIL_TAKEN, R.string.emailTaken);
            put(UserErrorCode.EMAIL_MISSING, R.string.youMustEnterEmail);
        }};
    private void handleInvalidUsername(GenericResponse<UserErrorCode> responseWrapper)
    {
        if( responseWrapper != null && responseWrapper.response != null && isAdded())
        {
            int errorStringId = R.string.invalidUsername;
            if( errorCodeStringMap.containsKey(responseWrapper.response) )
            {
                errorStringId = errorCodeStringMap.get(responseWrapper.response);
            }
            String errorString = getResources().getString(errorStringId);
            usernameEditText.setError(errorString);
        }
    }

    private void handleInvalidEmail(GenericResponse<UserErrorCode> responseWrapper)
    {
        if( responseWrapper != null && responseWrapper.response != null && isAdded())
        {
            int errorStringId = R.string.invalidEmail;
            if( errorCodeStringMap.containsKey(responseWrapper.response) )
            {
                errorStringId = errorCodeStringMap.get(responseWrapper.response);
            }
            emailEditText.setError(getResources().getString(errorStringId));
        }
    }

    private Callback<GenericResponse<Account>> createProfileCallback = new Callback<GenericResponse<Account>>()
    {
        @Override
        public void success(GenericResponse<Account> accountWrapper, Response response) {
            if( accountWrapper != null
                    && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK)
                    && accountWrapper.response != null
                    && isAdded())
            {
                FlurryAgent.logEvent(getResources().getString(R.string.flurryCreateProfile));
                AccountManager.setStoredAccount(accountWrapper.response);
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
            else
            {
                handleLoginFailure(accountWrapper);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            handleLoginFailure(null);
        }
    };

    Callback<GenericResponse<UserErrorCode>> validateUsernameCallback = new Callback<GenericResponse<UserErrorCode>>() {
        @Override
        public void success(GenericResponse<UserErrorCode> responseWrapper, Response response) {
            if( responseWrapper != null && responseWrapper.status != null && responseWrapper.response != null
                    && responseWrapper.status.equals(StatusCode.OK) && responseWrapper.response.equals(UserErrorCode.OK)
                    && isAdded())
            {
                usernameEditText.setError(null);
            }
            else
            {
                handleInvalidUsername(responseWrapper);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            usernameEditText.setError(null);
        }
    };

    Callback<GenericResponse<UserErrorCode>> validateEmailCallback = new Callback<GenericResponse<UserErrorCode>>() {
        @Override
        public void success(GenericResponse<UserErrorCode> responseWrapper, Response response) {
            if( responseWrapper != null && responseWrapper.status != null && responseWrapper.response != null
                    && responseWrapper.status.equals(StatusCode.OK) && responseWrapper.response.equals(UserErrorCode.OK)
                    && isAdded())
            {
                emailEditText.setError(null);
            }
            else
            {
                handleInvalidEmail(responseWrapper);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            emailEditText.setError(null);
        }
    };
}
