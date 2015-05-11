package com.kamcord.app.kamcord.activity.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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
import android.widget.Toast;

import com.kamcord.app.kamcord.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button loginProfileBtn;

    private TextView forgetPasswordTextView;
    private String forgetPasswordStr;

    public LoginFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login_simple, container, false);

        forgetPasswordTextView = (TextView) v.findViewById(R.id.forgetpassword_textview);
        forgetPasswordStr = getResources().getString(R.string.kamcordPassword);
        SpannableStringBuilder textViewStyle = new SpannableStringBuilder(forgetPasswordStr);
        URLSpan forgetpasswordSpan = new URLSpan("https://www.kamcord.com/tos/") {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getActivity().getApplicationContext(), "Click terms of service", Toast.LENGTH_SHORT).show();
            }
        };
        highLightText(forgetpasswordSpan, forgetPasswordStr, textViewStyle);
        forgetPasswordTextView.setText(textViewStyle);

        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return v;
    }

    public void highLightText(URLSpan urlSpan, String highLightStr, SpannableStringBuilder textViewStyle) {
        int indexOfMatchStr = forgetPasswordStr.indexOf(highLightStr);
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
