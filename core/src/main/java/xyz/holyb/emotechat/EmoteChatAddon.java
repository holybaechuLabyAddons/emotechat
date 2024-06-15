package xyz.holyb.emotechat;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.notification.Notification;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import xyz.holyb.emotechat.emote.EmoteProvider;
import xyz.holyb.emotechat.listener.ChatMessageSendListener;
import xyz.holyb.emotechat.listener.ChatReceiveListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class LegacyConfig {
  Map<String, BTTVEmote> emotes;
}

@AddonMain
public class EmoteChatAddon extends LabyAddon<EmoteChatConfiguration> {
  private static EmoteChatAddon instance;

  public EmoteChatAddon(){
    instance = this;
  }

  public static EmoteChatAddon get(){
    return instance;
  }

  @Override
  protected void enable() {
    // Migrate configuration to new API
    try {
      JsonReader fileReader = new JsonReader(new FileReader(Laby.labyAPI().labyModLoader().getGameDirectory() + "/labymod-neo/configs/emotechat/settings.json"));
      LegacyConfig config = new Gson().fromJson(fileReader, LegacyConfig.class);

      migrateFromLegacyConfiguration(config);
    } catch (FileNotFoundException ignored) {}

    this.registerSettingCategory();

    this.registerListener(new ChatReceiveListener(this));
    this.registerListener(new ChatMessageSendListener(this));

    this.logger().info("Enabled the EmoteChat v" + this.addonInfo().getVersion());
  }

  @Override
  protected Class<EmoteChatConfiguration> configurationClass() {
    return EmoteChatConfiguration.class;
  }

  private void migrateFromLegacyConfiguration(LegacyConfig config) {
    Optional<BTTVEmote> firstEmote = config.emotes.values().stream().findFirst();
    if (firstEmote.isPresent() && Objects.isNull(firstEmote.get().user)) return;

    this.configuration().emotes = new HashMap<>();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (Entry<String, BTTVEmote> entry : config.emotes.entrySet()) {
      futures.add(CompletableFuture.supplyAsync(() -> {
        BTTVEmote emote = entry.getValue();
        String name = entry.getKey();

        if (Objects.isNull(emote.user)) return null;

        this.configuration().emotes.put(name, EmoteProvider.addBTTV(emote.id));

        return null;
      }));
    }

    CompletableFuture<Void> combined = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    combined.join();

    this.saveConfiguration();
  }
}
