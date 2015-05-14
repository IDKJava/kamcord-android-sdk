package com.kamcord.app.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.fragment.CreateProfileFragment;
import com.kamcord.app.fragment.LoginFragment;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private LayoutInflater mLayoutInflater;
    private View panel;
    private Button createProfileButton;
    private Button logInButton;
    private Button skipButton;
    private Button shareViewButton;
    private String subTitleStr;
    private String subTitleHighLightStr;
    private TextView subTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initLoginUI();
    }

    public void initLoginUI() {

        mLayoutInflater = LayoutInflater.from(getBaseContext());
        final View loginControlView = mLayoutInflater.inflate(R.layout.activity_login_inflater, null);
        ViewGroup.LayoutParams layoutParamsControl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(loginControlView, layoutParamsControl);
        panel = findViewById(R.id.panel);

        subTitleTextView = (TextView) findViewById(R.id.subtitle_textview);
        subTitleStr = getResources().getString(R.string.kamcordSubtitle);
        subTitleHighLightStr = getResources().getString(R.string.kamcordSubtitleHighLighted);
        SpannableStringBuilder textViewStyle = new SpannableStringBuilder(subTitleStr);
        int indexOfMatchStr = subTitleStr.indexOf(subTitleHighLightStr);
        textViewStyle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.FabPrimaryColor)),
                indexOfMatchStr,
                indexOfMatchStr + subTitleHighLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textViewStyle.setSpan(new StyleSpan(Typeface.BOLD),
                indexOfMatchStr,
                indexOfMatchStr + subTitleHighLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        subTitleTextView.setText(textViewStyle);

        createProfileButton = (Button) findViewById(R.id.create_profile_btn);
        logInButton = (Button) findViewById(R.id.login_btn);
        skipButton = (Button) findViewById(R.id.skip_btn);
        shareViewButton = (Button) findViewById(R.id.shareview_btn);

        createProfileButton.setOnClickListener(this);
        logInButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        shareViewButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_profile_btn: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                        .add(R.id.activity_login_layout, new CreateProfileFragment())
                        .addToBackStack("CreateProfileFragment").commit();
                panel.setVisibility(View.INVISIBLE);
                break;
            }
            case R.id.login_btn: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                        .add(R.id.activity_login_layout, new LoginFragment())
                        .addToBackStack("LoginFragment").commit();
                panel.setVisibility(View.INVISIBLE);
                break;
            }
            case R.id.skip_btn: {
                Intent mainIntent = new Intent(this, com.kamcord.app.activity.RecordActivity.class);
                startActivity(mainIntent);
                break;
            }
//            case R.id.shareview_btn: {
//                getSupportFragmentManager().beginTransaction()
//                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
//                        .add(R.id.activity_login_layout, new RecordShareFragment())
//                        .addToBackStack("LoginFragment").commit();
//                panel.setVisibility(View.INVISIBLE);
//                break;
//            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (panel.getVisibility() == View.INVISIBLE) {
            panel.setVisibility(View.VISIBLE);
        }

    }
}
