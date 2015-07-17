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

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.service.RegistrationIntentService;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.KeyboardUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsAreValid = false;
        ButterKnife.reset(this);
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
        String username = usernameEditText.getEditableText().toString().trim();
        String password = passwordEditText.getEditableText().toString();
        AppServerClient.getInstance().login(username, password, loginCallback);
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
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (viewsAreValid) {
                handleLoginFailure(null);
            }
        }
    };
}
