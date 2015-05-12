package com.kamcord.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.utils.StringUtils;

public class CreateProfileFragment extends Fragment implements View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button createProfileBtn;

    private String termsOfServiceStr;
    private String highlightStrTerms;
    private String highlightStrPrivacy;
    private TextView subTitleTextView;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_createprofile_login, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mContext = getActivity().getApplicationContext();
        createProfileBtn = (Button) v.findViewById(R.id.createprofiel_btn);
        createProfileBtn.setOnClickListener(this);

        termsOfServiceStr = getResources().getString(R.string.kamcordTermsPolicy);
        highlightStrTerms = getResources().getString(R.string.kamcordTermsOfService);
        highlightStrPrivacy = getResources().getString(R.string.kamcordPrivacPolicy);

        subTitleTextView = (TextView) v.findViewById(R.id.terms_textview);
        SpannableStringBuilder textViewStyle = new SpannableStringBuilder(termsOfServiceStr);
        URLSpan termsOfServiceSpan = StringUtils.makeURLSpan(getActivity(), "https://www.kamcord.com/tos/");
        StringUtils.highLightText(mContext, termsOfServiceSpan, termsOfServiceStr, highlightStrTerms, textViewStyle);
        URLSpan privacyPolicySpan = StringUtils.makeURLSpan(getActivity(), "https://www.kamcord.com/privacy/");
        StringUtils.highLightText(mContext, privacyPolicySpan, termsOfServiceStr, highlightStrPrivacy, textViewStyle);
        subTitleTextView.setText(textViewStyle);
        subTitleTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }
}
