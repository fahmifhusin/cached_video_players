package com.lazyarts.vikram.cached_video_player;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.util.HashMap;
import java.util.Map;

/** CachedVideoPlayerPlugin */
public class CachedVideoPlayerPlugin implements FlutterPlugin, MethodCallHandler {
  private MethodChannel channel;
  private FlutterPluginBinding pluginBinding;
  private final Map<String, CachedVideoPlayer> players = new HashMap<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    pluginBinding = flutterPluginBinding;

    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "cached_video_player");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "init":
        // Handle init
        initialize();
        result.success(null);
        break;

      case "create":
        // Handle create
        String dataSource = call.argument("dataSource");
        String formatHint = call.argument("formatHint");
        Map<String, String> httpHeaders = call.argument("httpHeaders");

        try {
          Map<String, Object> response = new HashMap<>();
          response.put("textureId", create(dataSource, formatHint, httpHeaders));
          result.success(response);
        } catch (Exception e) {
          result.error("CREATE_ERROR", e.getMessage(), null);
        }
        break;

      case "dispose":
        String playerId = call.argument("playerId");
        dispose(playerId);
        result.success(null);
        break;

      case "setLooping":
        String loopingPlayerId = call.argument("playerId");
        boolean isLooping = call.argument("isLooping");
        setLooping(loopingPlayerId, isLooping);
        result.success(null);
        break;

      case "setVolume":
        String volumePlayerId = call.argument("playerId");
        double volume = call.argument("volume");
        setVolume(volumePlayerId, (float) volume);
        result.success(null);
        break;

      case "play":
        String playPlayerId = call.argument("playerId");
        play(playPlayerId);
        result.success(null);
        break;

      case "pause":
        String pausePlayerId = call.argument("playerId");
        pause(pausePlayerId);
        result.success(null);
        break;

      case "seekTo":
        String seekPlayerId = call.argument("playerId");
        int position = call.argument("position");
        seekTo(seekPlayerId, position);
        result.success(null);
        break;

      default:
        result.notImplemented();
    }
  }

  // Method implementations
  public void initialize() {
    try {
      System.out.println("CachedVideoPlayer initialized");
    } catch (Exception e) {
      System.err.println("Error initializing CachedVideoPlayer: " + e.getMessage());
    }
  }

  public long create(String dataSource, String formatHint, Map<String, String> httpHeaders) {
    final CachedVideoPlayer player = new CachedVideoPlayer(
            pluginBinding.getTextureRegistry(),
            pluginBinding.getApplicationContext()
    );

    String playerId = player.initialize(); // Sesuaikan dengan method yang ada di CachedVideoPlayer
    players.put(playerId, player);

    return (long) player.getTextureId();
  }

  public void dispose(String playerId) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.dispose();
      players.remove(playerId);
    }
  }

  public void setLooping(String playerId, boolean isLooping) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.setLooping(isLooping);
    }
  }

  public void setVolume(String playerId, float volume) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.setVolume(volume);
    }
  }

  public void play(String playerId) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.play();
    }
  }

  public void pause(String playerId) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.pause();
    }
  }

  public void seekTo(String playerId, int position) {
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.seekTo(position);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    for (CachedVideoPlayer player : players.values()) {
      player.dispose();
    }
    players.clear();

    if (channel != null) {
      channel.setMethodCallHandler(null);
      channel = null;
    }
    pluginBinding = null;
  }
}