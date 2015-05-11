package com.kamcord.app.kamcord.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kamcord.app.kamcord.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button createProfileBtn;

    private String termsOfServiceStr = "Terms of Service and Privacy Policy";
    private String highlightStrTerms = "Terms of Service";
    private String highlightStrPrivacy = "Privacy Policy";
    private TextView subTitleTextView;
    private int displayFlag = 1;
    private TextView forgetPasswordTextView;
    private TextView termsTextView;

    public LoginFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            displayFlag = bundle.getInt("createprofile", displayFlag);
            Log.d("Bundle value: ", "" + displayFlag);
        }

        View v = inflater.inflate(R.layout.fragment_login_simple, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        emailEditText = (EditText) v.findViewById(R.id.emailEditText);
        emailEditText.setVisibility(View.INVISIBLE);

        termsTextView = (TextView) v.findViewById(R.id.termsTextview);
        termsTextView.setVisibility(View.INVISIBLE);
        forgetPasswordTextView = (TextView) v.findViewById(R.id.forgetpasswordTextView);
        forgetPasswordTextView.setText("Forget Password?");

        return v;
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }
}
