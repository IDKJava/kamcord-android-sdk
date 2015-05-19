package com.kamcord.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import java.util.Collections;
import java.util.Comparator;
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

        mRecyclerAdapter = new GameRecordListAdapter(getActivity(), mSupportedGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mRecyclerAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.recordfragment_refreshlayout);
        mSwipeRefreshLayout.setProgressViewOffset(false, layoutParams.height, getResources().getDimensionPixelOffset(R.dimen.refreshEnd));
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
            private static final int HIDE_THRESHOLD = 20;
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

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible && mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0)) > 0) {

                    hideViews();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
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

        Activity activity = getActivity();
        if( activity != null ) {
            PackageManager pm = activity.getPackageManager();
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                appIsInstalled = true;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return appIsInstalled;
    }

    @Override
    public void onItemClick(View view, int position) {
        Game game = mSupportedGameList.get(position - 1);
        if (game.isInstalled) {
            mSelectedGame = game;
            SelectedGameListener listener = (SelectedGameListener) getActivity();
            listener.selectedGame(mSelectedGame);
            Toast.makeText(getActivity(),
                    "You will record " + mSelectedGame.name,
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            mSelectedGame = null;
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + game.play_store_id));
            getActivity().startActivity(intent);
        }
    }

    public interface SelectedGameListener {
        void selectedGame(com.kamcord.app.server.model.Game selectedGameModel);
    }

    private class GetGamesListCallback implements Callback<GenericResponse<PaginatedGameList>> {
        @Override
        public void success(GenericResponse<PaginatedGameList> gamesListWrapper, Response response) {
            if (gamesListWrapper != null && gamesListWrapper.response != null && gamesListWrapper.response.game_list != null) {
                for (Game game : gamesListWrapper.response.game_list) {
                    if (game.play_store_id != null) {
                        if (isAppInstalled(game.play_store_id)) {
                            game.isInstalled = true;
                        }
                        mSupportedGameList.add(game);
                    }
                }
                Collections.sort(mSupportedGameList, new Comparator<Game>() {
                    @Override
                    public int compare(Game g1, Game g2) {
                        return (g2.isInstalled ? 1 : 0) - (g1.isInstalled ? 1 : 0);
                    }
                });
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
