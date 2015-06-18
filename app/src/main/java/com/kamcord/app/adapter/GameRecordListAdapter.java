package com.kamcord.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
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
import com.kamcord.app.adapter.viewholder.GameItemViewHolder;
import com.kamcord.app.adapter.viewholder.InstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.NotInstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.RequestGameViewHolder;
import com.kamcord.app.model.RecordItem;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.StringUtils;
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
    private List<RecordItem> mRecordItems;
    private OnGameActionButtonClickListener mOnGameActionButtonClickListener;

    public GameRecordListAdapter(Context context, List<RecordItem> recordItems, OnGameActionButtonClickListener recordButtonClickListener) {
        this.mContext = context;
        this.mRecordItems = recordItems;
        this.mOnGameActionButtonClickListener = recordButtonClickListener;
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

    private void bindInstalledHeaderViewHolder(InstalledHeaderViewHolder viewHolder)
    {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.recordAndShareTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindNotInstalledHeaderViewHolder(NotInstalledHeaderViewHolder viewHolder)
    {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.alsoRecordTheseTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindRequestGameViewHolder(RequestGameViewHolder viewHolder)
    {
        viewHolder.requestGameImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getResources().getString(R.string.gameRequestEmail),});
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

    private void bindGameItemViewHolder(GameItemViewHolder viewHolder, final Game game)
    {
        if( game.icons != null && game.icons.regular != null ) {
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

        if( game.isInstalled ) {
            if (game.isRecording) {
                gameActionImageButton.setBackgroundResource(R.drawable.hollow_red_circle_background);
                gameActionImageButton
                    .setContentDescription(mContext.getResources().getString(R.string.recording));
                gameActionImageButton.setImageResource(R.drawable.ic_videocam_off_white_48dp);
                gameActionImageButton.setColorFilter(mContext.getResources().getColor(R.color.stopRecordingRed), PorterDuff.Mode.MULTIPLY);

                Animation animation = new AlphaAnimation(1f, 0.5f);
                animation.setDuration(500);
                animation.setRepeatCount(Animation.INFINITE);
                animation.setRepeatMode(Animation.REVERSE);
                gameActionImageButton.startAnimation(animation);
            } else {
                gameActionImageButton.setBackgroundResource(R.drawable.hollow_circle_background);
                gameActionImageButton
                    .setContentDescription(mContext.getResources().getString(R.string.idle));
                gameActionImageButton.setImageResource(R.drawable.ic_videocam_white_48dp);
                gameActionImageButton.setColorFilter(mContext.getResources().getColor(R.color.kamcordGreen), PorterDuff.Mode.MULTIPLY);

                gameActionImageButton.clearAnimation();
            }
        } else {
            gameActionImageButton.setBackgroundResource(R.drawable.hollow_blue_circle_background);
            gameActionImageButton.setImageResource(R.drawable.install_icon);
            gameActionImageButton.setColorFilter(mContext.getResources().getColor(R.color.kamcordBlue), PorterDuff.Mode.MULTIPLY);
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
