package com.animetv.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Native ExoPlayer screen. The WebView hands it a resolved stream URL (HLS / MP4)
 * via the ZenkaiNative.play(...) JS bridge — so the TV app can actually play
 * sources the WebView <video>/iframe path can't (HLS, header-gated embeds, etc.).
 */
@UnstableApi
public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setKeepContentOnPlayerReset(true);
        setContentView(playerView);
        applyImmersive();

        String url = getIntent().getStringExtra("url");
        String type = getIntent().getStringExtra("type");        // "hls" | "mp4" | null
        String headersJson = getIntent().getStringExtra("headers");

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "No video URL provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000);
        Map<String, String> headers = parseHeaders(headersJson);
        if (!headers.isEmpty()) http.setDefaultRequestProperties(headers);

        MediaItem.Builder item = new MediaItem.Builder().setUri(Uri.parse(url));
        if ("hls".equalsIgnoreCase(type)) item.setMimeType(MimeTypes.APPLICATION_M3U8);
        else if ("mp4".equalsIgnoreCase(type)) item.setMimeType(MimeTypes.VIDEO_MP4);

        player = new ExoPlayer.Builder(this)
            .setMediaSourceFactory(new DefaultMediaSourceFactory(http))
            .build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Playback error: " + error.getErrorCodeName(), Toast.LENGTH_LONG).show();
            }
        });
        player.setMediaItem(item.build());
        player.setPlayWhenReady(true);
        player.prepare();
    }

    private Map<String, String> parseHeaders(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        try {
            JSONObject o = new JSONObject(json);
            Iterator<String> keys = o.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                map.put(k, o.getString(k));
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    private void applyImmersive() {
        playerView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
