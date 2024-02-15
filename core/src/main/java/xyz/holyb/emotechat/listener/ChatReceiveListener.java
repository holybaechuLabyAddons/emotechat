package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.concurrent.task.Task;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import xyz.holyb.emotechat.emote.LegacyGlobalId;
import xyz.holyb.emotechat.emote.LegacyServerEmote;
import xyz.holyb.emotechat.utils.ImageUtils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class ChatReceiveListener {
  // TODO: integrate api to use emotes without using long string (once i buy server for that)
  // ^ reference: https://github.com/EmoteChat/EmoteChat/blob/master/emotechat-core/src/main/java/de/emotechat/addon/bttv/EmoteProvider.java

  private final EmoteChatAddon addon;
  private final ImageUtils imageUtils = new ImageUtils();

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private Map<LegacyGlobalId, Integer> containsEmote(String string) {
    Map<LegacyGlobalId, Integer> emotes = new HashMap<>();

    String[] words = string.split(" ");
    for (int i = 0; i < words.length; i++) {
      String word = words[i];

      LegacyGlobalId emoteID = LegacyGlobalId.parse(addon.legacyEmoteProvider.idSplitter, word);
      if (Objects.nonNull(emoteID)) emotes.put(emoteID, i);
    }

    return emotes;
  }

  private TextComponent replaceEmote(TextComponent component, Map<LegacyGlobalId, Integer> emotes, ChatMessage message)
      throws IOException {
    // ! Needs to be called when component contains emote

    Collection<Component> children = new ArrayList<>();

    String[] words = component.getText().split(" ");
    int lastPos = 0;
    for (Entry<LegacyGlobalId, Integer> entry : emotes.entrySet()) {
      LegacyGlobalId emoteID = entry.getKey();
      Integer emotePos = entry.getValue();

      children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos, emotePos))+" "));

      // Emote
      LegacyServerEmote serverEmote = addon.legacyEmoteProvider.retrieveEmoteByGlobalId(emoteID);
      BTTVEmote bttvEmote = new BTTVEmote(serverEmote.globalId, serverEmote.bttvId, serverEmote.name, serverEmote.imageType);

      if (bttvEmote.animated && addon.configuration().animatedEmotes().get()) {
        children.add(
            Component.empty().setChildren(List.of(
                addon.gameTickListener.addAnimatedEmote(message, imageUtils.getBufferedImagesFromGIF(bttvEmote.getImageURL(addon.configuration().emoteQuality().get()))),
                Component.text(" ")
            ))
        );
      } else {
        children.add(
          Component.empty().setChildren(List.of(
            Component.icon(Icon.url(bttvEmote.getImageURL(addon.configuration().emoteQuality().get()))).setSize(addon.configuration().emoteSize().get()),
            Component.text(" ")
          ))
        );
      }

      lastPos = emotePos;
    }

    children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos+1, words.length))));

    return Component.empty().setChildren(children);
  }

  private List<Component> replaceEmoteFromComponents(List<Component> components, ChatMessage message)
      throws IOException {
    List<Component> newComponents = new ArrayList<>();

    for (Component component : components) {
      if (!component.getChildren().isEmpty()) {
        component = Component.empty().setChildren(replaceEmoteFromComponents(component.getChildren(), message));
      }

      if (component instanceof TextComponent textComponent) {
        Map<LegacyGlobalId, Integer> emotes = containsEmote(textComponent.getText());

        if (!emotes.isEmpty()) {
          component = replaceEmote(textComponent, emotes, message);
        }
      }

      newComponents.add(component);
    }

    return newComponents;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    if (!addon.configuration().enabled().get()) return;

    ChatMessage message = event.chatMessage();

    if (message.component().getChildren().isEmpty()){
      Map<LegacyGlobalId, Integer> emotes = containsEmote(message.getFormattedText());

      if (emotes.isEmpty()) return;

      Task.builder(() -> {
        try {
          TextComponent component = replaceEmote(Component.text(message.getFormattedText()), emotes,
              message);
          Task.builder(() -> {
            // Execute on render thread to prevent emote not being rendered for a long time
            message.edit(component);
          }).build().executeOnRenderThread();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).build().execute();
    } else {
      Task.builder(() -> {
        try {
          List<Component> newChildren = replaceEmoteFromComponents(message.component().getChildren(), message);

          if (newChildren.equals(message.component().getChildren())) return;

          // Execute on render thread to prevent emote not being rendered for a long time
          Task.builder(() -> message.edit(message.component().setChildren(newChildren))).build().executeOnRenderThread();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).build().execute();
    }
  }
}