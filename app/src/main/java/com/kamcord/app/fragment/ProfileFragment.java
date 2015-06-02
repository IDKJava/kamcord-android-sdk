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
import com.kamcord.app.activity.ProfileVideoViewActivity;
import com.kamcord.app.adapter.ProfileAdapter;
import com.kamcord.app.model.ProfileItemType;
import com.kamcord.app.model.ProfileViewModel;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedVideoList;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.RecyclerViewScrollListener;
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

    private static final int HEADER_EXISTS = 1;

    @InjectView(R.id.signInPromptContainer)
    ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton)
    Button signInPromptButton;
    @InjectView(R.id.profilefragment_refreshlayout)
    SwipeRefreshLayout videoFeedRefreshLayout;
    @InjectView(R.id.profile_recyclerview)
    RecyclerView profileRecyclerView;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private List<ProfileViewModel> mProfileList = new ArrayList<>();
    private ProfileAdapter mProfileAdapter;
    private RecyclerViewScrollListener onRecyclerViewScrollListener;
    private LinearLayoutManager layoutManager = null;
    private ProfileViewModel userHeader;
    private String nextPage;
    private int totalItems = 0;
    private boolean footerVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);
        ButterKnife.inject(this, root);
        initKamcordProfileFragment(root);

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RecyclerViewScrollListener) {
            onRecyclerViewScrollListener = (RecyclerViewScrollListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onRecyclerViewScrollListener = null;
    }

    public void initKamcordProfileFragment(View view) {

        profileRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.card_margin)));
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userHeader = new ProfileViewModel(ProfileItemType.HEADER, null);
        mProfileList.add(userHeader);
        mProfileAdapter = new ProfileAdapter(getActivity(), mProfileList, this);
        mProfileAdapter.setOnItemClickListener(this);
        profileRecyclerView.setLayoutManager(layoutManager);
        profileRecyclerView.setAdapter(mProfileAdapter);

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
                    int cardMargin = getResources().getDimensionPixelSize(R.dimen.card_margin);
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                    videoFeedRefreshLayout.setEnabled(profileRecyclerView.getChildAdapterPosition(profileRecyclerView.getChildAt(0)) == 0
                            && profileRecyclerView.getChildAt(0).getTop() == cardMargin + tabsHeight);
                } else {
                    videoFeedRefreshLayout.setEnabled(true);
                }

                if (onRecyclerViewScrollListener != null) {
                    onRecyclerViewScrollListener.onRecyclerViewScrolled(recyclerView, dy, dy);
                }

                if ((mProfileList.size() - 1) < totalItems
                        && nextPage != null
                        && layoutManager.findLastCompletelyVisibleItemPosition() == (mProfileList.size() - 1)
                        && footerVisible == false) {
                    loadMoreItems();
                }
            }
        });

        videoFeedRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        videoFeedRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        videoFeedRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (AccountManager.isLoggedIn()) {
                    videoFeedRefreshLayout.setRefreshing(true);
                    Account myAccount = AccountManager.getStoredAccount();
                    AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new SwipeToRefreshVideoFeedCallBack());
                } else {
                    videoFeedRefreshLayout.setRefreshing(false);
                }
            }
        });

    }

    public void loadMoreItems() {
        footerVisible = true;
        mProfileList.add(new ProfileViewModel(ProfileItemType.FOOTER, null));
        mProfileAdapter.notifyItemInserted(mProfileAdapter.getItemCount());
        Account myAccount = AccountManager.getStoredAccount();
        AppServerClient.getInstance().getUserVideoFeed(myAccount.id, nextPage, new GetUserVideoFeedCallBack());
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mProfileList.size() != 0) {
            ProfileViewModel videoGetClicked = mProfileList.get(position);
            Intent intent = new Intent(getActivity(), ProfileVideoViewActivity.class);
            intent.putExtra(ProfileVideoViewActivity.ARG_VIDEO_PATH, videoGetClicked.getVideo().video_url);
            startActivity(intent);
        }
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
                mProfileList.clear();
                mProfileList.add(new ProfileViewModel(ProfileItemType.HEADER, null));
                nextPage = paginatedVideoListGenericResponse.response.next_page;
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                    ProfileViewModel profileViewModel = new ProfileViewModel(ProfileItemType.VIDEO, video);
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

    private class GetUserVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null) {
                nextPage = paginatedVideoListGenericResponse.response.next_page;
                if (mProfileList.get(mProfileAdapter.getItemCount() - 1).getType() == ProfileItemType.FOOTER) {
                    mProfileList.remove(mProfileAdapter.getItemCount() - 1);
                }
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                    ProfileViewModel profileViewModel = new ProfileViewModel(ProfileItemType.VIDEO, video);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AccountManager.isLoggedIn()) {
            signInPromptContainer.setVisibility(View.GONE);
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserInfo(myAccount.id, new GetUserInfoCallBack());
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new SwipeToRefreshVideoFeedCallBack());
        } else {
            signInPromptContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
