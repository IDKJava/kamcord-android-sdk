package com.kamcord.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.BuildConfig;
import com.kamcord.app.R;
import com.kamcord.app.adapter.GameRecordListAdapter;
import com.kamcord.app.model.RecordItem;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedGameList;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.utils.GameListUtils;
import com.kamcord.app.view.DynamicRecyclerView;
import com.kamcord.app.view.utils.GridViewItemDecoration;
import com.kamcord.app.view.utils.RecordLayoutSpanSizeLookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecordFragment extends Fragment implements
        GameRecordListAdapter.OnGameActionButtonClickListener {
    private static final String TAG = RecordFragment.class.getSimpleName();
    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    @InjectView(R.id.refreshRecordTab)
    TextView refreshRecordTab;
    @InjectView(R.id.recordfragment_refreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.record_recyclerview)
    DynamicRecyclerView mRecyclerView;

    private GameRecordListAdapter mRecyclerAdapter;
    private Game mSelectedGame = null;

    private List<RecordItem> mRecordItemList = new ArrayList<>();
    private List<Game> mGameList = new ArrayList();
    private RecyclerViewScrollListener onRecyclerViewScrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.record_tab, container, false);
        ButterKnife.inject(this, v);
        initKamcordRecordFragment(v);
        return v;
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

    @Override
    public void onResume() {
        super.onResume();
        handleServiceRunning();
        boolean gameListChanged = false;
        for (RecordItem item : mRecordItemList) {
            Game game = item.getGame();
            if (game != null ) {
                boolean isNowInstalled = isAppInstalled(game.play_store_id);
                if (isNowInstalled && !game.isInstalled) {
                    game.isInstalled = true;
                    gameListChanged = true;
                } else if (!isNowInstalled && game.isInstalled) {
                    game.isInstalled = false;
                    gameListChanged = true;
                }
            }
        }
        if (gameListChanged) {
            updateRecordItemList();
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void initKamcordRecordFragment(View v) {

        mGameList = GameListUtils.getCachedGameList();
        if (mGameList == null) {
            mGameList = new ArrayList<>();
        }
        updateRecordItemList();

        mRecyclerView.addItemDecoration(new GridViewItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));
        mRecyclerAdapter = new GameRecordListAdapter(getActivity(), mRecordItemList, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setSpanSizeLookup(new RecordLayoutSpanSizeLookup(mRecyclerView));

        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        if (mRecordItemList.size() == 0) {
            refreshRecordTab.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    AppServerClient.getInstance().getGamesList(true, false, new GetGamesListCallback());
                }
            });
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                AppServerClient.getInstance().getGamesList(true, false, new GetGamesListCallback());
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

    private void handleServiceRunning() {
        if (RecordingService.isRunning()) {
            RecordingSession recordingSession = RecordingService.getInstance().getRecordingSession();
            if (recordingSession != null) {
                for (Game game : mGameList) {
                    if (game.play_store_id.equals(recordingSession.getGamePackageName())) {
                        game.isRecording = true;
                    } else {
                        game.isRecording = false;
                    }
                }
            } else {
                // TODO: Show the user something about not finding the recording session.
                Log.v(TAG, "Unable to get the recording session!");
            }
        } else {
            for (Game game : mGameList) {
                game.isRecording = false;
            }
        }
        updateRecordItemList();
        mRecyclerAdapter.notifyDataSetChanged();
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

    private Toast startRecordingToast = null;
    public interface RecyclerViewScrollListener {
        void onRecyclerViewScrollStateChanged(RecyclerView recyclerView, int state);

        void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy);
    }

    public void obtainMediaProjection() {
        startActivityForResult(((MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .createScreenCaptureIntent(), MEDIA_PROJECTION_MANAGER_PERMISSION_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getActivity();
        if (activity != null
                && resultCode == Activity.RESULT_OK
                && requestCode == MEDIA_PROJECTION_MANAGER_PERMISSION_CODE) {
            if (mSelectedGame != null) {
                try {
                    Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(mSelectedGame.play_store_id);
                    FlurryAgent.logEvent(getResources().getString(R.string.flurryRecordStarted));
                    startActivity(launchIntent);

                    MediaProjection projection = ((MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                            .getMediaProjection(resultCode, data);
                    RecordingSession recordingSession = new RecordingSession(mSelectedGame);

                    RecordingService.initializeForRecording(projection, recordingSession);
                    activity.startService(new Intent(activity, RecordingService.class));
                } catch (ActivityNotFoundException e) {
                    // TODO: show the user something about not finding the game.
                    Log.w(TAG, "Could not find activity with package " + mSelectedGame.play_store_id);
                }
            } else {
                // TODO: show the user something about selecting a game.
                Log.w("Kamcord", "Unable to start recording because user has not selected a game to record!");
            }
        }
    }


    public void updateRecordItemList() {
        Collections.sort(mGameList, new Comparator<Game>() {
            @Override
            public int compare(Game g1, Game g2) {
                return (g2.isInstalled ? 1 : 0) - (g1.isInstalled ? 1 : 0);
            }
        });
        mRecordItemList.clear();
        boolean lastGameInstalled = false;
        for( Game game : mGameList ) {
            if( game.isInstalled && !lastGameInstalled ) {
                mRecordItemList.add(new RecordItem(RecordItem.Type.INSTALLED_HEADER, null));

            } else if( !game.isInstalled && lastGameInstalled ) {
                mRecordItemList.add(new RecordItem(RecordItem.Type.REQUEST_GAME, null));
                mRecordItemList.add(new RecordItem(RecordItem.Type.NOT_INSTALLED_HEADER, null));
            }
            mRecordItemList.add(new RecordItem(RecordItem.Type.GAME, game));
            lastGameInstalled = game.isInstalled;
        }
    }

    private class GetGamesListCallback implements Callback<GenericResponse<PaginatedGameList>> {
        @Override
        public void success(GenericResponse<PaginatedGameList> gamesListWrapper, Response response) {
            if (gamesListWrapper != null && gamesListWrapper.response != null && gamesListWrapper.response.game_list != null) {
                mGameList.clear();
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
                    mGameList.add(ripples);
                }
                for (Game game : gamesListWrapper.response.game_list) {
                    if (game.play_store_id != null) {
                        if (isAppInstalled(game.play_store_id)) {
                            game.isInstalled = true;
                        }
                        mGameList.add(game);
                    }
                }
                if (refreshRecordTab.getVisibility() == View.VISIBLE) {
                    refreshRecordTab.setVisibility(View.INVISIBLE);
                }
                GameListUtils.saveGameList(mGameList);
                updateRecordItemList();
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
    public void onGameActionButtonClick(final Game game) {

        if( game.isInstalled ) {

            if (!RecordingService.isRunning()) {
                mSelectedGame = game;
                obtainMediaProjection();
            } else {
                final RecordingSession recordingSession = RecordingService.getInstance().getRecordingSession();
                if( recordingSession != null && recordingSession.getGamePackageName().equals(game.play_store_id) ) {
                    if( !recordingSession.hasRecordedFrames() ) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.nothingRecordedYet)
                                .setMessage(String.format(getActivity().getResources().getString(R.string.kamcordHasntRecorded), recordingSession.getGameServerName()))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage(mSelectedGame.play_store_id);
                                            getActivity().startActivity(launchIntent);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Unable to start " + recordingSession.getGameServerName(), e);
                                        }
                                    }
                                })
                                .setNeutralButton(android.R.string.cancel, null)
                                .show();
                    } else {
                        stopRecording();
                        shareRecording(recordingSession);
                    }
                } else {
                    String message = String.format(Locale.ENGLISH, getResources().getString(R.string.youreAlreadyRecording), recordingSession.getGameServerName());
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.alreadyRecording)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    stopRecording();
                                    mSelectedGame = game;
                                    obtainMediaProjection();
                                }
                            })
                            .setNeutralButton(android.R.string.cancel, null)
                            .show();
                }
            }
        } else {

            mSelectedGame = null;
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + game.play_store_id));
            getActivity().startActivity(intent);
        }
    }

    private void stopRecording() {
        FragmentActivity activity = getActivity();
        activity.stopService(new Intent(activity, RecordingService.class));
        for( Game game : mGameList)
        {
            game.isRecording = false;
        }
        updateRecordItemList();
        mRecyclerAdapter.notifyDataSetChanged();
    }

    private void shareRecording(RecordingSession recordingSession)
    {
        if (recordingSession != null && recordingSession.hasRecordedFrames()) {
            FragmentActivity activity = getActivity();
            FlurryAgent.logEvent(getResources().getString(R.string.flurryReplayShareView));
            ShareFragment recordShareFragment = new ShareFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(ShareFragment.ARG_RECORDING_SESSION, recordingSession);
            recordShareFragment.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                    .add(R.id.activity_mdrecord_layout, recordShareFragment)
                    .addToBackStack("ShareFragment").commit();
        } else {
            // TODO: show the user something about being unable to get the recording session.
        }
    }
}
