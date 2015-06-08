package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.adapter.ProfileAdapter;
import com.kamcord.app.model.ProfileItem;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedVideoList;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.thread.Uploader;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.view.DynamicRecyclerView;
import com.kamcord.app.view.utils.ProfileLayoutSpanSizeLookup;
import com.kamcord.app.view.utils.ProfileViewItemDecoration;

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
public class ProfileFragment extends Fragment implements Uploader.UploadStatusListener {

    private static final int HEADER_EXISTS = 1;

    @InjectView(R.id.signInPromptContainer)
    ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton)
    Button signInPromptButton;
    @InjectView(R.id.profilefragment_refreshlayout)
    SwipeRefreshLayout videoFeedRefreshLayout;
    @InjectView(R.id.profile_recyclerview)
    DynamicRecyclerView profileRecyclerView;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private List<ProfileItem> mProfileList = new ArrayList<>();
    private ProfileAdapter mProfileAdapter;
    private ProfileItem userHeader;
    private String nextPage;
    private int totalItems = 0;
    private boolean footerVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);
        ButterKnife.inject(this, root);
        initKamcordProfileFragment();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void initKamcordProfileFragment() {

        if(AccountManager.isLoggedIn()) {
            userHeader = new ProfileItem<>(ProfileItem.Type.HEADER, (User) null);
            mProfileList.add(userHeader);
            signInPromptContainer.setVisibility(View.GONE);
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserInfo(myAccount.id, new GetUserInfoCallBack());
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
        } else {
            signInPromptContainer.setVisibility(View.VISIBLE);
        }

        mProfileAdapter = new ProfileAdapter(getActivity(), mProfileList);
        profileRecyclerView.setAdapter(mProfileAdapter);
        profileRecyclerView.setSpanSizeLookup(new ProfileLayoutSpanSizeLookup(profileRecyclerView));
        profileRecyclerView.addItemDecoration(new ProfileViewItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));

        videoFeedRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        videoFeedRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        videoFeedRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (AccountManager.isLoggedIn()) {
                    videoFeedRefreshLayout.setRefreshing(true);
                    Account myAccount = AccountManager.getStoredAccount();
                    AppServerClient.getInstance().getUserInfo(myAccount.id, new GetUserInfoCallBack());
                    AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new SwipeToRefreshVideoFeedCallBack());
                } else {
                    videoFeedRefreshLayout.setRefreshing(false);
                }
            }
        });
        
        profileRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (profileRecyclerView.getChildAt(0) != null) {
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                    videoFeedRefreshLayout.setEnabled(profileRecyclerView.getChildAdapterPosition(profileRecyclerView.getChildAt(0)) == 0
                            && profileRecyclerView.getChildAt(0).getTop() == tabsHeight);
                } else {
                    videoFeedRefreshLayout.setEnabled(true);
                }

                if ((mProfileList.size() - 1) < totalItems
                        && nextPage != null
                        && ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == (mProfileList.size() - 1)
                        && !footerVisible) {
                    loadMoreItems();
                }
            }
        });
    }

    public void loadMoreItems() {
        footerVisible = true;
        mProfileList.add(new ProfileItem<>(ProfileItem.Type.FOOTER, null));
        mProfileAdapter.notifyItemInserted(mProfileAdapter.getItemCount());
        Account myAccount = AccountManager.getStoredAccount();
        AppServerClient.getInstance().getUserVideoFeed(myAccount.id, nextPage, new GetUserVideoFeedCallBack());
    }

    private class GetUserInfoCallBack implements Callback<GenericResponse<User>> {
        @Override
        public void success(GenericResponse<User> userResponse, Response response) {
            if (userResponse != null && userResponse.response != null) {
                userHeader.setUser(userResponse.response);
                totalItems = userHeader.getUser().video_count;
                mProfileAdapter.notifyItemChanged(0);
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            videoFeedRefreshLayout.setRefreshing(false);
        }
    }

    private class SwipeToRefreshVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null) {
                if(mProfileList.size() > 1) {
                    mProfileList.subList(1, mProfileList.size()).clear();
                    nextPage = paginatedVideoListGenericResponse.response.next_page;
                    for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                        ProfileItem profileViewModel = new ProfileItem<>(ProfileItem.Type.VIDEO, video);
                        mProfileList.add(profileViewModel);
                    }
                }
                footerVisible = false;
                mProfileAdapter.notifyDataSetChanged();
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            videoFeedRefreshLayout.setRefreshing(false);
        }
    }

    private class GetUserVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null) {
                nextPage = paginatedVideoListGenericResponse.response.next_page;
                if (mProfileList.get(mProfileAdapter.getItemCount() - 1).getType() == ProfileItem.Type.FOOTER) {
                    mProfileList.remove(mProfileAdapter.getItemCount() - 1);
                }
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                    ProfileItem profileViewModel = new ProfileItem<>(ProfileItem.Type.VIDEO, video);
                    mProfileList.add(profileViewModel);
                }
                footerVisible = false;
                mProfileAdapter.notifyDataSetChanged();
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            videoFeedRefreshLayout.setRefreshing(false);
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onUploadStart(RecordingSession recordingSession) {

    }

    @Override
    public void onUploadProgress(RecordingSession recordingSession, float progress) {

    }

    @Override
    public void onUploadFinish(RecordingSession recordingSession, boolean success) {

    }
}
