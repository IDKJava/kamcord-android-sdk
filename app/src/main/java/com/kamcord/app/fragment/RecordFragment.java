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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kamcord.app.BuildConfig;
import com.kamcord.app.R;
import com.kamcord.app.adapter.GameRecordListAdapter;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedGameList;
import com.kamcord.app.utils.GameListUtils;
import com.kamcord.app.view.DynamicRecyclerView;
import com.kamcord.app.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecordFragment extends Fragment implements GameRecordListAdapter.OnItemClickListener {

    @InjectView(R.id.refreshRecordTab) TextView refreshRecordTab;
    @InjectView(R.id.recordfragment_refreshlayout) SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.record_recyclerview) DynamicRecyclerView mRecyclerView;

    private static final String TAG = RecordFragment.class.getSimpleName();
    private GameRecordListAdapter mRecyclerAdapter;
    private Game mSelectedGame = null;
    private GridLayoutManager gridLayoutManager;

    private List<Game> mSupportedGameList = new ArrayList<>();
    private RecyclerViewScrollListener onRecyclerViewScrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.record_tab, container, false);
        ButterKnife.inject(this, v);
        initKamcordRecordFragment(v);
        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if( activity instanceof RecyclerViewScrollListener)
        {
            onRecyclerViewScrollListener = (RecyclerViewScrollListener) activity;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        onRecyclerViewScrollListener = null;
    }

    public void initKamcordRecordFragment(View v) {

        mSupportedGameList = GameListUtils.getCachedGameList();
        if( mSupportedGameList == null )
        {
            mSupportedGameList = new ArrayList<>();
        }
        sortGameList(mSupportedGameList);

        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));
        mRecyclerAdapter = new GameRecordListAdapter(getActivity(), mSupportedGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        if( mSupportedGameList.size() == 0 ) {
            refreshRecordTab.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    AppServerClient.getInstance().getGamesList(false, false, new GetGamesListCallback());
                }
            });
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                AppServerClient.getInstance().getGamesList(false, false, new GetGamesListCallback());
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
                if (onRecyclerViewScrollListener != null) {
                    onRecyclerViewScrollListener.onRecyclerViewScrollStateChanged(recyclerView, state);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mRecyclerView.getChildAt(0) != null) {
                    int gridMargin = getResources().getDimensionPixelSize(R.dimen.grid_margin);
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                    mSwipeRefreshLayout.setEnabled(mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0)) == 0
                            && mRecyclerView.getChildAt(0).getTop() == gridMargin + tabsHeight);
                } else {
                    mSwipeRefreshLayout.setEnabled(true);
                }

                if (onRecyclerViewScrollListener != null) {
                    onRecyclerViewScrollListener.onRecyclerViewScrolled(recyclerView, dy, dy);
                }
            }
        });
    }

    private boolean isAppInstalled(String packageName) {
        boolean appIsInstalled = false;

        Activity activity = getActivity();
        if (activity != null) {
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
        Game game = mSupportedGameList.get(position);
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
        void selectedGame(com.kamcord.app.server.model.Game selectedG0ameModel);
    }

    public interface RecyclerViewScrollListener {
        void onRecyclerViewScrollStateChanged(RecyclerView recyclerView, int state);
        void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy);
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Game game : mSupportedGameList) {
            if (isAppInstalled(game.play_store_id) && !game.isInstalled) {
                game.isInstalled = true;
                sortGameList(mSupportedGameList);
                gridLayoutManager.scrollToPosition(mSupportedGameList.indexOf(game));
            }
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void sortGameList(List<Game> supportedGameList) {
        Collections.sort(supportedGameList, new Comparator<Game>() {
            @Override
            public int compare(Game g1, Game g2) {
                return (g2.isInstalled ? 1 : 0) - (g1.isInstalled ? 1 : 0);
            }
        });
    }

    private class GetGamesListCallback implements Callback<GenericResponse<PaginatedGameList>> {
        @Override
        public void success(GenericResponse<PaginatedGameList> gamesListWrapper, Response response) {
            if (gamesListWrapper != null && gamesListWrapper.response != null && gamesListWrapper.response.game_list != null) {
                mSupportedGameList.clear();
                if (BuildConfig.DEBUG) {
                    Game ripples = new Game();
                    ripples.name = "Ripple Test";
                    ripples.game_primary_id = "3047";
                    ripples.play_store_id = "com.kamcord.ripples";
                    ripples.icons = new Game.Icons();
                    ripples.icons.regular = "https://www.kamcord.com/images/core/logo-kamcord@2x.png";
                    if (isAppInstalled(ripples.play_store_id)) {
                        ripples.isInstalled = true;
                    }
                    mSupportedGameList.add(ripples);
                }
                for (Game game : gamesListWrapper.response.game_list) {
                    if (game.play_store_id != null) {
                        if (isAppInstalled(game.play_store_id)) {
                            game.isInstalled = true;
                        }
                        mSupportedGameList.add(game);
                    }
                }
                if(refreshRecordTab.getVisibility() == View.VISIBLE) {
                    refreshRecordTab.setVisibility(View.INVISIBLE);
                }
                sortGameList(mSupportedGameList);
                GameListUtils.saveGameList(mSupportedGameList);
                mRecyclerAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "Unable to get list of KCP games.");
            Log.e(TAG, "  " + retrofitError.toString());
            mSwipeRefreshLayout.setRefreshing(false);
            // TODO: show the user something about this.
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(getActivity());
    }
}
