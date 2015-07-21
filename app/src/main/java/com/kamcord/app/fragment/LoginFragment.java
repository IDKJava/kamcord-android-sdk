package com.kamcord.app.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.service.RegistrationIntentService;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.Connectivity;
import com.kamcord.app.utils.KeyboardUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func2;

public class LoginFragment extends Fragment {

    @InjectView(R.id.usernameEditText)
    EditText usernameEditText;
    @InjectView(R.id.passwordEditText)
    EditText passwordEditText;
    @InjectView(R.id.loginButton)
    Button loginButton;
    @InjectView(R.id.forgotPasswordTextView)
    TextView forgotPasswordTextView;

    private boolean viewsAreValid = true;
    private Toast toast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.inject(this, root);
        viewsAreValid = true;
        usernameEditText.requestFocus();
        usernameEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showSoftKeyboard(usernameEditText, getActivity());
            }
        }, 50);
        validateLogin();
        return root;
    }

    public void validateLogin() {
        final Observable<OnTextChangeEvent> usernameField = WidgetObservable.text(usernameEditText);
        final Observable<OnTextChangeEvent> passwordField = WidgetObservable.text(passwordEditText);

        final Observable<Boolean> isFieldsValid = Observable.combineLatest(usernameField, passwordField,
                new Func2<OnTextChangeEvent, OnTextChangeEvent, Boolean>() {
                    @Override
                    public Boolean call(OnTextChangeEvent onTextChangeEvent, OnTextChangeEvent onTextChangeEvent2) {
                        return usernameEditText.getText().length() != 0
                                && passwordEditText.getText().length() != 0;
                    }
                });

        isFieldsValid.subscribe(new Observer<Boolean>() {
            @Override
            public void onNext(final Boolean isValid) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginButton.setActivated(isValid);
                    }
                });
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsAreValid = false;
        ButterKnife.reset(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        KamcordAnalytics.startSession(this, Event.Name.PROFILE_LOGIN_VIEW);
    }

    @Override
    public void onStop() {
        super.onStop();
        KamcordAnalytics.endSession(this, Event.Name.PROFILE_LOGIN_VIEW, getArguments());
    }

    private int getContainerViewId() {
        if (getActivity() instanceof LoginActivity) {
            return ((LoginActivity) getActivity()).getContainerViewId();
        }
        return 0;
    }

    @OnFocusChange({R.id.usernameEditText, R.id.passwordEditText})
    void editTextOutsideTouch(View v, boolean focused) {
        KeyboardUtils.setSoftKeyboardVisibility(v, getActivity(), focused);
    }

    @OnClick(R.id.loginButton)
    public void login() {
        if (Connectivity.isConnected()) {
            loginButton.setEnabled(false);
            String username = usernameEditText.getEditableText().toString().trim();
            String password = passwordEditText.getEditableText().toString();
            KamcordAnalytics.startSession(loginCallback, Event.Name.PROFILE_LOGIN);
            AppServerClient.getInstance().login(username, password, loginCallback);
        } else {
            if (toast == null) {
                toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.failedToConnect), Toast.LENGTH_LONG);
            } else {
                toast.setText(getResources().getString(R.string.failedToConnect));
            }
            toast.show();
        }
    }

    @OnClick(R.id.forgotPasswordTextView)
    public void pushResetPasswordFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(getContainerViewId(), new ResetPasswordFragment())
                .addToBackStack(null).commit();
    }

    private void handleLoginFailure(GenericResponse<Account> accountWrapper) {
        AccountManager.clearStoredAccount();
        if (accountWrapper != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.loginFailed)
                    .setMessage(R.string.loginFailureMessage)
                    .setNeutralButton(android.R.string.ok, null)
                    .setPositiveButton(R.string.resetPassword,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    pushResetPasswordFragment();
                                }
                            })
                    .show();

        }
    }

    Callback<GenericResponse<Account>> loginCallback = new Callback<GenericResponse<Account>>() {
        @Override
        public void success(GenericResponse<Account> accountWrapper, Response response) {
            boolean isSuccess = accountWrapper != null && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK);
            String failureReason = accountWrapper != null && accountWrapper.status != null && !accountWrapper.status.equals(StatusCode.OK)
                    ? accountWrapper.status.status_reason : null;
            Bundle extras = analyticsExtras(isSuccess, failureReason);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_LOGIN, extras);

            if (viewsAreValid) {
                if (accountWrapper != null
                        && accountWrapper.status != null && accountWrapper.status.equals(StatusCode.OK)
                        && accountWrapper.response != null) {
                    FlurryAgent.logEvent(getResources().getString(R.string.flurryLogin));
                    Intent notifIntent = new Intent(getActivity().getApplicationContext(), RegistrationIntentService.class);
                    getActivity().getApplicationContext().startService(notifIntent);
                    AccountManager.setStoredAccount(accountWrapper.response);
                    Intent intent = new Intent(getActivity(), RecordActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    handleLoginFailure(accountWrapper);
                }
                loginButton.setEnabled(true);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Bundle extras = analyticsExtras(false, null);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_LOGIN, extras);

            if (viewsAreValid) {
                handleLoginFailure(null);
                loginButton.setEnabled(true);
            }
        }

        private Bundle analyticsExtras(boolean isSuccess, String failureReason) {
            Bundle extras = new Bundle();

            extras.putInt(KamcordAnalytics.IS_SUCCESS_KEY, isSuccess ? 1 : 0);
            extras.putString(KamcordAnalytics.FAILURE_REASON_KEY, failureReason);
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PROFILE_LOGIN_VIEW);
            extras.putInt(KamcordAnalytics.IS_LOGIN_KEY, 1);

            return extras;
        }
    };
}
