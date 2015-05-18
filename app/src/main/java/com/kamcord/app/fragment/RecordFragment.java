package com.kamcord.app.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.adapter.GameRecordListAdapter;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedGameList;
import com.kamcord.app.utils.FileManagement;
import com.kamcord.app.utils.SlidingTabLayout;
import com.kamcord.app.utils.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecordFragment extends Fragment implements GameRecordListAdapter.OnItemClickListener {
    private static final String TAG = RecordFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private Game mSelectedGame = null;

    private List<Game> mSupportedGameList = new ArrayList<>();

    private FileManagement mFileManagement;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout toolBarContainer;
    private Toolbar mToolbar;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.record_tab, container, false);
        initKamcordRecordFragment(v);
        return v;
    }

    public void initKamcordRecordFragment(View v) {

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        mSupportedGameList.clear();
        AppServerClient.getInstance().getGamesList(false, false, new GetGamesListCallback());

        // Custom Layout Params
        toolBarContainer = (LinearLayout) getActivity().findViewById(R.id.toolBarContainer);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mSlidingTabLayout = (SlidingTabLayout) getActivity().findViewById(R.id.tabs);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSlidingTabLayout.getLayoutParams();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.record_recyclerview);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));

        View header = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.fragment_recyclerview_header, mRecyclerView, false);

        mRecyclerAdapter = new GameRecordListAdapter(getActivity(), mSupportedGameList, header, 150);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mRecyclerAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.recordfragment_refreshlayout);
        mSwipeRefreshLayout.setProgressViewOffset(false, layoutParams.height, 200);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mSupportedGameList.clear();
                mRecyclerAdapter.notifyDataSetChanged();
                AppServerClient.getInstance().getGamesList(false, false, new GetGamesListCallback());
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 128;
            private static final int SHOW_THRESHOLD = 48;
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (mRecyclerView.getChildAt(0) != null) {
                    mSwipeRefreshLayout.setEnabled(mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0)) == 0
                            && mRecyclerView.getChildAt(0).getTop() == getResources().getDimensionPixelSize(R.dimen.grid_margin));
                } else {
                    mSwipeRefreshLayout.setEnabled(true);
                }

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    hideViews();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -SHOW_THRESHOLD && !controlsVisible) {
                    showViews();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if ((controlsVisible && i2 > 0) || (!controlsVisible && i2 < 0)) {
                    scrolledDistance += i2;
                }
            }
        });
    }

    private void hideViews() {
        toolBarContainer.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        toolBarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private boolean isAppInstalled(String packageName) {
        boolean appIsInstalled = false;

        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appIsInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appIsInstalled;
    }

    @Override
    public void onItemClick(View view, int position) {
        mSelectedGame = mSupportedGameList.get(position);
        selectdGameListener listener = (selectdGameListener) getActivity();
        listener.selectedGame(mSelectedGame);
        Toast.makeText(getActivity(),
                "You will record " + mSelectedGame.name,
                Toast.LENGTH_SHORT)
                .show();
    }

    public interface selectdGameListener {
        void selectedGame(com.kamcord.app.server.model.Game selectedGameModel);
    }

    private class GetGamesListCallback implements Callback<GenericResponse<PaginatedGameList>> {
        @Override
        public void success(GenericResponse<PaginatedGameList> gamesListWrapper, Response response) {
            if (gamesListWrapper != null && gamesListWrapper.response != null && gamesListWrapper.response.game_list != null) {
                for (Game game : gamesListWrapper.response.game_list) {
                    if (game.play_store_id != null && isAppInstalled(game.play_store_id)) {
                        mSupportedGameList.add(game);
                    }
                }
                mRecyclerAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "Unable to get list of KCP games.");
            Log.e(TAG, "  " + retrofitError.toString());
            // TODO: show the user something about this.
        }
    }
}
