package com.kamcord.app.kamcord.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kamcord.app.kamcord.R;

public class LoginFragment extends Fragment implements View.OnClickListener{

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button createProfileBtn;

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
        return v;
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }
}
