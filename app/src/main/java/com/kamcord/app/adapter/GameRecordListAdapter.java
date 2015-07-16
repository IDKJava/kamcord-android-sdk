package com.kamcord.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.kamcord.app.R;
import com.kamcord.app.adapter.viewholder.GameItemViewHolder;
import com.kamcord.app.adapter.viewholder.InstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.NotInstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.RequestGameViewHolder;
import com.kamcord.app.model.RecordItem;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

public class GameRecordListAdapter extends RecyclerView.Adapter<ViewHolder> {

    public static final int VIEW_TYPE_INSTALLED = 0;
    public static final int VIEW_TYPE_FIRST_INSTALLED = 1;
    public static final int VIEW_TYPE_LAST_INSTALLED = 2;
    public static final int VIEW_TYPE_NOT_INSTALLED = 3;

    private Context mContext;
    private List<RecordItem> mRecordItems;
    private OnGameActionButtonClickListener mOnGameActionButtonClickListener;

    Set<String> installedGameIds = new HashSet<>();
    Set<String> uninstalledGameIds = new HashSet<>();

    public GameRecordListAdapter(Context context, OnGameActionButtonClickListener recordButtonClickListener) {
        this.mContext = context;
        this.mOnGameActionButtonClickListener = recordButtonClickListener;

        mRecordItems = new ArrayList<>();
        reset();
    }

    public void reset() {
        mRecordItems.clear();
        installedGameIds.clear();
        uninstalledGameIds.clear();
        mRecordItems.add(new RecordItem(RecordItem.Type.INSTALLED_HEADER, null));
//        mRecordItems.add(new RecordItem(RecordItem.Type.FETCH_INSTALLED, null));
        mRecordItems.add(new RecordItem(RecordItem.Type.REQUEST_GAME, null));
        mRecordItems.add(new RecordItem(RecordItem.Type.NOT_INSTALLED_HEADER, null));
        mRecordItems.add(new RecordItem(RecordItem.Type.FETCH_MORE, null));
        this.notifyDataSetChanged();
    }


