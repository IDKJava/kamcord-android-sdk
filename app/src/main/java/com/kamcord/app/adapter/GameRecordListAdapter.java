package com.kamcord.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

import com.kamcord.app.R;
import com.kamcord.app.adapter.viewholder.FirstInstalledViewHolder;
import com.kamcord.app.adapter.viewholder.GameItemViewHolder;
import com.kamcord.app.adapter.viewholder.LastInstalledViewHolder;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.AccountManager;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

public class GameRecordListAdapter extends RecyclerView.Adapter<ViewHolder> {

    public static final int VIEW_TYPE_INSTALLED = 0;
    public static final int VIEW_TYPE_FIRST_INSTALLED = 1;
    public static final int VIEW_TYPE_LAST_INSTALLED = 2;
    public static final int VIEW_TYPE_NOT_INSTALLED = 3;

    private Context mContext;
    private List<Game> mGames;
    private OnGameActionButtonClickListener mOnGameActionButtonClickListener;

    public GameRecordListAdapter(Context context, List<Game> games, OnGameActionButtonClickListener recordButtonClickListener) {
        this.mContext = context;
        this.mGames = games;
        this.mOnGameActionButtonClickListener = recordButtonClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemLayoutView = inflater.inflate(R.layout.view_game_item, null);
        RecyclerView.ViewHolder viewHolder = new GameItemViewHolder(itemLayoutView);

        switch (viewType) {
            case VIEW_TYPE_FIRST_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_first_installed, null);
                viewHolder = new FirstInstalledViewHolder(itemLayoutView);
                break;

            case VIEW_TYPE_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item, null);
                viewHolder = new GameItemViewHolder(itemLayoutView);
                break;

            case VIEW_TYPE_LAST_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_last_installed, null);
                viewHolder = new LastInstalledViewHolder(itemLayoutView);
                break;

            default:
                break;
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Game game = mGames.get(position);

        if (viewHolder instanceof FirstInstalledViewHolder) {
            bindFirstInstalledViewHolder((FirstInstalledViewHolder) viewHolder, game);

        } else if (viewHolder instanceof LastInstalledViewHolder) {
            bindLastInstalledViewHolder((LastInstalledViewHolder) viewHolder, game);

        } else if (viewHolder instanceof GameItemViewHolder) {
            bindInstalledViewHolder((GameItemViewHolder) viewHolder, game);
        }
    }

    private void bindFirstInstalledViewHolder(FirstInstalledViewHolder viewHolder, Game game)
    {
        bindInstalledViewHolder(viewHolder, game);
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.recordAndShareTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindLastInstalledViewHolder(LastInstalledViewHolder viewHolder, Game game)
    {
        bindInstalledViewHolder(viewHolder, game);
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.alsoRecordTheseTextView, "fonts/proximanova_semibold.otf");
        viewHolder.requestGameImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getResources().getString(R.string.communityEmail),});
                intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.canIRecord));
                String body = mContext.getResources().getString(R.string.iWantToRecord) + " \n"
                        + "\n";
                if (AccountManager.isLoggedIn()) {
                    Account account = AccountManager.getStoredAccount();
                    body += String.format(Locale.ENGLISH, mContext.getResources().getString(R.string.sincerely), account.username);
                }
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

    private void bindInstalledViewHolder(GameItemViewHolder viewHolder, final Game game)
    {
        if( game.icons != null && game.icons.regular != null ) {
            Picasso.with(mContext)
                    .load(game.icons.regular)
                    .tag(game.play_store_id)
                    .into(viewHolder.gameThumbnailImageView);
        }

        viewHolder.gameNameTextView.setText(game.name);
        viewHolder.gameFollowerCountTextView.setText(
                String.format(Locale.ENGLISH,
                        mContext.getResources().getString(R.string.followersWithCount),
                        game.number_of_followers));

        ImageButton gameActionImageButton = viewHolder.gameActionImageButton;
        gameActionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnGameActionButtonClickListener != null) {
                    mOnGameActionButtonClickListener.onGameActionButtonClick(game);
                }
            }
        });
        if (game.isRecording) {
            gameActionImageButton.setBackgroundResource(R.drawable.fab_circle_red);
            gameActionImageButton.setImageResource(R.drawable.ic_videocam_off_white_48dp);
            Animation animation = new AlphaAnimation(1f, 0.5f);
            animation.setDuration(500);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            gameActionImageButton.startAnimation(animation);
        } else {
            gameActionImageButton.setBackgroundResource(R.drawable.fab_circle);
            gameActionImageButton.setImageResource(R.drawable.ic_videocam_white_48dp);
            gameActionImageButton.clearAnimation();
        }
    }



    @Override
    public int getItemCount() {
        return mGames.size();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = VIEW_TYPE_NOT_INSTALLED;

        Game game = mGames.get(position);
        if (game.isInstalled) {
            if (position == 0) {
                viewType = VIEW_TYPE_FIRST_INSTALLED;

            } else if (position + 1 > mGames.size() || !mGames.get(position + 1).isInstalled) {
                viewType = VIEW_TYPE_LAST_INSTALLED;

            } else {
                viewType = VIEW_TYPE_INSTALLED;
            }
        }

        return viewType;
    }

    public interface OnGameActionButtonClickListener {
        void onGameActionButtonClick(Game game);
    }
}
