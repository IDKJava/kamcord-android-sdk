package com.kamcord.app.kamcord.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.server.client.AppServerClient;
import com.kamcord.app.kamcord.activity.server.model.Game;
import com.kamcord.app.kamcord.activity.server.model.GenericResponse;
import com.kamcord.app.kamcord.activity.server.model.PaginatedGameList;
import com.kamcord.app.kamcord.activity.utils.FileManagement;

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
    private String nextSupportedGamePage = null;

    private Context mContext;

    private FileManagement mFileManagement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.record_tab, container, false);
        mContext = getActivity().getApplicationContext();
        initKamcordRecordFragment(v);
        return v;
    }

    public void initKamcordRecordFragment(View v) {

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        // gridview init;
        // int paddingTop = Utils.getToolbarHeight(mContext) + Utils.getTabsHeight(mContext);
        // mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), paddingTop, mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());

        AppServerClient.getInstance().getGamesList(false, false, new GetGamesListCallback());

//        mRecyclerView = (RecyclerView) v.findViewById(R.id.record_recyclerview);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
//        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
//        mRecyclerAdapter = new GameRecordListAdapter(mContext, mSupportedGameList);
//        mRecyclerAdapter.setOnItemClickListener(this);
//        mRecyclerView.setAdapter(mRecyclerAdapter);

//        mBroadcastReceiver = new CustomReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.kamcord.RecordService");
//        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onItemClick(View view, int position) {
        mSelectedGame = mSupportedGameList.get(position);
        selectdGameListener listener = (selectdGameListener) getActivity();
        listener.selectedGame(mSelectedGame);
        Toast.makeText(mContext,
                "You will record " + mSelectedGame.name,
                Toast.LENGTH_SHORT)
                .show();
    }

    public interface selectdGameListener {
        void selectedGame(com.kamcord.app.kamcord.activity.server.model.Game selectedGameModel);
    }

    private static class GetGamesListCallback implements Callback<GenericResponse<PaginatedGameList>>
    {
        @Override
        public void success(GenericResponse<PaginatedGameList> gamesListWrapper, Response response) {
            Log.v(TAG, "Success!");
            if( gamesListWrapper != null && gamesListWrapper.response != null && gamesListWrapper.response.game_list != null )
            {
                for( Game game : gamesListWrapper.response.game_list )
                {
                    Log.v(TAG, "  " + game.name + ": " + game.play_store_id);
                }
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "Unable to get list of KCP games.");
            Log.e(TAG, "  " + retrofitError.toString());
            // TODO: show the user something about this.
        }
    }
//
//    public String getVideoThumbnail() {
//        return thumbnailString;
//    }
//
//    public class CustomReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            thumbnailString = intent.getStringExtra("ThumbNailPath");
//        }
//    }
}
