package xyz.holyb.emotechat.listener;

import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.IconComponent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.client.chat.ChatMessage;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import xyz.holyb.emotechat.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class AnimatedEmoteRenderer {
  private final ImageUtils imageUtils = new ImageUtils();
  private final EmoteChatAddon addon;

  private final Map<String, List<String>> animatedEmotes = new HashMap<>();
  private final Map<String, Integer> frameCounts = new HashMap<>();
  private final Map<String, List<ChatMessage>> messages = new HashMap<>();

  public AnimatedEmoteRenderer(EmoteChatAddon addon) {
    this.addon = addon;
  }

  public IconComponent addAnimatedEmote(BTTVEmote emote, ChatMessage message) throws IOException {
    Integer emoteQuality = addon.configuration().emoteQuality().get();
    String key = emote.legacyGlobalId.emoteId+"x"+emoteQuality;
    if (!this.animatedEmotes.containsKey(key)) {
      List<String> emoteFrames = new ArrayList<>();
      for (BufferedImage image : imageUtils.getBufferedImagesFromGIF(emote.getImageURL(emoteQuality))) {
        String base64 = "data:image/png;base64,"+imageUtils.getBase64FromImage(image);
        emoteFrames.add(base64);
      }
      this.animatedEmotes.put(key, emoteFrames);
      this.frameCounts.put(key, 0);
    }

    if (!this.messages.containsKey(key)) {
      this.messages.put(key, new ArrayList<>());
    }
    this.messages.get(key).add(message);

    return Component.icon(
        Icon.url(this.animatedEmotes.get(key).get(0)),
        Style.builder().color(NamedTextColor.WHITE).build(),
        addon.configuration().emoteSize().get()
    );
  }

  private void updateIcon(List<Component> components, String url) {
    for (Component component : components) {
      if (!component.getChildren().isEmpty()) updateIcon(component.getChildren(), url);

      if (component instanceof IconComponent iconComponent){
        iconComponent.setIcon(Icon.url(url)).setSize(addon.configuration().emoteSize().get());
      }
    }
  }

  @Subscribe
  public void onGameTick(GameTickEvent e) {
    List<String> emotesToRemove = new ArrayList<>();

    animatedEmotes.forEach((globalId, base64s) -> {
      List<ChatMessage> messages = this.messages.get(globalId);
      Iterator<ChatMessage> iterator = messages.iterator();

      if (frameCounts.get(globalId) >= base64s.size()) {
        frameCounts.replace(globalId, 0);
      }

      while (iterator.hasNext()) {
        ChatMessage message = iterator.next();

        if (!this.addon.configuration().doNotCheckForUnknownMessages().get() && Laby.labyAPI().chatProvider().chatController().getMessages().contains(message)) {
          iterator.remove();
          if (this.messages.get(globalId).isEmpty()) {
            emotesToRemove.add(globalId);
          }
          continue;
        }

        updateIcon(message.component().getChildren(), this.animatedEmotes.get(globalId).get(frameCounts.get(globalId)));
        message.edit(message.component());
      }
      frameCounts.replace(globalId, frameCounts.get(globalId) + 1);
    });

    for (String emote : emotesToRemove) {
      this.animatedEmotes.remove(emote);
      this.frameCounts.remove(emote);
      this.messages.remove(emote);
    }
  }
}