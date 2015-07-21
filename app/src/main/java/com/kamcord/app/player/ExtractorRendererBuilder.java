package com.kamcord.app.player;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.widget.TextView;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

public class ExtractorRendererBuilder implements Player.RendererBuilder {

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;

    private final Context context;
    private final String userAgent;
    private final Uri uri;
    private final TextView debugTextView;
    private final Extractor extractor;

    public ExtractorRendererBuilder(Context context, String userAgent, Uri uri,
                                    TextView debugTextView, Extractor extractor) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;
        this.debugTextView = debugTextView;
        this.extractor = extractor;
    }

    @Override
    public void buildRenderers(Player player, Player.RendererBuilderCallback callback) {

        // Build the video and audio renderers.
        DataSource dataSource = new DefaultUriDataSource(context, userAgent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, extractor, 2,
                BUFFER_SIZE);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
                null, true, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING, 5000, null,
                player.getMainHandler(), player, 50);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                null, true, player.getMainHandler(), player);

        // Build the debug renderer.
        TrackRenderer debugRenderer = debugTextView != null
                ? new DebugTrackRenderer(debugTextView, player, videoRenderer) : null;

        // Invoke the callback.
        TrackRenderer[] renderers = new TrackRenderer[Player.RENDERER_COUNT];
        renderers[Player.TYPE_VIDEO] = videoRenderer;
        renderers[Player.TYPE_AUDIO] = audioRenderer;
        renderers[Player.TYPE_DEBUG] = debugRenderer;
        callback.onRenderers(null, null, renderers, new DefaultBandwidthMeter());
    }

}
