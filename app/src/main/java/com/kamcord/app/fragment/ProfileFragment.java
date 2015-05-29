package com.kamcord.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.adapter.ProfileAdapter;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedVideoList;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by donliang1 on 5/6/15.
 */
public class ProfileFragment extends Fragment implements ProfileAdapter.OnItemClickListener {

    @InjectView(R.id.signInPromptContainer) ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton) Button signInPromptButton;
    @InjectView(R.id.profilefragment_refreshlayout) SwipeRefreshLayout videoFeedRefreshLayout;
    @InjectView(R.id.profile_recyclerview) RecyclerView profileRecyclerView;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private List<Video> mProfileList = new ArrayList<>();
    private ProfileAdapter mProfileAdapter;
    private RecordFragment.RecyclerViewScrollListener onRecyclerViewScrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);
        ButterKnife.inject(this, root);
        initKamcordProfileFragment(root);

        return root;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if( activity instanceof RecordFragment.RecyclerViewScrollListener)
        {
            onRecyclerViewScrollListener = (RecordFragment.RecyclerViewScrollListener) activity;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        onRecyclerViewScrollListener = null;
    }

    public void initKamcordProfileFragment(View view) {

        profileRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.card_margin)));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mProfileAdapter = new ProfileAdapter(getActivity(), mProfileList);
        mProfileAdapter.setOnItemClickListener(this);
        profileRecyclerView.setLayoutManager(layoutManager);
        profileRecyclerView.setAdapter(mProfileAdapter);

        if( AccountManager.isLoggedIn() )
        {
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
        }

        profileRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
                if (onRecyclerViewScrollListener != null) {
                    onRecyclerViewScrollListener.onRecyclerViewScrollStateChanged(recyclerView, state);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (profileRecyclerView.getChildAt(0) != null) {
                    int gridMargin = getResources().getDimensionPixelSize(R.dimen.grid_margin);
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                } else {
                }

                if (onRecyclerViewScrollListener != null) {
                    onRecyclerViewScrollListener.onRecyclerViewScrolled(recyclerView, dy, dy);
                }
            }
        });

        videoFeedRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        videoFeedRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        videoFeedRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                videoFeedRefreshLayout.setRefreshing(true);
                Account myAccount = AccountManager.getStoredAccount();
                AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
            }
        });

    }

    @Override
    public void onItemClick(View view, int position) {
        if(mProfileList.size() != 0) {
            Video videoGetClicked = mProfileList.get(position);
        }
    }

    private class GetUserVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null) {
                mProfileList.clear();
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                    mProfileList.add(video);
                }
                mProfileAdapter.notifyDataSetChanged();
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(AccountManager.isLoggedIn()) {
            signInPromptContainer.setVisibility(View.GONE);
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
        }
        else
        {
            signInPromptContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt()
    {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
