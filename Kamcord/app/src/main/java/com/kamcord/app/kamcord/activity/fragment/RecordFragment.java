package com.kamcord.app.kamcord.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.utils.FileManagement;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;

import java.util.ArrayList;

public class RecordFragment extends Fragment implements GameRecordListAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private GameModel mSelectedGame = null;

    private Context mContext;

    private FileManagement mFileManagement;

    private ArrayList<GameModel> mSupportedGameList = new ArrayList<GameModel>() {
        {
            add(new GameModel("jp.ne.mkb.games.BOOSTBEAST", "jp.ne.mkb.games.BOOSTBEAST", R.drawable.boost_beast));
            add(new GameModel("com.rovio.BadPiggies", "com.rovio.BadPiggies", R.drawable.bad_piggies));
            add(new GameModel("com.yodo1.crossyroad", "com.yodo1.crossyroad", R.drawable.crossy_road));
            add(new GameModel("com.fingersoft.hillclimb", "com.fingersoft.hillclimb", R.drawable.hill_racing));
            add(new GameModel("com.madfingergames.deadtrigger2", "com.madfingergames.deadtrigger2", R.drawable.dead_trigger));
            add(new GameModel("com.kabam.marvelbattle", "com.kabam.marvelbattle", R.drawable.marvel));
        }
    };

    private String thumbnailString;
//    private CustomReceiver mBroadcastReceiver;

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

        mRecyclerView = (RecyclerView) v.findViewById(R.id.record_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(mContext, mSupportedGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

//        mBroadcastReceiver = new CustomReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.kamcord.RecordService");
//        mContext.registerReceiver(mBroadcastReceiver, filter);

    }

    @Override
    public void onItemClick(View view, int position) {
        // Package for Launch Game
        mSelectedGame = mSupportedGameList.get(position);
        selectdGameListener listener = (selectdGameListener) getActivity();
        listener.selectedGame(mSelectedGame);
        Toast.makeText(mContext,
                "You will record " + mSelectedGame.getPackageName(),
                Toast.LENGTH_SHORT)
                .show();
    }

    public interface selectdGameListener {
        public void selectedGame(GameModel selectedGameModel);
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
