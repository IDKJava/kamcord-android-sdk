package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.UserErrorCode;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.KeyboardUtils;
import com.kamcord.app.utils.StringUtils;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateProfileFragment extends Fragment {

    @InjectView(R.id.usernameEditText)
    EditText usernameEditText;
    @InjectView(R.id.passwordEditText)
    EditText passwordEditText;
    @InjectView(R.id.emailEditText)
    EditText emailEditText;
    @InjectView(R.id.createProfileButton)
    Button createProfileButton;
    @InjectView(R.id.termsAndPolicyTextView)
    TextView termsAndPolicyTextView;

    private boolean viewsAreValid = false;

    private static final HashMap<UserErrorCode, Integer> ERROR_CODE_STRING_MAP = new HashMap<UserErrorCode, Integer>() {{
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_profile, container, false);

        ButterKnife.inject(this, root);
        viewsAreValid = true;
        initializeTermsAndPolicyString();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsAreValid = false;
        ButterKnife.reset(this);
    }

    @OnFocusChange({R.id.usernameEditText, R.id.emailEditText, R.id.passwordEditText})
    public void editTextFocusChange(View v, boolean hasFocus) {
        if (isResumed()) {
            KeyboardUtils.setSoftKeyboardVisibility(v, getActivity(), hasFocus);
        }
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.usernameEditText:
                    validateUsername();
                    break;

                case R.id.emailEditText:
                    validateEmail();
                    break;

                default:
                    break;
            }
        }
    }

    private void validateUsername() {
        if (isResumed()) {
            String username = usernameEditText.getEditableText().toString();
            if (username.isEmpty()) {
                usernameEditText.setError(getResources().getString(R.string.youMustEnterUsername));
            } else {
                usernameEditText.setError(null);
                AppServerClient.getInstance().validateUsername(username, validateUsernameCallback);
            }
        }
    }

    private void validateEmail() {
        if (isResumed()) {
            String email = emailEditText.getEditableText().toString();
            KeyboardUtils.hideSoftKeyboard(emailEditText, getActivity().getApplicationContext());
            if (email.isEmpty()) {
                emailEditText.setError(getResources().getString(R.string.youMustEnterEmail));
            } else {
                emailEditText.setError(null);
                AppServerClient.getInstance().validateEmail(email, validateEmailCallback);
            }
        }
    }

    private void initializeTermsAndPolicyString() {
        String termsAndPolicyText = termsAndPolicyTextView.getText().toString();
        String termsText = getResources().getString(R.string.termsOfService);
        String policyText = getResources().getString(R.string.privacyPolicy);

        SpannableStringBuilder linkedSpan = StringUtils.linkify(termsAndPolicyText,
                new String[]{termsText, policyText},
                new String[]{"https://www.kamcord.com/tos/", "https://www.kamcord.com/privacy/"});

        termsAndPolicyTextView.setText(linkedSpan);
        termsAndPolicyTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.createProfileButton)
    public void createProfile() {
        this.createProfileButton.setEnabled(false);
        String username = usernameEditText.getEditableText().toString();
        String email = emailEditText.getEditableText().toString();
        String password = passwordEditText.getEditableText().toString();
        KamcordAnalytics.startSession(createProfileCallback, Event.Name.PROFILE_CREATION);
        AppServerClient.getInstance().createProfile(username, email, password, createProfileCallback);
    }

    private void handleLoginFailure(GenericResponse<Account> accountWrapper) {
        AccountManager.clearStoredAccount();
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.createProfileErrorMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void handleInvalidUsername(GenericResponse<UserErrorCode> responseWrapper) {
        if (responseWrapper != null && responseWrapper.response != null && isResumed()) {
            int errorStringId = R.string.invalidUsername;
            if (ERROR_CODE_STRING_MAP.containsKey(responseWrapper.response)) {
                errorStringId = ERROR_CODE_STRING_MAP.get(responseWrapper.response);
            }
            String errorString = getResources().getString(errorStringId);
            usernameEditText.setError(errorString);
        }
    }

    private void handleInvalidEmail(GenericResponse<UserErrorCode> responseWrapper) {
        if (responseWrapper != null && responseWrapper.response != null && isResumed()) {
            int errorStringId = R.string.invalidEmail;
            if (ERROR_CODE_STRING_MAP.containsKey(responseWrapper.response)) {
                errorStringId = ERROR_CODE_STRING_MAP.get(responseWrapper.response);
            }
            emailEditText.setError(getResources().getString(errorStringId));
        }
    }

    private Callback<GenericResponse<Account>> createProfileCallback = new Callback<GenericResponse<Account>>() {
        @Override
        public void success(GenericResponse<Account> accountWrapper, Response response) {
            boolean isSuccess = accountWrapper != null && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK);
            String failureReason = accountWrapper != null && accountWrapper.status != null && !accountWrapper.status.equals(StatusCode.OK)
                    ? accountWrapper.status.status_reason : null;
            Bundle extras = analyticsExtras(isSuccess, failureReason);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_CREATION, extras);

            if (viewsAreValid) {
                if (accountWrapper != null
                        && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK)
                        && accountWrapper.response != null
                        && isResumed()) {
                    FlurryAgent.logEvent(getResources().getString(R.string.flurryCreateProfile));
                    AccountManager.setStoredAccount(accountWrapper.response);
                    Intent intent = new Intent(getActivity(), RecordActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    handleLoginFailure(accountWrapper);
                }
                createProfileButton.setEnabled(true);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Bundle extras = analyticsExtras(false, null);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_CREATION, extras);

            if (viewsAreValid) {
                handleLoginFailure(null);
                createProfileButton.setEnabled(true);
            }
        }

        private Bundle analyticsExtras(boolean isSuccess, String failureReason) {
            Bundle extras = new Bundle();

            extras.putInt(KamcordAnalytics.IS_SUCCESS_KEY, isSuccess ? 1 : 0);
            extras.putString(KamcordAnalytics.FAILURE_REASON_KEY, failureReason);
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PROFILE_CREATION_VIEW);

            return extras;
        }
    };

    Callback<GenericResponse<UserErrorCode>> validateUsernameCallback = new Callback<GenericResponse<UserErrorCode>>() {
        @Override
        public void success(GenericResponse<UserErrorCode> responseWrapper, Response response) {
            if (viewsAreValid) {
                if (responseWrapper != null && responseWrapper.status != null && responseWrapper.response != null
                        && responseWrapper.status.equals(StatusCode.OK) && responseWrapper.response.equals(UserErrorCode.OK)
                        && isResumed()) {
                    usernameEditText.setError(null);
                } else {
                    handleInvalidUsername(responseWrapper);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (viewsAreValid) {
                usernameEditText.setError(null);
            }
        }
    };

    Callback<GenericResponse<UserErrorCode>> validateEmailCallback = new Callback<GenericResponse<UserErrorCode>>() {
        @Override
        public void success(GenericResponse<UserErrorCode> responseWrapper, Response response) {
            if (viewsAreValid) {
                if (responseWrapper != null && responseWrapper.status != null && responseWrapper.response != null
                        && responseWrapper.status.equals(StatusCode.OK) && responseWrapper.response.equals(UserErrorCode.OK)
                        && isResumed()) {
                    emailEditText.setError(null);
                } else {
                    handleInvalidEmail(responseWrapper);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (viewsAreValid) {
                emailEditText.setError(null);
            }
        }
    };
}