    public void addGames(List<Game> games) {
        int minIndex = mRecordItems.size();
        int maxIndex = 0;
        for( Game game : games ) {
            if( installedGameIds.contains(game.game_primary_id) ) {
                int index = findGameItemIndex(game);
                if( game.isInstalled ) {
                    if( index != GAME_NOT_FOUND ) {
                        mRecordItems.set(index, new RecordItem(RecordItem.Type.GAME, game));
                    }

                } else {
                    installedGameIds.remove(game.game_primary_id);
                    uninstalledGameIds.add(game.game_primary_id);
                    if( index != GAME_NOT_FOUND ) {
                        mRecordItems.remove(index);
                        int newIndex = installedGameIds.size() + 3;
                        mRecordItems.add(newIndex, new RecordItem(RecordItem.Type.GAME, game));
                    }

                }

            } else if( uninstalledGameIds.contains(game.game_primary_id) ) {
                int index = findGameItemIndex(game);
                if( game.isInstalled ) {
                    installedGameIds.add(game.game_primary_id);
                    uninstalledGameIds.remove(game.game_primary_id);
                    if( index != GAME_NOT_FOUND ) {
                        mRecordItems.remove(index);
                        int newIndex = 1;
                        mRecordItems.add(newIndex, new RecordItem(RecordItem.Type.GAME, game));
                    }

                } else {
                    if( index != GAME_NOT_FOUND ) {
                        mRecordItems.set(index, new RecordItem(RecordItem.Type.GAME, game));
                    }

                }

            } else {
                if( game.isInstalled ) {
                    installedGameIds.add(game.game_primary_id);
                    int index = 0;
                    for( RecordItem item : mRecordItems ) {
                        if( item.getType() == RecordItem.Type.REQUEST_GAME ) {
                            break;
                        }
                        index++;
                    }
                    mRecordItems.add(index, new RecordItem(RecordItem.Type.GAME, game));

                } else {
                    uninstalledGameIds.add(game.game_primary_id);
                    int index = mRecordItems.get(mRecordItems.size() - 1).getType() == RecordItem.Type.FETCH_MORE ?
                            mRecordItems.size() - 1 : mRecordItems.size();
                    mRecordItems.add(index, new RecordItem(RecordItem.Type.GAME, game));

                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeMoreSpinner() {
        int size = mRecordItems.size();
        if( mRecordItems.get(size-1).getType() == RecordItem.Type.FETCH_MORE) {
            mRecordItems.remove(size-1);
            notifyItemRemoved(size-1);
        }
    }

    private static final int GAME_NOT_FOUND = -1;
    private int findGameItemIndex(Game game) {
        int index = 0;
        boolean foundGame = false;
        for( RecordItem item : mRecordItems ) {
            if( item.getType() == RecordItem.Type.GAME
                    && item.getGame() != null
                    && item.getGame().game_primary_id != null
                    && item.getGame().game_primary_id.equals(game.game_primary_id) ) {
                foundGame = true;
                break;
            }
            index++;
        }
        return foundGame ? index : GAME_NOT_FOUND;
    }


    public int size() {
        return mRecordItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemLayoutView = inflater.inflate(R.layout.view_game_item, null);
        RecyclerView.ViewHolder viewHolder = new GameItemViewHolder(itemLayoutView);

        RecordItem.Type type = RecordItem.Type.values()[viewType];
        switch (type) {
            case INSTALLED_HEADER:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_installed_header, null);
                viewHolder = new InstalledHeaderViewHolder(itemLayoutView);
                break;

            case GAME:
                itemLayoutView = inflater.inflate(R.layout.view_game_item, null);
                viewHolder = new GameItemViewHolder(itemLayoutView);
                break;

            case REQUEST_GAME:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_request_game, null);
                viewHolder = new RequestGameViewHolder(itemLayoutView);
                break;

            case NOT_INSTALLED_HEADER:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_not_installed_header, null);
                viewHolder = new NotInstalledHeaderViewHolder(itemLayoutView);
                break;

            case FETCH_INSTALLED:
            case FETCH_MORE:
                ProgressBar progressSpinner = new ProgressBar(parent.getContext());
                progressSpinner.setIndeterminate(true);
                viewHolder = new ViewHolder(progressSpinner) {};
                break;

            default:
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        RecordItem item = mRecordItems.get(position);

        if (viewHolder instanceof InstalledHeaderViewHolder) {
            bindInstalledHeaderViewHolder((InstalledHeaderViewHolder) viewHolder);

        } else if (viewHolder instanceof RequestGameViewHolder) {
            bindRequestGameViewHolder((RequestGameViewHolder) viewHolder);

        } else if (viewHolder instanceof GameItemViewHolder) {
            Game game = item.getGame();
            bindGameItemViewHolder((GameItemViewHolder) viewHolder, game);

        } else if (viewHolder instanceof NotInstalledHeaderViewHolder) {
            bindNotInstalledHeaderViewHolder((NotInstalledHeaderViewHolder) viewHolder);
        }
    }

    private void bindInstalledHeaderViewHolder(InstalledHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.recordAndShareTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindNotInstalledHeaderViewHolder(NotInstalledHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.alsoRecordTheseTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindRequestGameViewHolder(RequestGameViewHolder viewHolder) {
        viewHolder.requestGameImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getResources().getString(R.string.gameRequestEmail),});
                intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.canIRecord));
                String body = mContext.getResources().getString(R.string.iWantToRecord);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.setType("*/*");
                intent.setData(Uri.parse("mailto:"));
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                } else {
                    // TODO: show the user there's no app to handle emails.
                }
            }
        });
    }

    private void bindGameItemViewHolder(GameItemViewHolder viewHolder, final Game game) {
        if (game.icons != null && game.icons.regular != null) {
            Picasso.with(mContext)
                    .load(game.icons.regular)
                    .tag(game.play_store_id)
                    .into(viewHolder.gameThumbnailImageView);
        }

        viewHolder.gameNameTextView.setText(game.name);
        viewHolder.gameVideoCountTextView.setText(
                String.format(Locale.ENGLISH,
                        mContext.getResources().getQuantityString(R.plurals.videosWithCount, game.number_of_videos),
                        StringUtils.commatizedCount(game.number_of_videos)));

        ImageButton gameActionImageButton = viewHolder.gameActionImageButton;
        gameActionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnGameActionButtonClickListener != null) {
                    mOnGameActionButtonClickListener.onGameActionButtonClick(game);
                }
            }
        });

        if (game.isInstalled) {
            if (!game.isRecording) {
                gameActionImageButton.setBackgroundResource(R.drawable.hollow_circle_background);
                gameActionImageButton.setContentDescription(mContext.getResources().getString(R.string.idle));
                gameActionImageButton.setImageResource(R.drawable.button_record_selector);
                gameActionImageButton.clearAnimation();
            }
        } else {
            gameActionImageButton.setBackgroundResource(R.drawable.hollow_blue_circle_background);
            gameActionImageButton.setImageResource(R.drawable.button_download_selector);
        }
    }


    @Override
    public int getItemCount() {
        return mRecordItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        RecordItem item = mRecordItems.get(position);
        return item.getType().ordinal();
    }

    public interface OnGameActionButtonClickListener {
        void onGameActionButtonClick(Game game);
    }
}
