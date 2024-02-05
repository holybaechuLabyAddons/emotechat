package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.concurrent.task.Task;
import net.labymod.api.util.logging.Logging;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;

import java.util.*;

public class NewChatReceiveListener {
  // TODO: integrate api to use emotes without using long string (once i buy server for that)
  // ^ reference: https://github.com/EmoteChat/EmoteChat/blob/master/emotechat-core/src/main/java/de/emotechat/addon/bttv/EmoteProvider.java

  private final EmoteChatAddon addon;

  public NewChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private String isEmote(String word) {
    // ex: <:pepoG:5d63e543375afb1da9a68a5a@BTTV>

    if (!(
        word.startsWith("<") &&
            word.endsWith(">")
    )) return null;

    String[] parts = word.substring(2, word.length()-1).split("[:@]");

    if (parts.length != 3) return null;
    if (
        parts[0].matches("^[A-Za-z0-9_.]+$") && // Emote slug/name
        parts[1].matches("^[A-Za-z0-9_.]+$") && // Emote ID from provider
        Objects.equals(parts[2], "BTTV") // Emote provider (only BTTV for now)
    ) return parts[1];

    return null;
  }

  private String containsEmote(String string) {
    for (String word : string.split(" ")) {
      String emoteID = isEmote(word);
      if (Objects.nonNull(emoteID)) return emoteID;
    }

    return null;
  }

  private TextComponent replaceEmote(TextComponent component) {
    // ! Needs to be called when component contains emote

    Collection<Component> children = new ArrayList<>();

    StringBuilder nonEmoteString = new StringBuilder();
    for (String word : component.getText().split(" ")) {
      String emoteID = isEmote(word);

      Logging.getLogger().info(word);

      if (Objects.isNull(emoteID)) {
        nonEmoteString.append(word).append(" ");
      } else {
        if (!nonEmoteString.isEmpty()) {
          children.add(component.copy().text(nonEmoteString.toString()));
          nonEmoteString = new StringBuilder();
        }

        // Emote
        BTTVEmote bttvEmote = BTTVEmote.id(emoteID);

        Component emoteComponent = Component.empty();
        emoteComponent.setChildren(List.of(
            Component.icon(Icon.url(bttvEmote.getImageURL(addon.configuration().emoteQuality().get()))),
            component.copy().text(" ")
        ));

        children.add(emoteComponent);
      }
    }

    if (!nonEmoteString.isEmpty()) {
      children.add(component.copy().text(nonEmoteString.toString()));
    }

    return Component.empty().setChildren(children);
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    ChatMessage message = event.chatMessage();

    if (message.component().getChildren().isEmpty()){
      String emoteID = containsEmote(message.getFormattedText());

      if (Objects.isNull(emoteID)) return;

      Task.builder(() -> {
        // Execute asynchronously
        Task.builder(() -> {
          // Execute on render thread to prevent emote not being rendered for a long time
          message.edit(replaceEmote(Component.text(message.getFormattedText())));
        }).build().execute();
      }).build().executeOnRenderThread();
    }
  }
}
