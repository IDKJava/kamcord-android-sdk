package com.kamcord.app.kamcord.activity.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

    private String termsOfServiceStr = "Terms of Service and Privacy Policy";
    private String highlightStrTerms = "Terms of Service";
    private String highlightStrPrivacy = "Privacy Policy";
    private TextView subTitleTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_login, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        createProfileBtn = (Button) v.findViewById(R.id.createprofiel_btn);
        createProfileBtn.setOnClickListener(this);

        subTitleTextView = (TextView) v.findViewById(R.id.termsTextview);
        SpannableStringBuilder textViewStyle = new SpannableStringBuilder(termsOfServiceStr);
        highLightText(highlightStrTerms, textViewStyle);
        highLightText(highlightStrPrivacy, textViewStyle);
        subTitleTextView.setText(textViewStyle);

        return v;
    }

    public void highLightText(String highLightStr, SpannableStringBuilder textViewStyle) {
        int indexOfMatchStr = termsOfServiceStr.indexOf(highLightStr);
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
