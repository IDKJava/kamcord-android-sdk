package com.kamcord.app.kamcord.activity.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kamcord.app.kamcord.R;

public class CreateProfileFragment extends Fragment implements View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button createProfileBtn;

    private String termsOfServiceStr;
    private String highlightStrTerms;
    private String highlightStrPrivacy;
    private TextView subTitleTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_createprofile_login, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        createProfileBtn = (Button) v.findViewById(R.id.createprofiel_btn);
        createProfileBtn.setOnClickListener(this);

        termsOfServiceStr = getResources().getString(R.string.kamcordTermsPolicy);
        highlightStrTerms = getResources().getString(R.string.kamcordTermsOfService);
        highlightStrPrivacy = getResources().getString(R.string.kamcordPrivacPolicy);

        subTitleTextView = (TextView) v.findViewById(R.id.terms_textview);
        SpannableStringBuilder textViewStyle = new SpannableStringBuilder(termsOfServiceStr);
        URLSpan termsOfServiceSpan = new URLSpan("https://www.kamcord.com/tos/") {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse("https://www.kamcord.com/tos/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        };
        highLightText(termsOfServiceSpan, highlightStrTerms, textViewStyle);
        URLSpan privacyPolicySpan = new URLSpan("https://www.kamcord.com/privacy/") {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse("https://www.kamcord.com/privacy/");
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
            }
        };
        highLightText(privacyPolicySpan, highlightStrPrivacy, textViewStyle);
        subTitleTextView.setText(textViewStyle);
        subTitleTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    public void highLightText(URLSpan urlSpan, String highLightStr, SpannableStringBuilder textViewStyle) {
        int indexOfMatchStr = termsOfServiceStr.indexOf(highLightStr);
        textViewStyle.setSpan(urlSpan,
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textViewStyle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.TermsHighLighted)),
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textViewStyle.setSpan(new StyleSpan(Typeface.BOLD),
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }
}
