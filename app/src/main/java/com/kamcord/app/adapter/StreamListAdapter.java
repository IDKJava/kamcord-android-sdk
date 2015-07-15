package com.kamcord.app.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.InstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.NotInstalledHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.RequestGameViewHolder;
import com.kamcord.app.adapter.viewholder.StreamItemViewHolder;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by donliang1 on 15/7/10.
 */
public class StreamListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<FeedItem> mFeedItems;

    public StreamListAdapter(Context context, List<FeedItem> feedItems) {
        this.mContext = context;
        this.mFeedItems = feedItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemLayoutView;
        FeedItem.Type type = FeedItem.Type.values()[viewType];
        switch (type) {
            case TEXT_HEADER: {
                itemLayoutView = inflater.inflate(R.layout.view_game_item_installed_header, null);
                return new InstalledHeaderViewHolder(itemLayoutView);
            }
            case STREAM: {
                itemLayoutView = inflater.inflate(R.layout.fragment_stream_item, parent, false);
                return new StreamItemViewHolder(itemLayoutView);
            }
            case VIDEO: {
                itemLayoutView = inflater.inflate(R.layout.view_game_item_request_game, null);
                return new RequestGameViewHolder(itemLayoutView);
            }
            case FOOTER:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_not_installed_header, null);
                return new NotInstalledHeaderViewHolder(itemLayoutView);

            default:
                break;
        }
        return new FooterViewHolder(null);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof InstalledHeaderViewHolder) {
            bindInstalledHeaderViewHolder((InstalledHeaderViewHolder) viewHolder);

        } else if (viewHolder instanceof RequestGameViewHolder) {
            bindRequestGameViewHolder((RequestGameViewHolder) viewHolder);

        } else if (viewHolder instanceof StreamItemViewHolder) {
            Stream stream = getItem(position).getStream();
            if (stream != null) {
                bindStreamVideoItemViewHolder((StreamItemViewHolder) viewHolder, stream);
            }

        } else if (viewHolder instanceof NotInstalledHeaderViewHolder) {
            bindNotInstalledHeaderViewHolder((NotInstalledHeaderViewHolder) viewHolder);
        }
    }

    private void bindInstalledHeaderViewHolder(InstalledHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.recordAndShareTextView, "fonts/proximanova_semibold.otf");
        viewHolder.recordAndShareTextView.setText(mContext.getResources().getString(R.string.livestreamHeader));
    }

    private void bindNotInstalledHeaderViewHolder(NotInstalledHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.alsoRecordTheseTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindRequestGameViewHolder(RequestGameViewHolder viewHolder) {
    }

    @TargetApi(21)
    private void bindStreamVideoItemViewHolder(StreamItemViewHolder viewHolder, final Stream stream) {

        viewHolder.getStreamItemTitle().setText(stream.name);
        final TextView streamViewsTextView = viewHolder.getStreamViews();
        streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
        final ImageView streamImageView = viewHolder.getStreamItemThumbnail();
        if (stream.thumbnails != null && stream.thumbnails.medium != null) {
            Picasso.with(mContext)
                    .load(stream.thumbnails.medium.unsecure_url)
                    .into(streamImageView);
        }
        streamImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stream.current_viewers_count = stream.current_viewers_count + 1;
                streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
                Intent intent = new Intent(mContext, VideoViewActivity.class);
                intent.putExtra(VideoViewActivity.ARG_STREAM,
                        new Gson().toJson(stream));

                mContext.startActivity(intent);
            }
        });

        String username = "";

        if (stream.user != null && stream.user.username != null)
            username = stream.user.username;

        viewHolder.getStreamItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.author), username));
    }

    public FeedItem getItem(int position) {
        return mFeedItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mFeedItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem item = mFeedItems.get(position);
        return item.getType().ordinal();
    }
}


