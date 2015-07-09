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
import com.kamcord.app.adapter.ProfileAdapter;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Card;
import com.kamcord.app.server.model.DiscoverFeed;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.Group;
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

    @InjectView(R.id.homefeedPromptContainer)
    ViewGroup homefeedPromptContainer;
    @InjectView(R.id.homefragment_refreshlayout)
    SwipeRefreshLayout discoverFeedRefreshLayout;
    @InjectView(R.id.home_recyclerview)
    DynamicRecyclerView homeRecyclerView;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private List<FeedItem> mProfileList = new ArrayList<>();
    private ProfileAdapter mProfileAdapter;
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
            AppServerClient.getInstance().getDiscoverFeed(null, new GetDiscoverFeedCallBack());
        } else {
            discoverFeedRefreshLayout.setEnabled(false);
            homefeedPromptContainer.setVisibility(View.VISIBLE);
        }

        mProfileAdapter = new ProfileAdapter(getActivity(), mProfileList);
        homeRecyclerView.setAdapter(mProfileAdapter);
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
                    client.getDiscoverFeed(null, new SwipeToRefreshDiscoverFeedCallBack());
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
        mProfileList.add(new FeedItem<>(FeedItem.Type.FOOTER, null));
        mProfileAdapter.notifyItemInserted(mProfileAdapter.getItemCount());
        AppServerClient.getInstance().getDiscoverFeed(nextPage, new GetDiscoverFeedCallBack());
    }

    private class SwipeToRefreshDiscoverFeedCallBack implements Callback<GenericResponse<DiscoverFeed>> {
        @Override
        public void success(GenericResponse<DiscoverFeed> discoverFeedGenericResponse, Response response) {
            if (discoverFeedGenericResponse != null
                    && discoverFeedGenericResponse.response != null
                    && discoverFeedGenericResponse.response.group_set != null
                    && viewsAreValid) {
                Group streamGroup = null;
                for (Group group : discoverFeedGenericResponse.response.group_set.groups) {
                    if (group.group_type.equals(Group.STREAM_LIST)) {
                        streamGroup = group;
                        break;
                    }
                }
                if (streamGroup != null) {
                    Iterator<FeedItem> iterator = mProfileList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getType() == FeedItem.Type.STREAM) {
                            iterator.remove();
                        }
                    }
                    nextPage = streamGroup.next_page;
                    for (Card card : streamGroup.card_models) {
                        if (card.stream != null) {
                            FeedItem profileViewModel = new FeedItem<>(FeedItem.Type.STREAM, card.stream);
                            mProfileList.add(profileViewModel);
                        }
                    }
                    footerVisible = false;
                    mProfileAdapter.notifyDataSetChanged();
                    discoverFeedRefreshLayout.setRefreshing(false);
                }
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

    private class GetDiscoverFeedCallBack implements Callback<GenericResponse<DiscoverFeed>> {
        @Override
        public void success(GenericResponse<DiscoverFeed> discoverFeedGenericResponse, Response response) {
            if (discoverFeedGenericResponse != null
                    && discoverFeedGenericResponse.response != null
                    && discoverFeedGenericResponse.response.group_set != null
                    && viewsAreValid) {
                Group streamGroup = null;
                for (Group group : discoverFeedGenericResponse.response.group_set.groups) {
                    if (group.group_type.equals(Group.STREAM_LIST)) {
                        streamGroup = group;
                        break;
                    }
                }
                if (streamGroup != null) {
                    nextPage = streamGroup.next_page;
                    if (mProfileList.size() > 0 && (mProfileList.get(mProfileAdapter.getItemCount() - 1).getType() == FeedItem.Type.FOOTER)) {
                        mProfileList.remove(mProfileAdapter.getItemCount() - 1);
                    }
                    for (Card card : streamGroup.card_models) {
                        if (card.stream != null) {
                            FeedItem profileViewModel = new FeedItem<>(FeedItem.Type.STREAM, card.stream);
                            mProfileList.add(profileViewModel);
                        }
                    }
                    footerVisible = false;
                    mProfileAdapter.notifyDataSetChanged();
                    discoverFeedRefreshLayout.setRefreshing(false);
                    homefeedPromptContainer.setVisibility(View.GONE);
                }
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
}
