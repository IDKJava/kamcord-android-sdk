package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.adapter.StreamListAdapter;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Card;
import com.kamcord.app.server.model.CardList;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.utils.Connectivity;
import com.kamcord.app.view.DynamicRecyclerView;
import com.kamcord.app.view.utils.ProfileLayoutSpanSizeLookup;
import com.kamcord.app.view.utils.ProfileViewItemDecoration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends Fragment {

    @InjectView(R.id.homeFeedPromptContainer)
    ViewGroup homeFeedPromptContainer;
    @InjectView(R.id.homefragment_refreshlayout)
    SwipeRefreshLayout discoverFeedRefreshLayout;
    @InjectView(R.id.home_recyclerview)
    DynamicRecyclerView homeRecyclerView;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private List<FeedItem> mFeedItemList = new ArrayList<>();
    private StreamListAdapter mStreamAdapter;
    private String nextPage;
    private int totalItems = 0;
    private boolean footerVisible = false;
    private boolean viewsAreValid = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_tab, container, false);

        ButterKnife.inject(this, root);
        viewsAreValid = true;
        initKamcordHomeFragment();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsAreValid = false;
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

    public void initKamcordHomeFragment() {

        if (Connectivity.isConnected()) {
            discoverFeedRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    discoverFeedRefreshLayout.setRefreshing(true);
                }
            });
            AppServerClient.getInstance().getHomeFeed(null, new GetHomeFeedCallBack());
        } else {
            discoverFeedRefreshLayout.setEnabled(false);
            homeFeedPromptContainer.setVisibility(View.VISIBLE);
        }

        mStreamAdapter = new StreamListAdapter(getActivity(), mFeedItemList);
        homeRecyclerView.setAdapter(mStreamAdapter);
        homeRecyclerView.setSpanSizeLookup(new ProfileLayoutSpanSizeLookup(homeRecyclerView));
        homeRecyclerView.addItemDecoration(new ProfileViewItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));

        discoverFeedRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        discoverFeedRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        discoverFeedRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Connectivity.isConnected()) {
                    discoverFeedRefreshLayout.setRefreshing(true);
                    AppServerClient.AppServer client = AppServerClient.getInstance();
                    client.getHomeFeed(null, new SwipeToRefreshHomeFeedCallBack());
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.failedToConnect), Toast.LENGTH_SHORT).show();
                    discoverFeedRefreshLayout.setRefreshing(false);
                }
            }
        });

        homeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (homeRecyclerView.getChildAt(0) != null) {
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                    int cardMargin = getResources().getDimensionPixelSize(R.dimen.cardview_lightermargin);
                    discoverFeedRefreshLayout.setEnabled(homeRecyclerView.getChildAdapterPosition(homeRecyclerView.getChildAt(0)) == 0
                            && homeRecyclerView.getChildAt(0).getTop() == tabsHeight + cardMargin);
                } else {
                    discoverFeedRefreshLayout.setEnabled(true);
                }

                if ((mFeedItemList.size() - 1) < totalItems
                        && nextPage != null
                        && ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == (mFeedItemList.size() - 1)
                        && !footerVisible) {
                    loadMoreItems();
                }
            }
        });
    }

    public void loadMoreItems() {
        footerVisible = true;
        mFeedItemList.add(new FeedItem<>(FeedItem.Type.FOOTER, null));
        mStreamAdapter.notifyItemInserted(mStreamAdapter.getItemCount());
        AppServerClient.getInstance().getHomeFeed(nextPage, new GetHomeFeedCallBack());
    }

    private class SwipeToRefreshHomeFeedCallBack implements Callback<GenericResponse<CardList>> {
        @Override
        public void success(GenericResponse<CardList> homeFeedGenericResponse, Response response) {
            if (homeFeedGenericResponse != null
                    && homeFeedGenericResponse.response != null
                    && viewsAreValid) {
                Iterator<FeedItem> iterator = mFeedItemList.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                for (Card card : homeFeedGenericResponse.response.cardList) {
                    if (card.stream != null) {
                        FeedItem streamFeedItem = new FeedItem<>(FeedItem.Type.STREAM, card.stream);
                        mFeedItemList.add(streamFeedItem);
                    } else if (card.video != null) {
                        FeedItem videoFeedItem = new FeedItem<>(FeedItem.Type.VIDEO, card.video);
                        mFeedItemList.add(videoFeedItem);
                    }
                }
                footerVisible = false;
                mStreamAdapter.notifyDataSetChanged();
                discoverFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            if (viewsAreValid) {
                discoverFeedRefreshLayout.setRefreshing(false);
            }
        }
    }

    private class GetHomeFeedCallBack implements Callback<GenericResponse<CardList>> {
        @Override
        public void success(GenericResponse<CardList> homeFeedGenericResponse, Response response) {
            if (homeFeedGenericResponse != null
                    && homeFeedGenericResponse.response != null
                    && viewsAreValid) {
                nextPage = homeFeedGenericResponse.response.next_page;
                if (mFeedItemList.size() > 0 && (mFeedItemList.get(mStreamAdapter.getItemCount() - 1).getType() == FeedItem.Type.FOOTER)) {
                    mFeedItemList.remove(mStreamAdapter.getItemCount() - 1);
                }
                for (Card card : homeFeedGenericResponse.response.cardList) {
                    if (card.stream != null) {
                        FeedItem profileViewModel = new FeedItem<>(FeedItem.Type.STREAM, card.stream);
                        mFeedItemList.add(profileViewModel);
                    }
                }
                footerVisible = false;
                mStreamAdapter.notifyDataSetChanged();
                discoverFeedRefreshLayout.setRefreshing(false);
                homeFeedPromptContainer.setVisibility(View.GONE);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            if (viewsAreValid) {
                discoverFeedRefreshLayout.setRefreshing(false);
            }
        }
    }

    public DynamicRecyclerView getHomeRecyclerView() {
        return this.homeRecyclerView;
    }
}
