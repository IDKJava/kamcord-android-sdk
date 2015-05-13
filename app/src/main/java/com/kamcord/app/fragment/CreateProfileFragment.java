package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateProfileFragment extends Fragment {

    @InjectView(R.id.usernameEditText) EditText userNameEditText;
    @InjectView(R.id.passwordEditText) EditText passwordEditText;
    @InjectView(R.id.emailEditText) EditText emailEditText;
    @InjectView(R.id.createProfileButton) Button createProfileButton;
    @InjectView(R.id.termsAndPolicyTextView) TextView termsAndPolicyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_profile, container, false);

        ButterKnife.inject(this, root);
        initializeTermsAndPolicyString();

        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        String username = userNameEditText.getEditableText().toString();
        String email = emailEditText.getEditableText().toString();
        String password = passwordEditText.getEditableText().toString();
        AppServerClient.getInstance().createProfile(username, email, password, createProfileCallback);
    }

    private Callback<GenericResponse<Account>> createProfileCallback = new Callback<GenericResponse<Account>>()
    {
        @Override
        public void success(GenericResponse<Account> accountGenericResponse, Response response) {

        }

        @Override
        public void failure(RetrofitError error) {

        }
    };
}
