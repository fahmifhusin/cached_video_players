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
public class CachedVideoPlayerPlugin implements FlutterPlugin, MethodCallHandler, VideoPlayerApi {
  private MethodChannel channel;
  private FlutterPluginBinding pluginBinding;
  private final Map<String, CachedVideoPlayer> players = new HashMap<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    pluginBinding = flutterPluginBinding;

    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "cached_video_player");
    channel.setMethodCallHandler(this);

    // Setup Pigeon VideoPlayerApi
    VideoPlayerApi.setup(flutterPluginBinding.getBinaryMessenger(), this);
  }

  // ===== IMPLEMENTASI VIDEO PLAYER API =====

  @Override
  public void initialize() {
    // Method initialize yang sebelumnya missing - TAMBAHKAN INI
    // Ini dipanggil dari Flutter side ketika plugin diinisialisasi
    // Bisa digunakan untuk setup awal cache manager atau resources lainnya
    try {
      // Untuk sekarang kita biarkan kosong, atau tambahkan inisialisasi jika diperlukan
      System.out.println("CachedVideoPlayer initialized");
    } catch (Exception e) {
      System.err.println("Error initializing CachedVideoPlayer: " + e.getMessage());
    }
  }

  @Override
  public void create(@NonNull CreateMessage arg) {
    // Method create yang sudah ada - JANGAN DIUBAH
    Map<String, Object> createResult = new HashMap<>();
    try {
      String dataSource = arg.getDataSource();
      String formatHint = arg.getFormatHint();
      Map<String, String> httpHeaders = arg.getHttpHeaders();

      TextureMessage output = create(dataSource, formatHint, httpHeaders);
      createResult.put("result", output);
    } catch (Exception exception) {
      createResult.put("error", wrapError(exception));
    }
  }

  // Helper method untuk create
  public TextureMessage create(String dataSource, String formatHint, Map<String, String> httpHeaders) {
    final CachedVideoPlayer player = new CachedVideoPlayer(
            pluginBinding.getTextureRegistry(),
            pluginBinding.getApplicationContext()
    );

    String playerId = player.create(dataSource, formatHint, httpHeaders);
    players.put(playerId, player);

    TextureMessage output = new TextureMessage();
    output.setTextureId((long) player.getTextureId());
    return output;
  }

  @Override
  public void dispose(@NonNull VideoPlayerMessage arg) {
    String playerId = arg.getPlayerId();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.dispose();
      players.remove(playerId);
    }
  }

  @Override
  public void setLooping(@NonNull LoopingMessage arg) {
    String playerId = arg.getPlayerId();
    Boolean isLooping = arg.getIsLooping();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.setLooping(isLooping);
    }
  }

  @Override
  public void setVolume(@NonNull VolumeMessage arg) {
    String playerId = arg.getPlayerId();
    Double volume = arg.getVolume();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.setVolume(volume.floatValue());
    }
  }

  @Override
  public void play(@NonNull VideoPlayerMessage arg) {
    String playerId = arg.getPlayerId();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.play();
    }
  }

  @Override
  public void pause(@NonNull VideoPlayerMessage arg) {
    String playerId = arg.getPlayerId();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.pause();
    }
  }

  @Override
  public void seekTo(@NonNull PositionMessage arg) {
    String playerId = arg.getPlayerId();
    Long position = arg.getPosition();
    CachedVideoPlayer player = players.get(playerId);
    if (player != null) {
      player.seekTo(position.intValue());
    }
  }

  @Override
  public void setMixWithOthers(@NonNull MixWithOthersMessage arg) {
    // Implementation bisa kosong jika tidak diperlukan
    Boolean mixWithOthers = arg.getMixWithOthers();
    // Handle audio mixing settings jika diperlukan
  }

  // ===== METHOD CHANNEL HANDLER =====

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    // Handler untuk MethodCall legacy
    switch (call.method) {
      case "init":
        // Handle init method call
        initialize();
        result.success(null);
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    // Cleanup semua players
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

  // ===== HELPER METHODS =====

  private Map<String, Object> wrapError(Exception exception) {
    Map<String, Object> errorMap = new HashMap<>();
    errorMap.put("message", exception.toString());
    errorMap.put("code", exception.getClass().getSimpleName());
    return errorMap;
  }
}