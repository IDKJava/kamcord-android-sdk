package com.kamcord.app.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.kamcord.app.R;
import com.kamcord.app.fragment.WelcomeFragment;

public class LoginActivity extends ActionBarActivity {

    private int containerViewId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        containerViewId = View.generateViewId();
        RelativeLayout contentView = new RelativeLayout(this);
        contentView.setId(containerViewId);
        contentView.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
        setContentView(contentView);

        getSupportFragmentManager().beginTransaction()
                .add(containerViewId, new WelcomeFragment())
                .commit();
    }

    public int getContainerViewId() {
        return containerViewId;
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
}
